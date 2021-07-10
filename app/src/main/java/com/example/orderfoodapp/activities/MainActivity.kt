package com.example.orderfoodapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.orderfoodapp.R
import com.example.orderfoodapp.adapters.LoginAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        mAuth = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = LoginAdapter(supportFragmentManager, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
        login_view_pager.adapter = adapter
        login_tab_layout.setupWithViewPager(login_view_pager)
    }

    override fun onStart() {
        super.onStart()
        val user = mAuth.currentUser
        Log.d("User", user.toString())
        if(user != null){
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
            Log.d("TAG",user.toString())
        }
    }
}
