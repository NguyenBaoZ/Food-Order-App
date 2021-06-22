package com.example.orderfoodapp


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.cart_item.view.*
import java.text.DecimalFormat
import kotlin.math.roundToInt

class CartItemAdapter (
    private val cartList: MutableList<CartItem>
): RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder>() {

    private val viewBinderHelper = ViewBinderHelper()
    private var currentAction = ""
    private val df = DecimalFormat("##.##")

    private var key = ""
    private var keyProduct = ""
    private lateinit var customerEmail: String

    class CartItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        return CartItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.cart_item,
                parent,
                false
            )
        )
    }

    fun addCartItem(cartItem: CartItem) {
        cartList.add(cartItem)
        notifyItemInserted(cartList.size - 1)
    }

    private fun deleteCartItem(pos: Int) {
        cartList.removeAt(pos)
        notifyDataSetChanged()
    }

    fun deleteAll() {
        cartList.clear()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        val curCartItem = cartList[position]

        viewBinderHelper.setOpenOnlyOne(true)
        viewBinderHelper.bind(holder.itemView.swipe_layout, curCartItem.toString())
        viewBinderHelper.closeLayout(curCartItem.toString())

        customerEmail = Firebase.auth.currentUser?.email.toString()
        findCurrentKey()

        holder.itemView.apply {
            Picasso.get().load(curCartItem.cartItemImage).into(foodImage_imageView)
            foodName_textView.text = curCartItem.cartItemName
            amount_textView.text = curCartItem.cartItemAmount.toString()
            price_textView.text = curCartItem.cartItemPrice.toString()

            val unitPrice = curCartItem.cartItemPrice / curCartItem.cartItemAmount
            val id = curCartItem.cartID

            delete_button.setOnClickListener() {
                deleteCartItem(position)
                note_editText.setText("")
                note_layout.visibility = View.GONE
                deleteItem(curCartItem)
                Toast.makeText(this.context, "Deleted successfully!", Toast.LENGTH_LONG).show()
            }

            note_button.setOnClickListener() {
                note_layout.visibility = View.VISIBLE
            }

            increase_button.setOnClickListener() {
                currentAction = "+"
                var amount = amount_textView.text.toString().toInt()
                amount++
                val newPrice = df.format((amount * unitPrice)).toDouble()
                findKeyProduct(amount, newPrice, id)
            }

            decrease_button.setOnClickListener() {
                var amount = amount_textView.text.toString().toInt()
                if (amount > 1) {
                    currentAction = "-"
                    amount--
                    val newPrice = df.format((amount * unitPrice)).toDouble()
                    findKeyProduct(amount, newPrice, id)
                }
            }
        }


    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    private fun findCurrentKey() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Bill")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    if ((data.child("customerEmail").value)?.equals(customerEmail) == true
                         && data.child("status").value?.equals("pending") == true
                    ) {
                        key = data.key.toString()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun findKeyProduct(amount: Int, newUnitPrice: Double, id: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Bill/$key/products")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    if ((data.child("id").value as String) == id) {
                        keyProduct = data.key.toString()
                        updateProduct(amount, newUnitPrice)
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun updateProduct(amount: Int, newUnitPrice: Double) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Bill/$key/products/$keyProduct")
        dbRef.child("amount").setValue(amount)
        dbRef.child("unitPrice").setValue(newUnitPrice * 1.0)
        findTotalPrice(amount, newUnitPrice)
    }

    private fun findTotalPrice(amount: Int, newUnitPrice: Double) {
        var curTotal = 0.0
        var newTotal: Double
        val dbUpdate = FirebaseDatabase.getInstance().getReference("Bill/$key")
        dbUpdate.child("subTotal").get().addOnSuccessListener {
            val a: Any = it.value as Any
            val type = a::class.simpleName
            if(type == "Long" || type == "Double")
                curTotal = a.toString().toDouble()

            newTotal =   if(currentAction == "+")
                ((curTotal + (newUnitPrice / amount).toFloat()) * 100.0).roundToInt() /100.0
            else
                ((curTotal - (newUnitPrice / amount).toFloat()) * 100.0).roundToInt() /100.0

            dbUpdate.child("subTotal").setValue(newTotal)
        }
    }

    private fun deleteItem(curCart: CartItem) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Bill/$key/products")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    if ((data.child("id").value as String) == curCart.cartID &&
                        (data.child("size").value as String) == curCart.cartSize) {
                        var delPrice = 0.0

                        val a: Any = data.child("unitPrice").value as Any
                        val type = a::class.simpleName
                        if (type == "Long" || type == "Double")
                            delPrice = a.toString().toDouble()

                        updateAfterDel(delPrice)
                        data.ref.removeValue()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun updateAfterDel(delPrice: Double) {
        var curSubTotal = 0.0
        var newSubTotal: Double
        val dbUpdate = FirebaseDatabase.getInstance().getReference("Bill/$key")
        dbUpdate.child("subTotal").get().addOnSuccessListener {
            val a: Any = it.value as Any
            val type = a::class.simpleName
            if (type == "Long" || type == "Double")
                curSubTotal = a.toString().toDouble()

            newSubTotal = curSubTotal - delPrice
            dbUpdate.child("subTotal").setValue(df.format(newSubTotal).toDouble())
        }
    }

}