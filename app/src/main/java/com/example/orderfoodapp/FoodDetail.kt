package com.example.orderfoodapp

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_food_detail.*
import kotlinx.android.synthetic.main.fragment_filter_all_food.*
import kotlinx.android.synthetic.main.fragment_main_menu.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class FoodDetail : AppCompatActivity() {

    private lateinit var customerEmail: String

    private var priceS: Double = 0.0
    private var priceM: Double = 0.0
    private var priceL: Double = 0.0

    private val df = DecimalFormat("##.00")
    private var sizeChosen = "none"
    private var keyBill = "none"
    private var subTotal = 0.0

    private var keyFav = "none"
    private var isFav = false

    private var rating: Long = 5
    private lateinit var commentAdapter: CommentAdapter

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

        //check if this dish is in favourite list or not and display corresponding icon
        val dbRef = FirebaseDatabase.getInstance().getReference("Favourite")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    if((data.child("customerEmail").value)?.equals(customerEmail) == true) {
                        keyFav = data.key.toString()
                        val dbRef2 = FirebaseDatabase.getInstance().getReference("Favourite/$keyFav/products")
                        dbRef2.get().addOnSuccessListener {
                            for(data2 in it.children) {
                                if(data2.value as String == curDish!!.id) {
                                    ic_heart.setImageResource(R.drawable.ic_heart_fill)
                                    isFav = true
                                    break
                                }
                            }
                        }
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FoodDetail, "Cannot check favourite or not!", Toast.LENGTH_LONG).show()
            }

        })


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
            if(sizeChosen != "none") {
                if(keyBill != "none") {
                    pushItemToPendingBill(curDish!!)
                }
                else {
                    createNewBill()
                    pushItemToPendingBill(curDish!!)
                }
                showDialog()
            }
            else {
                Toast.makeText(this, "Please choose a food size!", Toast.LENGTH_LONG).show()
            }
        }

        buyNow_button.setOnClickListener() {
            createNewBillBuyNow(curDish!!)
            val intent = Intent(this, CheckoutActivity::class.java)
            val bundle = Bundle()
            bundle.putString("key", keyBill)
            bundle.putBoolean("isBuyNow", true)
            intent.putExtras(bundle)
            startActivity(intent)
            finish()
        }

        ic_heart.setOnClickListener() {
            if(isFav){
                deleteFav(curDish!!)
                ic_heart.setImageResource(R.drawable.ic_heart)
                isFav = false
            }
            else {
                addFav(curDish!!)
                ic_heart.setImageResource(R.drawable.ic_heart_fill)
                isFav = true
            }
        }

        commentAdapter = CommentAdapter(mutableListOf())
        comment_recyclerView.adapter = commentAdapter

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        comment_recyclerView.layoutManager = layoutManager

        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        comment_recyclerView.addItemDecoration(itemDecoration)

        loadComment(curDish!!)
        checkBuyOrNot(curDish!!)

        star1_image.setOnClickListener() {
            onStarClick(it)
        }

        star2_image.setOnClickListener() {
            onStarClick(it)
        }

        star3_image.setOnClickListener() {
            onStarClick(it)
        }

        star4_image.setOnClickListener() {
            onStarClick(it)
        }

        star5_image.setOnClickListener() {
            onStarClick(it)
        }

        send_button.setOnClickListener() {
            onCommentClick(curDish!!)
        }
    }

    private fun addFav(curDish: Dish) {
        if(keyFav == "none") {
            val dbRef = FirebaseDatabase.getInstance().getReference("Favourite")
            keyFav = dbRef.push().key.toString()
            val hashMap = HashMap<String, String>()
            hashMap["customerEmail"] = customerEmail
            dbRef.child(keyFav).setValue(hashMap)

        }
        val dbRef2 = FirebaseDatabase.getInstance().getReference("Favourite/$keyFav/products")
        val keyPush = dbRef2.push().key.toString()
        dbRef2.child(keyPush).setValue(curDish.id)
        Toast.makeText(this, "Add to Favourite successfully!", Toast.LENGTH_LONG).show()
    }

    private fun deleteFav(curDish: Dish) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Favourite/$keyFav/products")
        dbRef.get().addOnSuccessListener {
            for(data in it.children) {
                if(data.value as String == curDish.id) {
                    data.ref.removeValue()
                    Toast.makeText(this, "Remove successfully!", Toast.LENGTH_LONG).show()
                }
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
                        keyBill = data.key.toString()
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
        val unitPrice = convertToDoubleFormat(price_value.text.toString())

        val dbPush = FirebaseDatabase.getInstance().getReference("Bill/$keyBill/products")
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
                        unitPrice
                    )
                    dbPush.push().setValue(item)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        val dbUpdate = FirebaseDatabase.getInstance().getReference("Bill/$keyBill")
        val newTotal = subTotal + unitPrice
        dbUpdate.child("subTotal").setValue(convertToDoubleFormat(df.format(newTotal)))
    }

    private fun createNewBill() {
        val newBill = CreateBillItem(
            customerEmail,
            "pending",
            0.0
        )
        val dbCreate = FirebaseDatabase.getInstance().getReference("Bill")
        keyBill = dbCreate.push().key.toString()
        dbCreate.child(keyBill).setValue(newBill)
    }

    private fun createNewBillBuyNow(curDish: Dish) {
        val price = convertToDoubleFormat(price_value.text.toString())

        val newBill = CreateBillItem(
            customerEmail,
            "pending",
            price
        )

        //create new bill with pending status
        val dbCreate = FirebaseDatabase.getInstance().getReference("Bill")
        keyBill = dbCreate.push().key.toString()
        dbCreate.child(keyBill).setValue(newBill)

        //push current dish into new bill above
        val dbPushProduct = FirebaseDatabase.getInstance().getReference("Bill/$keyBill/products")
        val item = PushBillItem(
            amount_text.text.toString().toLong(),
            curDish.id,
            curDish.image,
            curDish.name,
            sizeChosen,
            price
        )
        dbPushProduct.push().setValue(item)
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

    private fun showDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.add_to_cart_success_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val openCart_button = dialog.findViewById<Button>(R.id.openCart_button)
        openCart_button.setOnClickListener() {
            dialog.dismiss()
            finish()
            val intent = Intent(Intent(this, CartActivity::class.java))
            startActivity(intent)
        }

        val backToHome_button = dialog.findViewById<Button>(R.id.backToHome_button)
        backToHome_button.setOnClickListener() {
            dialog.dismiss()
            finish()
        }
        dialog.show()
    }

    private fun convertToDoubleFormat(str: String): Double {
        var strNum = str
        return if(strNum.contains(",")) {
            strNum = strNum.replace(",", ".")
            strNum.toDouble()
        } else
            strNum.toDouble()
    }

    private fun checkBuyOrNot(curDish: Dish) {
        var isBought = false
        val dbRef = FirebaseDatabase.getInstance().getReference("Bill")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    if ((data.child("customerEmail").value)?.equals(customerEmail) == true
                        && data.child("status").value?.equals("done") == true) {

                        val dbRef2 = FirebaseDatabase.getInstance().getReference("Bill/${data.key}/products")
                        dbRef2.get().addOnSuccessListener {
                            for(data2 in it.children) {
                                if(data2.child("id").value as String == curDish.id) {
                                    isBought = true
                                    break
                                }
                            }
                        }

                    }
                }

                if(isBought)
                    showComment()
                else
                    hideComment()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun showComment() {
        leaveRating_textView.visibility = View.VISIBLE
        star1_image.visibility = View.VISIBLE
        star2_image.visibility = View.VISIBLE
        star3_image.visibility = View.VISIBLE
        star4_image.visibility = View.VISIBLE
        star5_image.visibility = View.VISIBLE
        circleImageView.visibility = View.VISIBLE
        comment_editText.visibility = View.VISIBLE
        send_button.visibility = View.VISIBLE
    }

    private fun hideComment() {
        leaveRating_textView.visibility = View.GONE
        star1_image.visibility = View.GONE
        star2_image.visibility = View.GONE
        star3_image.visibility = View.GONE
        star4_image.visibility = View.GONE
        star5_image.visibility = View.GONE
        circleImageView.visibility = View.GONE
        comment_editText.visibility = View.GONE
        send_button.visibility = View.GONE
        setMargins(comment_textView, 100, 0, 0, 0)
    }

    private fun setMargins(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        if (view.layoutParams is MarginLayoutParams) {
            val p = view.layoutParams as MarginLayoutParams
            p.setMargins(left, top, right, bottom)
            view.requestLayout()
        }
    }

    private fun onStarClick(view: View) {
        when(view.id) {
            R.id.star1_image -> {
                rating = 1
                star1_image.setImageResource(R.drawable.ic_big_star)
                star2_image.setImageResource(R.drawable.ic_big_empty_star)
                star3_image.setImageResource(R.drawable.ic_big_empty_star)
                star4_image.setImageResource(R.drawable.ic_big_empty_star)
                star5_image.setImageResource(R.drawable.ic_big_empty_star)
            }

            R.id.star2_image -> {
                rating = 2
                star1_image.setImageResource(R.drawable.ic_big_star)
                star2_image.setImageResource(R.drawable.ic_big_star)
                star3_image.setImageResource(R.drawable.ic_big_empty_star)
                star4_image.setImageResource(R.drawable.ic_big_empty_star)
                star5_image.setImageResource(R.drawable.ic_big_empty_star)
            }

            R.id.star3_image -> {
                rating = 3
                star1_image.setImageResource(R.drawable.ic_big_star)
                star2_image.setImageResource(R.drawable.ic_big_star)
                star3_image.setImageResource(R.drawable.ic_big_star)
                star4_image.setImageResource(R.drawable.ic_big_empty_star)
                star5_image.setImageResource(R.drawable.ic_big_empty_star)
            }

            R.id.star4_image -> {
                rating = 4
                star1_image.setImageResource(R.drawable.ic_big_star)
                star2_image.setImageResource(R.drawable.ic_big_star)
                star3_image.setImageResource(R.drawable.ic_big_star)
                star4_image.setImageResource(R.drawable.ic_big_star)
                star5_image.setImageResource(R.drawable.ic_big_empty_star)
            }

            R.id.star5_image -> {
                rating = 5
                star1_image.setImageResource(R.drawable.ic_big_star)
                star2_image.setImageResource(R.drawable.ic_big_star)
                star3_image.setImageResource(R.drawable.ic_big_star)
                star4_image.setImageResource(R.drawable.ic_big_star)
                star5_image.setImageResource(R.drawable.ic_big_star)
            }
        }
    }

    private fun onCommentClick(curDish: Dish) {
        var isUpLoaded = false
        val dbRef = FirebaseDatabase.getInstance().getReference("Comment/${curDish.id}")
        dbRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    if(data.child("customerEmail").value as String == customerEmail) {
                        val dbRemove = FirebaseDatabase.getInstance().getReference("Comment/${curDish.id}/${data.key}")
                        dbRemove.ref.removeValue()
                        pushComment(curDish)
                        isUpLoaded = true
                    }
                }
                if(!isUpLoaded)
                    pushComment(curDish)
                hideComment()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FoodDetail, "Cannot upload comments!", Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun pushComment(curDish: Dish) {
        val sdf = SimpleDateFormat("HH:mm dd/MM/yyyy")
        val content = comment_editText.text.toString()
        val dishID = curDish.id
        val time = sdf.format(Calendar.getInstance().time)
        val comment = CommentItem(
            customerEmail,
            rating,
            content,
            time
        )

        val dbRef = FirebaseDatabase.getInstance().getReference("Comment/$dishID")
        dbRef.push().setValue(comment)
        Toast.makeText(this@FoodDetail, "Thanks for your feedback!", Toast.LENGTH_LONG).show()
    }

    private fun loadComment(curDish: Dish) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Comment/${curDish.id}")
        dbRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentAdapter.deleteAll()
                for(data in snapshot.children) {
                    val comment = CommentItem (
                        data.child("customerEmail").value as String,
                        data.child("rating").value as Long,
                        data.child("comment").value as String,
                        data.child("time").value as String
                    )
                    commentAdapter.addComment(comment)
                }

                if(commentAdapter.itemCount == 0)
                    comment_textView.text = "No comments yet"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FoodDetail, "Cannot load comments!", Toast.LENGTH_LONG).show()
            }

        })
    }

}