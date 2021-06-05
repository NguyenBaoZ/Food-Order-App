package com.example.orderfoodapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.activity_cart.back_button

class CartActivity : AppCompatActivity() {

    //customerEmail will be generated automatically when they sign up or log in
    //when the app complete, customerEmail will be passed from the previous activities
    private lateinit var customerID: String

    private lateinit var cartItemAdapter: CartItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        customerID = intent.getStringExtra("customerID").toString()

        cartItemAdapter = CartItemAdapter(mutableListOf())
        cart_recyclerView.adapter = cartItemAdapter

        val layoutManager = LinearLayoutManager(this)
        cart_recyclerView.layoutManager = layoutManager

        findKey()

        back_button.setOnClickListener() {
            finish()
        }

        continue_button.setOnClickListener() {
            startActivity(Intent(this, CheckoutActivity::class.java))
        }

    }


    private fun findKey() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Bill")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    if((data.child("customer").value)?.equals(customerID) == true &&
                        data.child("status").value?.equals("pending") == true) {

                        subTotal_textView.text = (data.child("total").value as Double).toString()
                        loadCartData(data.key.toString())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun loadCartData(key: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Bill/$key/products")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cartItemAdapter.deleteAll()
                for(data in snapshot.children) {
                    val item = CartItem(
                        data.child("image").value as String,
                        data.child("name").value as String,
                        data.child("amount").value as Long,
                        data.child("unitPrice").value as Double,
                    )
                    cartItemAdapter.addCartItem(item)
                }
                numItem_textView.text = cartItemAdapter.itemCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

}