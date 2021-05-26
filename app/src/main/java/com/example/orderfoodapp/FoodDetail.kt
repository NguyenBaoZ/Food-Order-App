package com.example.orderfoodapp

import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_food_detail.*

class FoodDetail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_detail)

        back_button.setOnClickListener() {
            finish()
        }

        image_s_size.setOnClickListener() {
            resetButton()
            image_s_size.setBackgroundResource(R.drawable.rounded_button_clicked)
            s_size_text.setTextColor(Color.BLACK)
            s_size_text.typeface = Typeface.DEFAULT_BOLD
        }

        image_m_size.setOnClickListener() {
            resetButton()
            image_m_size.setBackgroundResource(R.drawable.rounded_button_clicked)
            m_size_text.setTextColor(Color.BLACK)
            m_size_text.typeface = Typeface.DEFAULT_BOLD
        }

        image_l_size.setOnClickListener() {
            resetButton()
            image_l_size.setBackgroundResource(R.drawable.rounded_button_clicked)
            l_size_text.setTextColor(Color.BLACK)
            l_size_text.typeface = Typeface.DEFAULT_BOLD
        }

        image_increase_amount.setOnClickListener() {
            var amount = amount_text.text.toString().toInt()
            amount++
            amount_text.text = amount.toString()
        }

        image_decrease_amount.setOnClickListener() {
            var amount = amount_text.text.toString().toInt()
            if(amount > 1) {
                amount--
                amount_text.text = amount.toString()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val curDish = intent.getParcelableExtra<Dish>("curDish")

        if(curDish != null) {
            food_image.setImageResource(curDish.dishImage)
            food_text.text = curDish.dishName
            rates_text.text = curDish.dishRating
            time_text.text = curDish.deliveryTime
        }
    }

    fun resetButton() {
        image_s_size.setBackgroundResource(R.drawable.rounded_button)
        image_m_size.setBackgroundResource(R.drawable.rounded_button)
        image_l_size.setBackgroundResource(R.drawable.rounded_button)

        s_size_text.setTextColor(Color.GRAY)
        m_size_text.setTextColor(Color.GRAY)
        l_size_text.setTextColor(Color.GRAY)

        s_size_text.typeface = Typeface.DEFAULT
        m_size_text.typeface = Typeface.DEFAULT
        l_size_text.typeface = Typeface.DEFAULT
    }
}