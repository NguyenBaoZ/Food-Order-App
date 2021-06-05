package com.example.orderfoodapp

import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_food_detail.*
import java.text.DecimalFormat
import kotlin.math.roundToInt

class FoodDetail : AppCompatActivity() {

    //customerEmail will be generated automatically when they sign up or log in
    //when the app complete, customerEmail will be passed from the previous activities
    private val customerEmail = "khoavo617@gmail"

    private lateinit var priceS: String
    private lateinit var priceM: String
    private lateinit var priceL: String

    private var sizeChosen = "none"
    private var key = "none"
    private var total = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_detail)

        val curDish = intent.getParcelableExtra<Dish>("curDish")

        if(curDish != null) {
            Picasso.get().load(curDish.image).into(food_image)
            food_text.text = curDish.name
            rates_text.text = curDish.rated.toString()
            time_text.text = curDish.deliveryTime
            description_textView.text = curDish.description
            price_value.text = "0.00"

            priceS = curDish.priceS.toString()
            priceM = curDish.priceM.toString()
            priceL = curDish.priceL.toString()
        }

        back_button.setOnClickListener() {
            finish()
        }

        image_s_size.setOnClickListener() {
            resetButton()

            image_s_size.setBackgroundResource(R.drawable.rounded_button_clicked)
            s_size_text.setTextColor(Color.BLACK)
            s_size_text.typeface = Typeface.DEFAULT_BOLD

            sizeChosen = "S"
            displayPrice(curDish!!)
        }

        image_m_size.setOnClickListener() {
            resetButton()

            image_m_size.setBackgroundResource(R.drawable.rounded_button_clicked)
            m_size_text.setTextColor(Color.BLACK)
            m_size_text.typeface = Typeface.DEFAULT_BOLD

            sizeChosen = "M"
            displayPrice(curDish!!)
        }

        image_l_size.setOnClickListener() {
            resetButton()

            image_l_size.setBackgroundResource(R.drawable.rounded_button_clicked)
            l_size_text.setTextColor(Color.BLACK)
            l_size_text.typeface = Typeface.DEFAULT_BOLD

            sizeChosen = "L"
            displayPrice(curDish!!)
        }

        image_increase_amount.setOnClickListener() {
            var amount = amount_text.text.toString().toInt()
            amount++
            amount_text.text = amount.toString()
            displayPrice(curDish!!)
        }

        image_decrease_amount.setOnClickListener() {
            var amount = amount_text.text.toString().toInt()
            if(amount > 1) {
                amount--
                amount_text.text = amount.toString()
            }
            displayPrice(curDish!!)
        }

        //find the pending bill of this customer
        findPendingBill()
        Log.i("msg1", key)

        addToCart_button.setOnClickListener() {
            if(key != "none") {
                Log.i("msg2", key)
                pushItemToPendingBill(curDish!!)
            }
            else {
                createNewBill()
                pushItemToPendingBill(curDish!!)
            }
        }
    }

    private fun findPendingBill() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Bill")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    if((data.child("customer").value)?.equals(customerEmail) == true &&
                        data.child("status").value?.equals("pending") == true) {
                        key = data.key.toString()
                        Log.i("msg3", key)
                        total = data.child("total").value.toString().toDouble()
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun pushItemToPendingBill(curDish: Dish) {
        val item = PushBillItem(
            amount_text.text.toString().toLong(),
            curDish.id,
            curDish.image,
            curDish.name,
            sizeChosen,
            price_value.text.toString().toDouble()
        )
        val dbPush = FirebaseDatabase.getInstance().getReference("Bill/$key/products")
        dbPush.push().setValue(item)

        val dbUpdate = FirebaseDatabase.getInstance().getReference("Bill/$key")
        val newTotal = total + price_value.text.toString().toDouble()
        dbUpdate.child("total").setValue(newTotal)
    }

    private fun createNewBill() {
        val newBill = CreateBillItem(
            customerEmail,
            "pending",
            0.0
        )
        val dbCreate = FirebaseDatabase.getInstance().getReference("Bill")
        key = dbCreate.push().key.toString()
        dbCreate.child(key).setValue(newBill)
    }

    private fun resetButton() {
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

    private fun displayPrice(curDish: Dish) {
        val df = DecimalFormat("##.00")
        when(sizeChosen) {
            "S" -> price_value.text = df.format(curDish.priceS * amount_text.text.toString().toInt())
            "M" -> price_value.text = df.format(curDish.priceM * amount_text.text.toString().toInt())
            "L" -> price_value.text = df.format(curDish.priceL * amount_text.text.toString().toInt())
        }
    }
}