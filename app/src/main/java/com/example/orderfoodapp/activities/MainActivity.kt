package com.example.orderfoodapp.activities

import android.R.attr.data
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.orderfoodapp.R
import com.example.orderfoodapp.adapters.LoginAdapter
import com.example.orderfoodapp.fragments.LoginFragment
import com.example.orderfoodapp.fragments.SignUpFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var loginFragment: Fragment
    private lateinit var signupFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        mAuth = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loginFragment = LoginFragment()
        signupFragment = SignUpFragment()

        val fragments = arrayListOf(loginFragment, signupFragment)
        val adapter = LoginAdapter(fragments, this)
        login_view_pager.adapter = adapter

        TabLayoutMediator(login_tab_layout, login_view_pager) { tab, position ->
            if (position == 0)
                tab.text = "Login"
            else
                tab.text = "Signup"
        }.attach()
    }

    override fun onStart() {
        super.onStart()
        val user = mAuth.currentUser
        Log.d("User", user.toString())
        if(user != null && user.isEmailVerified){
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
        }
    }
}
