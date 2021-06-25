package com.example.orderfoodapp

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*

class LoginActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var fusedLocationProvider: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
        requestPermission()

        mAuth = Firebase.auth
        val user = mAuth.currentUser

        if(user != null) {
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
        }
        else {
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
        }

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
                        val intent = Intent(this, MainMenuActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Login Error: " + task.exception, Toast.LENGTH_SHORT)
                            .show()
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

}