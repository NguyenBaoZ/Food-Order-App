package com.example.orderfoodapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlin.math.sign

class LoginActivity : AppCompatActivity() {
    private lateinit var email_editText: EditText
    private lateinit var password_editText: EditText
    private lateinit var login_button: Button
    private lateinit var signup_textView: TextView

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email_editText = findViewById(R.id.email_editText)
        password_editText = findViewById(R.id.password_editText)
        login_button = findViewById(R.id.login_button)
        signup_textView = findViewById(R.id.signup_textView)

        mAuth = Firebase.auth

        login_button.setOnClickListener(){
            loginUser()
        }
        signup_textView.setOnClickListener(){
            startActivity(Intent(this,SignUpActivity::class.java))
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
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(this, "User logged in successfully", Toast.LENGTH_SHORT)
                            .show()
                        startActivity(Intent(this, Main_Menu_Activity::class.java))
                    } else {
                        Toast.makeText(this, "Login Error: " + task.exception, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }
    }
}