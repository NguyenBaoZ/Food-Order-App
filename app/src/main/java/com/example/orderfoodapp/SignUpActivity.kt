package com.example.orderfoodapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_signup.*

class SignUpActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        mAuth = Firebase.auth

        signup_button.setOnClickListener(){
            createUser()
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
                        //send email verify
                        Firebase.auth.currentUser?.sendEmailVerification()
                        // Sign in success, update UI with the signed-in user's information
                        createCustomerData(email)
                        Toast.makeText(this, "User sign up successfully, please check mail to verify account!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        val bundle = Bundle()
                        bundle.putString("email", email)
                        bundle.putString("password", password)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Sign Up Error: " + task.exception,Toast.LENGTH_SHORT).show()
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

