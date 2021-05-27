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

    private val mainMemuFragment = MainMenuFragment()
    private val profileFragment = ProfileFragment()
    private val menuFragment = MenuFragment()
    private val favouriteFragment = FavouriteFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        //this lines of code should be in MainMenuActivity, but for saving time, i place them here, will replace soon
        //if you need to use this MainActivity, just comment out this code
        //this code is for switching between tabs of bottom navigation bar, NOT do functional task
        replaceFragment(mainMemuFragment)

        bottom_app_bar.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.navbottombar_profile -> {
                    replaceFragment(profileFragment)
                    shoppingCart_button.visibility = View.GONE
                }
                R.id.navbottombar_home -> {
                    replaceFragment(mainMemuFragment)
                    shoppingCart_button.visibility = View.VISIBLE
                }
                R.id.navbottombar_favourite -> {
                    replaceFragment(favouriteFragment)
                    shoppingCart_button.visibility = View.VISIBLE
                }
                R.id.navbottombar_menu -> {
                    replaceFragment(menuFragment)
                    shoppingCart_button.visibility = View.VISIBLE
                }
            }
            true
        }

        shoppingCart_button.setOnClickListener() {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

//    override fun onStart() {
//        super.onStart()
//        mAuth = Firebase.auth
//        val user = mAuth.currentUser
//        if(user == null){
//            startActivity(Intent(this, LoginActivity::class.java))
//        }
//        else
//        {
//            startActivity(Intent(this, ProfileActivity::class.java))
//        }
//    }

    //this function use for switching between tabs of bottom navigation bar, belong to MainMenuActivity
    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }
}
