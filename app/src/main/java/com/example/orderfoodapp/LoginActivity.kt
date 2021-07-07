package com.example.orderfoodapp

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.facebook.*
import com.facebook.login.LoginManager
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
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.view.*

class LoginActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 123
    private lateinit var callBackManager: CallbackManager

    private var typeOfLogin = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
        requestPermission()

        firebaseAuth = FirebaseAuth.getInstance()
        callBackManager = CallbackManager.Factory.create()

        mAuth = Firebase.auth
        val user = mAuth.currentUser

        if(user != null && user.isEmailVerified) {
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
        }

        val bundle = intent.extras
        if(bundle != null) {
            //if bundle passed from Sign up
            val newEmail = intent.getStringExtra("email").toString()
            val newPassword = intent.getStringExtra("password").toString()
            if(newEmail != "null" && newPassword != "null") {
                email_editText.setText(newEmail)
                password_editText.setText(newPassword)
            }
        }

        login_button.setOnClickListener(){
            loginUser()
        }

        signup_textView.setOnClickListener(){
            startActivity(Intent(this,SignUpActivity::class.java))
        }

        google_login_button.setOnClickListener() {
            typeOfLogin = 1
            createGoogleRequest()
            signInByGoogle()
        }

        facebook_view.setOnClickListener() {
            facebookButton.performClick()
        }

        facebookButton.setReadPermissions(listOf("public_profile", "email"))
        facebookButton.setOnClickListener() {
            typeOfLogin = 2
            signInByFacebook()
        }

        forgotpassword_textView.setOnClickListener() {
            LoginManager.getInstance().logOut()
        }
    }

    private fun loginUser() {
        val email: String = email_editText.text.toString()
        val password: String = password_editText.text.toString()
        if (TextUtils.isEmpty(email)) {
            email_editText.error = "Email can't be empty"
            email_editText.requestFocus()
        } else if (TextUtils.isEmpty(password)) {
            password_editText.error = "Password can't be empty"
            password_editText.requestFocus()
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = mAuth.currentUser
                        if(user?.isEmailVerified == false) {
                            user.sendEmailVerification()
                            Toast.makeText(this, "Please check mail and verify your account!", Toast.LENGTH_LONG).show()
                        }
                        else {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(this, "User logged in successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, MainMenuActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        Toast.makeText(this, "Login Error: " + task.exception, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun requestPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
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

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signInByGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = mAuth.currentUser
                    Log.d("msgLogin", "signInWithCredential success with email: ${user?.email}")
                    user?.email?.let { checkAccountExist(it) }
                } else {
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
                    startActivity(Intent(this@LoginActivity, MainMenuActivity::class.java))
                }
                else {
                    createCustomerData(email)
                    startActivity(Intent(this@LoginActivity, MainMenuActivity::class.java))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


    private fun signInByFacebook() {
        facebookButton.registerCallback(callBackManager, object :FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                handleFacebookAccessToken(result!!.accessToken)
            }

            override fun onCancel() {
                TODO("Not yet implemented")
            }

            override fun onError(error: FacebookException?) {
                TODO("Not yet implemented")
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
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }
    }


}