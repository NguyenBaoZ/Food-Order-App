package com.example.orderfoodapp.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.orderfoodapp.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {

    private var key = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onResume() {
        super.onResume()

        displayCustomer()

        order_history_text.setOnClickListener() {
            startActivity(Intent(context, OrderHistoryActivity::class.java))
        }

        profile_text.setOnClickListener() {
            val intent = Intent(context, EditProfileActivity::class.java)
            intent.putExtra("key", key)
            startActivity(intent)
        }

        payment_method_text.setOnClickListener() {
            startActivity(Intent(context, PaymentMethod::class.java))
        }

        about_us_text.setOnClickListener() {
            startActivity(Intent(context, AboutUs::class.java))
        }
    }

    private fun displayCustomer() {
        val customerEmail = Firebase.auth.currentUser?.email.toString()
        profile_mail.text = customerEmail
        val dbRef = FirebaseDatabase.getInstance().getReference("Customer")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    if(data.child("email").value as String == customerEmail) {
                        key = data.key.toString()
                        profile_name.text = data.child("fullName").value as String
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Cannot load customer's data, please try later!", Toast.LENGTH_LONG).show()
            }

        })
    }
}