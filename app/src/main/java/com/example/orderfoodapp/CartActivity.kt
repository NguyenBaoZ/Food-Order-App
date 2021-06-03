package com.example.orderfoodapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.activity_cart.back_button
import kotlinx.android.synthetic.main.activity_checkout.*

class CartActivity : AppCompatActivity() {

    private lateinit var cartItemAdapter: CartItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        cartItemAdapter = CartItemAdapter(mutableListOf())
        cart_recyclerView.adapter = cartItemAdapter

        val layoutManager = LinearLayoutManager(this)
        cart_recyclerView.layoutManager = layoutManager

        //fake data
        val dish1 = CartItem(R.drawable.img_hamburger,"Hamburger",1,7.68)
        val dish2 = CartItem(R.drawable.img_alaska_lobster,"Alaska Lobster",2,7.68)
        val dish3 = CartItem(R.drawable.img_pizza,"Pizza",1,7.68)
        val dish4 = CartItem(R.drawable.img_peach_tea,"Peach tea",3,7.68)
        val dish5 = CartItem(R.drawable.img_shushi,"Shushi",1,7.68)

        cartItemAdapter.addCartItem(dish1)
        cartItemAdapter.addCartItem(dish2)
        cartItemAdapter.addCartItem(dish3)
        cartItemAdapter.addCartItem(dish4)
        cartItemAdapter.addCartItem(dish5)

        back_button.setOnClickListener() {
            finish()
        }

        continue_button.setOnClickListener() {
            startActivity(Intent(this, CheckoutActivity::class.java))
        }
    }
}