package com.example.orderfoodapp

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore.Images.Media.insertImage
import android.transition.Fade
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.r0adkll.slidr.Slidr
import kotlinx.android.synthetic.main.activity_food_detail.*
import kotlinx.android.synthetic.main.dish_item.view.*
import kotlinx.android.synthetic.main.fragment_filter_all_food.*
import kotlinx.android.synthetic.main.fragment_main_menu.*
import java.io.File
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
    private lateinit var sameProviderAdapter: DishAdapter
    private lateinit var sameCategoryAdapter: DishAdapter

    lateinit var bitmap: Bitmap

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_detail)

        val fade = Fade()
        window.enterTransition = fade
        window.exitTransition = fade

        Slidr.attach(this)

        customerEmail = Firebase.auth.currentUser?.email.toString()
        val curDish = intent.getParcelableExtra<Dish>("curDish")

        if(curDish != null) {
            val storageRef = FirebaseStorage.getInstance().getReference("dish_image/${curDish.id}.jpg")
            try {
                val localFile = File.createTempFile("tempfile", ".jpg")
                storageRef.getFile(localFile).addOnSuccessListener {
                    bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                    food_image.setImageBitmap(bitmap)
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
            }

            food_text.text = curDish.name
            rates_text.text = curDish.rated
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
            showCurAmount(curDish!!, it)

            image_s_size.setBackgroundResource(R.drawable.rounded_button_clicked)
            s_size_text.setTextColor(Color.BLACK)
            s_size_text.typeface = Typeface.DEFAULT_BOLD

            sizeChosen = "S"
        }

        image_m_size.setOnClickListener() {
            resetButton()
            showCurAmount(curDish!!, it)

            image_m_size.setBackgroundResource(R.drawable.rounded_button_clicked)
            m_size_text.setTextColor(Color.BLACK)
            m_size_text.typeface = Typeface.DEFAULT_BOLD

            sizeChosen = "M"
        }

        image_l_size.setOnClickListener() {
            resetButton()
            showCurAmount(curDish!!, it)

            image_l_size.setBackgroundResource(R.drawable.rounded_button_clicked)
            l_size_text.setTextColor(Color.BLACK)
            l_size_text.typeface = Typeface.DEFAULT_BOLD

            sizeChosen = "L"
        }

        image_increase_amount.setOnClickListener() {
            var amount = amount_text.text.toString().toInt()
            if(amount < number_text.text.toString().toInt()) {
                amount++
                amount_text.text = amount.toString()
                displayPrice()
            }
            else {
                Toast.makeText(this@FoodDetail, "Maximum", Toast.LENGTH_LONG).show()
            }
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
            if(sizeChosen != "none" && amount_text.text.isNotEmpty()) {
                if(keyBill != "none") {
                    pushItemToPendingBill(curDish!!)
                }
                else {
                    createNewBill()
                    pushItemToPendingBill(curDish!!)
                }
                showDialog()
            }
            else if(amount_text.text.isEmpty()) {
                Toast.makeText(this, "Sold out!", Toast.LENGTH_LONG).show()
            }
            else {
                Toast.makeText(this, "Please choose a food size!", Toast.LENGTH_LONG).show()
            }
        }

        buyNow_button.setOnClickListener() {
            if(amount_text.text.isNotEmpty()) {
                createNewBillBuyNow(curDish!!)
                val intent = Intent(this, CheckoutActivity::class.java)
                val bundle = Bundle()
                bundle.putString("key", keyBill)
                bundle.putBoolean("isBuyNow", true)
                intent.putExtras(bundle)
                startActivity(intent)
                finish()
            }
            else {
                Toast.makeText(this, "Sold out!", Toast.LENGTH_LONG).show()
            }
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

        sameProviderAdapter = DishAdapter(mutableListOf())
        sameProvider_recyclerView.adapter = sameProviderAdapter

        sameCategoryAdapter = DishAdapter(mutableListOf())
        sameCategory_recyclerView.adapter = sameCategoryAdapter

        val layoutManager1 = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        comment_recyclerView.layoutManager = layoutManager1
        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        comment_recyclerView.addItemDecoration(itemDecoration)

        val layoutManager2 = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        sameProvider_recyclerView.layoutManager = layoutManager2

        val layoutManager3 = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        sameCategory_recyclerView.layoutManager = layoutManager3

        hideComment()
        loadComment(curDish!!)
        checkBuyOrNot(curDish)
        loadRecommended(curDish)

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
            onCommentClick(curDish)
        }

        ic_chat.setOnClickListener() {
            val dbGetMail = FirebaseDatabase.getInstance().getReference("Provider/${curDish.provider}/email")
            dbGetMail.get().addOnSuccessListener {
                val providerEmail = it.value as String
                val intent = Intent(this, MessageChatActivity::class.java)
                intent.putExtra("providerEmail", providerEmail)
                startActivity(intent)
            }
        }

        ic_share.setOnClickListener() {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND

            val path = insertImage(contentResolver, bitmap, "Food", "I want to share this amazing food to everyoneee")
            val uri = Uri.parse(path)

            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.type = "image/*"
            startActivity(Intent.createChooser(intent, "Share this food to: "))
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

    private fun showCurAmount(curDish: Dish, view: View) {
        numLeft_text.text = "Num left: "
        val dbRef = FirebaseDatabase.getInstance().getReference("Product/${curDish.id}")
        dbRef.get().addOnSuccessListener {
            when(view.id) {
                R.id.image_s_size -> {
                    val curAmount = it.child("amountS").value as Long
                    if(curAmount == 0L) {
                        image_s_size.setBackgroundResource(R.drawable.rounded_button_soldout)
                        s_size_text.setTextColor(Color.WHITE)
                        numLeft_text.text = "Sold out!"
                        numLeft_text.setTextColor(Color.RED)
                        number_text.text = ""
                        amount_text.text = ""
                        image_increase_amount.isEnabled = false
                        image_decrease_amount.isEnabled = false
                        displayPriceDefault(curDish)
                    }
                    else {
                        numLeft_text.setTextColor(Color.parseColor("#009E0F"))
                        number_text.text = curAmount.toString()
                        number_text.setTextColor(Color.parseColor("#009E0F"))
                        amount_text.text = "1"
                        image_increase_amount.isEnabled = true
                        image_decrease_amount.isEnabled = true
                        displayPrice()
                    }
                }

                R.id.image_m_size -> {
                    val curAmount = it.child("amountM").value as Long
                    if(curAmount == 0L) {
                        image_m_size.setBackgroundResource(R.drawable.rounded_button_soldout)
                        m_size_text.setTextColor(Color.WHITE)
                        numLeft_text.text = "Sold out!"
                        numLeft_text.setTextColor(Color.RED)
                        number_text.text = ""
                        amount_text.text = ""
                        image_increase_amount.isEnabled = false
                        image_decrease_amount.isEnabled = false
                        displayPriceDefault(curDish)
                    }
                    else {
                        numLeft_text.setTextColor(Color.parseColor("#009E0F"))
                        number_text.text = curAmount.toString()
                        number_text.setTextColor(Color.parseColor("#009E0F"))
                        amount_text.text = "1"
                        image_increase_amount.isEnabled = true
                        image_decrease_amount.isEnabled = true
                        displayPrice()
                    }
                }

                R.id.image_l_size -> {
                    val curAmount = it.child("amountL").value as Long
                    if(curAmount == 0L) {
                        image_l_size.setBackgroundResource(R.drawable.rounded_button_soldout)
                        l_size_text.setTextColor(Color.WHITE)
                        numLeft_text.text = "Sold out!"
                        numLeft_text.setTextColor(Color.RED)
                        number_text.text = ""
                        amount_text.text = ""
                        image_increase_amount.isEnabled = false
                        image_decrease_amount.isEnabled = false
                        displayPriceDefault(curDish)
                    }
                    else {
                        numLeft_text.setTextColor(Color.parseColor("#009E0F"))
                        number_text.text = curAmount.toString()
                        number_text.setTextColor(Color.parseColor("#009E0F"))
                        amount_text.text = "1"
                        image_increase_amount.isEnabled = true
                        image_decrease_amount.isEnabled = true
                        displayPrice()
                    }
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

    private fun displayPriceDefault(curDish: Dish) {
        when(sizeChosen) {
            "S" -> price_value.text = curDish.priceS.toString()
            "M" -> price_value.text = curDish.priceM.toString()
            "L" -> price_value.text = curDish.priceL.toString()
        }
    }

    private fun showDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_to_cart_success)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val backToHome_button = dialog.findViewById<Button>(R.id.backToHome_button)
        backToHome_button.setOnClickListener() {
            dialog.dismiss()
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
                                    showComment()
                                    break
                                }
                            }
                        }

                    }
                }
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
        setMargins(comment_textView, 90, 450, 0, 0)
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
        setMargins(comment_textView, 90, 0, 0, 0)
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
                    comment_textView.text = "No comments yet!"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FoodDetail, "Cannot load comments!", Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun loadRecommended(curDish: Dish) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Product")
        dbRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                sameProviderAdapter.deleteAll()
                sameCategoryAdapter.deleteAll()

                for(data in snapshot.children) {

                    if(data.child("provider").value as String == curDish.provider
                        && data.child("id").value as String != curDish.id) {
                        val itemProvider = Dish(
                            data.child("id").value as String,
                            data.child("name").value as String,
                            data.child("priceS").value as Double,
                            data.child("priceM").value as Double,
                            data.child("priceL").value as Double,
                            data.child("rated").value as String,
                            curDish.deliveryTime,
                            data.child("category").value as String,
                            data.child("description").value as String,
                            data.child("salePercent").value as Long,
                            data.child("provider").value as String,
                            data.child("amountS").value as Long,
                            data.child("amountSsold").value as Long,
                            data.child("amountM").value as Long,
                            data.child("amountMsold").value as Long,
                            data.child("amountL").value as Long,
                            data.child("amountLsold").value as Long,
                        )
                        sameProviderAdapter.addDish(itemProvider)
                    }

                    if(data.child("category").value as String == curDish.category
                        && data.child("id").value as String != curDish.id) {
                        val itemProvider = Dish(
                            data.child("id").value as String,
                            data.child("name").value as String,
                            data.child("priceS").value as Double,
                            data.child("priceM").value as Double,
                            data.child("priceL").value as Double,
                            data.child("rated").value as String,
                            "Closely",
                            data.child("category").value as String,
                            data.child("description").value as String,
                            data.child("salePercent").value as Long,
                            data.child("provider").value as String,
                            data.child("amountS").value as Long,
                            data.child("amountSsold").value as Long,
                            data.child("amountM").value as Long,
                            data.child("amountMsold").value as Long,
                            data.child("amountL").value as Long,
                            data.child("amountLsold").value as Long,
                        )
                        sameCategoryAdapter.addDish(itemProvider)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FoodDetail, "Cannot load recommended!", Toast.LENGTH_LONG).show()
            }

        })
    }

}