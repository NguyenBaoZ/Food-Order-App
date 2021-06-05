package com.example.orderfoodapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dish_item.view.*

class DishAdapter (
    private val dishList: MutableList<Dish>
): RecyclerView.Adapter<DishAdapter.DishViewHolder>() {

    class DishViewHolder (itemView: View): RecyclerView.ViewHolder(itemView)

    private val foodDetail: FoodDetail = FoodDetail()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        return DishViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.dish_item,
                parent,
                false
            )
        )
    }

    fun addDish(dish: Dish) {
        dishList.add(dish)
        notifyItemInserted(dishList.size - 1)
    }

    fun deleteAll() {
        dishList.clear()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: DishViewHolder, position: Int) {
        val curDish = dishList[position]
        holder.itemView.apply {
            Picasso.get().load(curDish.image).into(dishImage_imageView)
            dishName_textView.text = curDish.name
            dishRating_textView.text = curDish.rated.toString()
            deliveryTime_textView.text = curDish.deliveryTime

            setOnClickListener { view: View ->
                val intent = Intent(context,FoodDetail::class.java)
                intent.putExtra("curDish", curDish)
                context.startActivities(arrayOf(intent))
            }
        }

    }

    override fun getItemCount(): Int {
        return dishList.size
    }

}