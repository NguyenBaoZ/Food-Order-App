package com.example.orderfoodapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.comment_item.view.*
import kotlinx.android.synthetic.main.dish_item.view.*
import java.io.File

class DishAdapter (
    private val dishList: MutableList<Dish>
): RecyclerView.Adapter<DishAdapter.DishViewHolder>() {

    class DishViewHolder (itemView: View): RecyclerView.ViewHolder(itemView)

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

        val storageRef = FirebaseStorage.getInstance().getReference("dish_image/${curDish.id}.jpg")
        try {
            val localFile = File.createTempFile("tempfile", ".jpg")
            storageRef.getFile(localFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                holder.itemView.dishImage_imageView.setImageBitmap(bitmap)
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }

        holder.itemView.apply {
            dishName_textView.text = curDish.name
            dishRating_textView.text = curDish.rated.toString()
            deliveryTime_textView.text = curDish.deliveryTime

            val saleOff = curDish.salePercent.toInt()
            if(saleOff != 0) {
                saleOff_textView.visibility = View.VISIBLE
                saleOff_textView.text = " $saleOff% OFF "
            }

            setOnClickListener {
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