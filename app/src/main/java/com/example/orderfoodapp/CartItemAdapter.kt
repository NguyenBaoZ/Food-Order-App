package com.example.orderfoodapp


import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.cart_item.view.*
import java.text.DecimalFormat

class CartItemAdapter (
    private val cartList: MutableList<CartItem>
): RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder>() {

    private val viewBinderHelper = ViewBinderHelper()

    class CartItemViewHolder (itemView: View): RecyclerView.ViewHolder(itemView)

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

    fun deleteCartItem(pos: Int) {
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

        holder.itemView.apply {
            Picasso.get().load(curCartItem.cartItemImage).into(foodImage_imageView)
            foodName_textView.text = curCartItem.cartItemName
            amount_textView.text = curCartItem.cartItemAmount.toString()
            price_textView.text = curCartItem.cartItemPrice.toString()

            val unitPrice = curCartItem.cartItemPrice/curCartItem.cartItemAmount
            val df = DecimalFormat("##.0")

            delete_button.setOnClickListener() {
                deleteCartItem(position)
                note_editText.setText("")
                note_layout.visibility = View.GONE
            }

            note_button.setOnClickListener() {
                note_layout.visibility = View.VISIBLE
            }

            increase_button.setOnClickListener() {
                var amount = amount_textView.text.toString().toInt()
                amount++
                amount_textView.text = amount.toString()
                price_textView.text = df.format((amount * unitPrice))
                findKey()
            }

            decrease_button.setOnClickListener() {
                var amount = amount_textView.text.toString().toInt()
                if(amount > 1) {
                    amount--
                    amount_textView.text = amount.toString()
                    price_textView.text = df.format((amount * unitPrice))
                }
            }
        }


    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    private fun findKey() {
//        val dbRef = FirebaseDatabase.getInstance().getReference("Bill/-MbRSruAkFIk4TmyUzPn")
//        dbRef.child("total").setValue(69.69)
//        val mAuth = Firebase.auth
//        Log.i("msg", mAuth.currentUser.toString())
    }

}