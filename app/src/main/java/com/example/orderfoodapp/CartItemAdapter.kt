package com.example.orderfoodapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import kotlinx.android.synthetic.main.activity_food_detail.*
import kotlinx.android.synthetic.main.cart_item.view.*

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

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        val curCartItem = cartList[position]

        viewBinderHelper.setOpenOnlyOne(true)
        viewBinderHelper.bind(holder.itemView.swipe_layout, curCartItem.toString())
        viewBinderHelper.closeLayout(curCartItem.toString())

        holder.itemView.apply {
            foodImage_imageView.setImageResource(curCartItem.cartItemImage)
            foodName_textView.text = curCartItem.cartItemName
            amount_textView.text = curCartItem.cartItemAmount.toString()
            price_textView.text = curCartItem.cartItemPrice.toString()

            delete_button.setOnClickListener() {
                deleteCartItem(position)
            }

            note_button.setOnClickListener() {
                note_layout.visibility = View.VISIBLE
            }

            increase_button.setOnClickListener() {
                var amount = amount_textView.text.toString().toInt()
                amount++
                amount_textView.text = amount.toString()
            }

            decrease_button.setOnClickListener() {
                var amount = amount_textView.text.toString().toInt()
                if(amount > 1) {
                    amount--
                    amount_textView.text = amount.toString()
                }
            }
        }


    }

    override fun getItemCount(): Int {
        return cartList.size
    }

}