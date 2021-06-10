package com.example.orderfoodapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
   private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val bundle = intent.extras

        if(bundle != null) {
            val newEmail = intent.getStringExtra("email").toString()
            val newPassword = intent.getStringExtra("password").toString()

            email_editText.setText(newEmail)
            password_editText.setText(newPassword)
        }

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
                        startActivity(Intent(this, MainMenuActivity::class.java))
                    } else {
                        Toast.makeText(this, "Login Error: " + task.exception, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }
    }
}