package com.example.orderfoodapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_checkout.*

class CheckoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        back_button.setOnClickListener() {
            finish()
        }
//
//        standard_button.setOnClickListener {
//            delivery_group.clearCheck()
//        }
//
//        premium_button.setOnClickListener {
//            delivery_group.clearCheck()
//        }
//
//        cod_button.setOnClickListener {
//            payment_group.clearCheck()
//        }
//
//        myWallet_button.setOnClickListener {
//            payment_group.clearCheck()
//        }
    }
}