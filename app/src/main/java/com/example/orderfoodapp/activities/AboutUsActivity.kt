package com.example.orderfoodapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.orderfoodapp.R
import kotlinx.android.synthetic.main.activity_about_us.*

class AboutUsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        back_button.setOnClickListener() {
            finish()
        }
    }
}