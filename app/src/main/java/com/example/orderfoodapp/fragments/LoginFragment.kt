package com.example.orderfoodapp.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.Intent.getIntent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.orderfoodapp.models.NewCustomer
import com.example.orderfoodapp.R
import com.example.orderfoodapp.activities.MainMenuActivity
import com.example.orderfoodapp.activities.ForgotPasswordActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_login.*


class LoginFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 123
    private lateinit var callBackManager: CallbackManager

    private var typeOfLogin = 0

    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onResume() {
        super.onResume()
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(context as Activity)
        requestPermission()

        firebaseAuth = FirebaseAuth.getInstance()
        callBackManager = CallbackManager.Factory.create()

        mAuth = Firebase.auth
        val user = mAuth.currentUser

        if(user != null && user.isEmailVerified) {
            val intent = Intent(activity, MainMenuActivity::class.java)
            startActivity(intent)
        }

        val emailPassed = SignUpFragment.KotlinConstantClass.COMPANION_OBJECT_EMAIL
        val passwordPassed = SignUpFragment.KotlinConstantClass.COMPANION_OBJECT_PASSWORD

        if(emailPassed.isNotEmpty() && passwordPassed.isNotEmpty()) {
            email_editText.setText(emailPassed)
            password_editText.setText(passwordPassed)
        }

        //init loading dialog
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_loading_login)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        login_button.setOnClickListener{
            dialog.show()
            loginUser()
        }

        google_login_button.setOnClickListener {
            typeOfLogin = 1
            createGoogleRequest()
            signInByGoogle()
        }

        facebook_view.setOnClickListener {
            facebookButton.performClick()
        }

        facebookButton.setReadPermissions(listOf("public_profile", "email"))
        facebookButton.fragment = this
        facebookButton.setOnClickListener {
            typeOfLogin = 2
            signInByFacebook()
        }

        forgotpassword_textView.setOnClickListener {
            startActivity(Intent(requireActivity(), ForgotPasswordActivity::class.java))
        }
    }
    private fun loginUser() {
        val email: String = email_editText.text.toString()
        val password: String = password_editText.text.toString()
        when {
            TextUtils.isEmpty(email) -> {
                email_editText.error = "Email can't be empty"
                email_editText.requestFocus()
            }
            TextUtils.isEmpty(password) -> {
                password_editText.error = "Password can't be empty"
                password_editText.requestFocus()
            }
            else -> {
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            val user = mAuth.currentUser
                            if(user?.isEmailVerified == false) {
                                user.sendEmailVerification()

                                if(dialog.isShowing) {
                                    dialog.dismiss()
                                }

                                Toast.makeText(requireActivity(), "Please check mail and verify your account!", Toast.LENGTH_LONG).show()
                            }
                            else {
                                if(dialog.isShowing) {
                                    dialog.dismiss()
                                }

                                // Sign in success, update UI with the signed-in user's information
                                Toast.makeText(requireActivity(), "User logged in successfully", Toast.LENGTH_SHORT).show()
                                val intent = Intent(requireActivity(), MainMenuActivity::class.java)
                                startActivity(intent)
                            }
                        } else {
                            if(dialog.isShowing) {
                                dialog.dismiss()
                            }
                            Toast.makeText(requireActivity(), "Login Error: " + task.exception, Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun requestPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }
    }

    private fun createGoogleRequest() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun signInByGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        dialog.show()
        super.onActivityResult(requestCode, resultCode, data)

        if(typeOfLogin == 1) {
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d("msgLogin", "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    if(dialog.isShowing) {
                        dialog.dismiss()
                    }
                    // Google Sign In failed, update UI appropriately
                    Log.w("msgLogin", "Google sign in failed", e)
                }
            }
        }

        else if(typeOfLogin == 2) {
            callBackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = mAuth.currentUser
                    Log.d("msgLogin", "signInWithCredential success with email: ${user?.email}")
                    user?.email?.let { checkAccountExist(it) }
                } else {
                    if(dialog.isShowing) {
                        dialog.dismiss()
                    }
                    // If sign in fails, display a message to the user.
                    Log.w("msgLogin", "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun createCustomerData(email: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Customer")
        val newCustomer = NewCustomer(
            "",
            1000,
            "",
            email,
            "",
            "",
            ""
        )
        val key = dbRef.push().key.toString()
        dbRef.child(key).setValue(newCustomer)
    }

    private fun checkAccountExist(email: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Customer")
        val query = dbRef.orderByChild("email").equalTo(email)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    if(dialog.isShowing) {
                        dialog.dismiss()
                    }

                    Toast.makeText(requireActivity(), "User logged in successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), MainMenuActivity::class.java))
                }
                else {
                    createCustomerData(email)
                    if(dialog.isShowing) {
                        dialog.dismiss()
                    }

                    Toast.makeText(requireActivity(), "User logged in successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), MainMenuActivity::class.java))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


    private fun signInByFacebook() {
        facebookButton.registerCallback(callBackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                handleFacebookAccessToken(result!!.accessToken)
            }

            override fun onCancel() {
                if(dialog.isShowing) {
                    dialog.dismiss()
                }
            }

            override fun onError(error: FacebookException?) {
                if(dialog.isShowing) {
                    dialog.dismiss()
                }
            }

        })
    }

    private fun handleFacebookAccessToken(accessToken: AccessToken?) {
        val credential = FacebookAuthProvider.getCredential(accessToken!!.token)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                val user = mAuth.currentUser
                user?.email?.let { it1 -> checkAccountExist(it1) }
            }
            .addOnFailureListener {
                Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
            }
    }

}