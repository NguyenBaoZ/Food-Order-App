package com.example.orderfoodapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileActivity : AppCompatActivity() {
    private lateinit var signout_textView: TextView
    private lateinit var profilemail_textView: TextView

    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        mAuth = Firebase.auth

        profilemail_textView = findViewById(R.id.profile_mail)
//        var i:Intent = intent
//        var bundle : Bundle? =i.extras
//        var email : String? = bundle?.getString("email")
//        profilemail_textView.text = email

        signout_textView = findViewById(R.id.sign_out_text)
        signout_textView.setOnClickListener(){
            mAuth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}