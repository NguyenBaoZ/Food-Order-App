package com.example.orderfoodapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.orderfoodapp.fragments.FavouriteFragment
import com.example.orderfoodapp.fragments.MainMenuFragment
import com.example.orderfoodapp.fragments.MenuFragment
import com.example.orderfoodapp.fragments.ProfileFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main_menu.*


class MainMenuActivity : AppCompatActivity() {

    private val mainMenuFragment = MainMenuFragment()
    private val profileFragment = ProfileFragment()
    private val menuFragment = MenuFragment()
    private val favouriteFragment = FavouriteFragment()

    private var customerEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        customerEmail = Firebase.auth.currentUser?.email.toString()

        //this code is for switching between tabs of bottom navigation bar, NOT do functional task
        replaceFragment(mainMenuFragment)
        bottom_app_bar.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.navbottombar_profile -> {
                    replaceFragment(profileFragment)
                    shoppingCart_button.visibility = View.GONE
                }
                R.id.navbottombar_home -> {
                    replaceFragment(mainMenuFragment)
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
            val intent = Intent(Intent(this, CartActivity::class.java))
            startActivity(intent)
        }
    }

    //this function use for switching between tabs of bottom navigation bar, belong to MainMenuActivity
    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }
} 