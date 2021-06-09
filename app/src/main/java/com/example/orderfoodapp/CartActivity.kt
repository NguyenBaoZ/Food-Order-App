package com.example.orderfoodapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.activity_cart.back_button

class CartActivity : AppCompatActivity() {

    private lateinit var customerEmail: String

    private lateinit var cartItemAdapter: CartItemAdapter
    private var key = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        customerEmail = Firebase.auth.currentUser?.email.toString()

        cartItemAdapter = CartItemAdapter(mutableListOf())
        cart_recyclerView.adapter = cartItemAdapter

        val layoutManager = LinearLayoutManager(this)
        cart_recyclerView.layoutManager = layoutManager

        findKey()

        back_button.setOnClickListener() {
            finish()
        }

        continue_button.setOnClickListener() {
            val intent = Intent(this, CheckoutActivity::class.java)
            intent.putExtra("key", key)
            startActivity(intent)
            finish()
        }

    }


    private fun findKey() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Bill")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    if((data.child("customerEmail").value)?.equals(customerEmail) == true &&
                        data.child("status").value?.equals("pending") == true) {

                        key = data.key.toString()

                        val a: Any = data.child("subTotal").value as Any
                        val type = a::class.simpleName
                        var subTotal = 0.0
                        if(type == "Long" || type == "Double")
                            subTotal = a.toString().toDouble()

                        subTotal_textView.text = subTotal.toString()
                        loadCartData()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun loadCartData() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Bill/$key/products")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cartItemAdapter.deleteAll()
                for(data in snapshot.children) {
                    val a: Any = data.child("unitPrice").value as Any
                    val type = a::class.simpleName
                    var price = 0.0
                    if(type == "Long" || type == "Double")
                        price = a.toString().toDouble()

                    val item = CartItem(
                        data.child("id").value as String,
                        data.child("size").value as String,
                        data.child("image").value as String,
                        data.child("name").value as String,
                        data.child("amount").value as Long,
                        price,
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


