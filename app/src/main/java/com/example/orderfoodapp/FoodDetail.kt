package com.example.orderfoodapp

import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_food_detail.*
import java.text.DecimalFormat

class FoodDetail : AppCompatActivity() {

    private lateinit var customerEmail: String

    private var priceS: Double = 0.0
    private var priceM: Double = 0.0
    private var priceL: Double = 0.0

    private val df = DecimalFormat("##.00")
    private var sizeChosen = "none"
    private var key = "none"
    private var subTotal = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_detail)

        customerEmail = Firebase.auth.currentUser?.email.toString()
        val curDish = intent.getParcelableExtra<Dish>("curDish")

        if(curDish != null) {
            Picasso.get().load(curDish.image).into(food_image)
            food_text.text = curDish.name
            rates_text.text = curDish.rated.toString()
            time_text.text = curDish.deliveryTime
            description_textView.text = curDish.description
            price_value.text = "0.00"

            val saleOff = curDish.salePercent.toInt()

            val saleS = curDish.priceS*(saleOff*1.0/100)
            val saleM = curDish.priceM*(saleOff*1.0/100)
            val saleL = curDish.priceL*(saleOff*1.0/100)

            priceS = curDish.priceS - saleS
            priceM = curDish.priceM - saleM
            priceL = curDish.priceL - saleL
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
            displayPrice()
        }

        image_m_size.setOnClickListener() {
            resetButton()

            image_m_size.setBackgroundResource(R.drawable.rounded_button_clicked)
            m_size_text.setTextColor(Color.BLACK)
            m_size_text.typeface = Typeface.DEFAULT_BOLD

            sizeChosen = "M"
            displayPrice()
        }

        image_l_size.setOnClickListener() {
            resetButton()

            image_l_size.setBackgroundResource(R.drawable.rounded_button_clicked)
            l_size_text.setTextColor(Color.BLACK)
            l_size_text.typeface = Typeface.DEFAULT_BOLD

            sizeChosen = "L"
            displayPrice()
        }

        image_increase_amount.setOnClickListener() {
            var amount = amount_text.text.toString().toInt()
            amount++
            amount_text.text = amount.toString()
            displayPrice()
        }

        image_decrease_amount.setOnClickListener() {
            var amount = amount_text.text.toString().toInt()
            if(amount > 1) {
                amount--
                amount_text.text = amount.toString()
            }
            displayPrice()
        }

        //find the pending bill of this customer
        findPendingBill()

        addToCart_button.setOnClickListener() {
            if(key != "none") {
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
                    if((data.child("customerEmail").value)?.equals(customerEmail) == true &&
                        data.child("status").value?.equals("pending") == true) {
                        key = data.key.toString()
                        subTotal = data.child("subTotal").value.toString().toDouble()
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun pushItemToPendingBill(curDish: Dish) {
        var isAdded = false
        val curSize = sizeChosen
        val unitPrice = price_value.text.toString().toDouble()

        val dbPush = FirebaseDatabase.getInstance().getReference("Bill/$key/products")
        dbPush.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    if(data.child("id").value as String == curDish.id &&
                       data.child("size").value as String == curSize) {
                        isAdded = true

                        val curAmount = data.child("amount").value as Long

                        val a: Any = data.child("unitPrice").value as Any
                        val type = a::class.simpleName
                        var curPrice = 0.0
                        if(type == "Long" || type == "Double")
                            curPrice = a.toString().toDouble()

                        val newAmount = curAmount + 1
                        val newPrice = curPrice + unitPrice

                        dbPush.child("${data.key}/amount").setValue(newAmount)
                        dbPush.child("${data.key}/unitPrice").setValue(df.format(newPrice).toDouble())
                    }
                }

                if(!isAdded) {
                    val item = PushBillItem(
                        amount_text.text.toString().toLong(),
                        curDish.id,
                        curDish.image,
                        curDish.name,
                        sizeChosen,
                        price_value.text.toString().toDouble()
                    )
                    dbPush.push().setValue(item)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        val dbUpdate = FirebaseDatabase.getInstance().getReference("Bill/$key")
        val newTotal = subTotal + unitPrice
        dbUpdate.child("subTotal").setValue(df.format(newTotal).toDouble())
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

    private fun displayPrice() {
        when(sizeChosen) {
            "S" -> price_value.text = df.format(priceS * amount_text.text.toString().toInt())
            "M" -> price_value.text = df.format(priceM * amount_text.text.toString().toInt())
            "L" -> price_value.text = df.format(priceL * amount_text.text.toString().toInt())
        }
    }
}