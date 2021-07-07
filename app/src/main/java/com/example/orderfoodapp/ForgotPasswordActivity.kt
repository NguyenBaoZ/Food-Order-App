package com.example.orderfoodapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_forgot_password.*

class ForgotPasswordActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        back_button.setOnClickListener() {
            finish()
        }

        forgotPassword_button.setOnClickListener() {
            resetPassword()
        }
    }

    private fun resetPassword() {
        val email: String = email_editText.text.toString()
        if (email.isEmpty()) {
            email_editText.error = "Email can't be empty"
            email_editText.requestFocus()
        }
        else {
            val auth = FirebaseAuth.getInstance()
            auth.sendPasswordResetEmail(email).addOnCompleteListener() { task ->
                if(task.isSuccessful) {
                    Toast.makeText(this, "Please check your mail to reset password", Toast.LENGTH_LONG).show()
                }
                else {
                    Toast.makeText(this, "Something went wrong! Try again!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}