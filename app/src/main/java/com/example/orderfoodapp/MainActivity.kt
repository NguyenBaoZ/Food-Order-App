package com.example.orderfoodapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.orderfoodapp.fragments.FavouriteFragment
import com.example.orderfoodapp.fragments.MainMenuFragment
import com.example.orderfoodapp.fragments.MenuFragment
import com.example.orderfoodapp.fragments.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main_menu.*

class MainActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    override fun onStart() {
        super.onStart()
        mAuth = Firebase.auth
        val user = mAuth.currentUser

        if(user == null){
            startActivity(Intent(this, LoginActivity::class.java))
        }
        else {
            startActivity(Intent(this, MainMenuActivity::class.java))
        }
    }
}
