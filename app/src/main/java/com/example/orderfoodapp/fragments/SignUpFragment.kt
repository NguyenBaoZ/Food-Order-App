package com.example.orderfoodapp.fragments

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.orderfoodapp.models.NewCustomer
import com.example.orderfoodapp.R
import com.facebook.login.LoginFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_signup.*

class SignUpFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth

    class KotlinConstantClass {
        companion object {
            var COMPANION_OBJECT_EMAIL = ""
            var COMPANION_OBJECT_PASSWORD = ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = Firebase.auth

        signup_button.setOnClickListener(){
            createUser()
        }
    }
    private fun createUser(){
        val email : String = email_editText.text.toString()
        val password : String = password_editText.text.toString()
        val confirmPassword : String = confirmPassword_editText.text.toString()
        when {
            TextUtils.isEmpty(email) -> {
                email_editText.error = "Email can't be empty"
                email_editText.requestFocus()
            }
            TextUtils.isEmpty(password) -> {
                password_editText.error = "Password can't be empty"
                password_editText.requestFocus()
            }
            TextUtils.isEmpty(confirmPassword) -> {
                confirmPassword_editText.error = "You must confirm password"
                confirmPassword_editText.requestFocus()
            }
            password != confirmPassword -> {
                confirmPassword_editText.error = "Your Confirm is not correct. Please confirm again"
                confirmPassword_editText.requestFocus()
            }
            else -> {
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            //send email verify
                            Firebase.auth.currentUser?.sendEmailVerification()

                            // Sign in success, update UI with the signed-in user's information
                            createCustomerData(email)

                            KotlinConstantClass.COMPANION_OBJECT_EMAIL = email
                            KotlinConstantClass.COMPANION_OBJECT_PASSWORD = password

                            Toast.makeText(requireActivity(), "User sign up successfully, please check mail to verify account!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireActivity(), "Sign Up Error: " + task.exception,Toast.LENGTH_SHORT).show()
                        }
                    }
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
}