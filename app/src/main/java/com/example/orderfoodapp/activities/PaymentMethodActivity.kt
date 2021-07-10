package com.example.orderfoodapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.orderfoodapp.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_payment_method.*

class PaymentMethodActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_method)

        showCurrentBalance()

        back_button.setOnClickListener() {
            finish()
        }
    }

    private fun showCurrentBalance() {
        val customerEmail = Firebase.auth.currentUser?.email.toString()
        val dbRef = FirebaseDatabase.getInstance().getReference("Customer")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    if(data.child("email").value as String == customerEmail) {
                        val a: Any = data.child("balance").value as Any
                        val type = a::class.simpleName
                        var curBalance = 0.0
                        if(type == "Long" || type == "Double")
                            curBalance = a.toString().toDouble()

                        current_balance_value.text = "$ $curBalance"

                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}