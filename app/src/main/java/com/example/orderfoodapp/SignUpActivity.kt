package com.example.orderfoodapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    private lateinit var email_editText: EditText
    private lateinit var password_editText: EditText
    private lateinit var confirmPassword_editText: EditText
    private lateinit var enterpriseRegister_textView: TextView
    private lateinit var signup_button: Button
    private lateinit var login_textView: TextView

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        email_editText = findViewById(R.id.email_editText)
        password_editText = findViewById(R.id.password_editText)
        confirmPassword_editText = findViewById(R.id.confirmPassword_editText)
        enterpriseRegister_textView = findViewById(R.id.enterpriseRegister_textView)
        signup_button = findViewById(R.id.signup_button)
        login_textView = findViewById(R.id.login_textView)

        mAuth = Firebase.auth

        signup_button.setOnClickListener(){
            createUser()
        }
        enterpriseRegister_textView.setOnClickListener(){
            startActivity(Intent(this,LoginActivity::class.java))
        }
        login_textView.setOnClickListener(){
            startActivity(Intent(this,LoginActivity::class.java))
        }
    }
    private fun createUser(){
        val email : String = email_editText.text.toString()
        val password : String = password_editText.text.toString()
        val confirmPassword : String = confirmPassword_editText.text.toString()
        if(TextUtils.isEmpty(email)){
            email_editText.error = "Email can't be empty"
            email_editText.requestFocus()
        }
        else if(TextUtils.isEmpty(password)){
            password_editText.error = "Password can't be empty"
            password_editText.requestFocus()
        }
        else if(TextUtils.isEmpty(confirmPassword)){
            confirmPassword_editText.error = "You must confirm password"
            confirmPassword_editText.requestFocus()
        }else if (password != confirmPassword){
            confirmPassword_editText.error = "Your Confirm is not correct. Please confirm again"
            confirmPassword_editText.requestFocus()
        }else{
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(this, "User sign up successfully",Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this,LoginActivity::class.java))
                    } else {
                        Toast.makeText(this, "Sign Up Error: " + task.exception,Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}

