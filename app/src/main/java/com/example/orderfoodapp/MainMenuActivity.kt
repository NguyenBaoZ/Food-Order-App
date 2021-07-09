package com.example.orderfoodapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.orderfoodapp.fragments.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main_menu.*


class MainMenuActivity : AppCompatActivity() {

    private val mainMenuFragment = MainMenuFragment()
    private val profileFragment = ProfileFragment()
    private val chatFragment = ChatFragment()
    private val favouriteFragment = FavouriteFragment()

    private var curFragment: Fragment = mainMenuFragment

    private var customerEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        customerEmail = Firebase.auth.currentUser?.email.toString()

        checkFullFillInformation()

        //assign main menu fragment at the beginning
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, mainMenuFragment)
        transaction.commit()

        //this code is for switching between tabs of bottom navigation bar, NOT do functional task
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
                    replaceFragment(chatFragment)
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

    private fun checkFullFillInformation() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Customer")
        val query = dbRef.orderByChild("email").equalTo(customerEmail)
        query.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    for(data in snapshot.children) {
                        val name = data.child("fullName").value as String
                        val address = data.child("address").value as String
                        val phoneNumber = data.child("phoneNumber").value as String
                        val gender = data.child("gender").value as String
                        val dateOfBirth = data.child("dateOfBirth").value as String

                        if(name.isEmpty() || address.isEmpty() || phoneNumber.isEmpty() || gender.isEmpty() || dateOfBirth.isEmpty()) {
                            startActivity(Intent(this@MainMenuActivity, FillInformationActivity::class.java))
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    //this function use for switching between tabs of bottom navigation bar, saving it's state
    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()

        //set type of animation base on source and destination fragment
        if(curFragment == mainMenuFragment) {
            transaction.setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left)
        }
        else if(curFragment == profileFragment) {
            transaction.setCustomAnimations(R.anim.enter_left_to_right, R.anim.exit_left_to_right)
        }
        else if(curFragment == favouriteFragment) {
            if(fragment == mainMenuFragment)
                transaction.setCustomAnimations(R.anim.enter_left_to_right, R.anim.exit_left_to_right)
            else
                transaction.setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left)
        }
        else if(curFragment == chatFragment) {
            if(fragment == profileFragment)
                transaction.setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left)
            else
                transaction.setCustomAnimations(R.anim.enter_left_to_right, R.anim.exit_left_to_right)
        }

        transaction.hide(curFragment)

        if(fragment.isAdded)
            transaction.show(fragment)
        else
            transaction.add(R.id.fragment_container, fragment)

        transaction.commit()
        curFragment = fragment
    }
} 