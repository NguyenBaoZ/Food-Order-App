package com.example.orderfoodapp.adapters

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.orderfoodapp.R
import com.example.orderfoodapp.activities.FoodDetailActivity
import com.example.orderfoodapp.models.Dish
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.dish_item.view.*
import java.io.File
import java.text.DecimalFormat

class DishAdapter (
    private val dishList: MutableList<Dish>
): RecyclerView.Adapter<DishAdapter.DishViewHolder>() {

    class DishViewHolder (itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishViewHolder {
        return DishViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_dish,
                parent,
                false
            )
        )
    }

    fun addDish(dish: Dish) {
        var sum: Long = 0
        var count = 0
        val dbRef = FirebaseDatabase.getInstance().getReference("Comment/${dish.id}")
        dbRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    sum += data.child("rating").value as Long
                    count++
                }

                if(count != 0) {
                    val rating: Double = sum*1.0 / count
                    val df = DecimalFormat("#.#")
                    dish.rated = df.format(rating)
                }
                else {
                    dish.rated = "Not rated"
                }

                dishList.add(dish)
                notifyItemInserted(dishList.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun addDishTopRating(dish: Dish) {
        var sum: Long = 0
        var count = 0
        val dbRef = FirebaseDatabase.getInstance().getReference("Comment/${dish.id}")
        dbRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    sum += data.child("rating").value as Long
                    count++
                }

                if(count != 0) {
                    val rating: Double = sum*1.0 / count
                    val df = DecimalFormat("#.#")
                    dish.rated = df.format(rating)
                    if(rating >= 4.5) {
                        dishList.add(dish)
                        notifyItemInserted(dishList.size - 1)
                    }
                }
                else {
                    dish.rated = "Not rated"
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun deleteAll() {
        dishList.clear()
        notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
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

            //adjust layout a bit if rating is a number
            if(curDish.rated != "Not rated") {
                dishRating_textView.textSize = 17F
                setMargins(star_icon, 20, 0, 0, 0)
            }

            dishName_textView.text = curDish.name
            dishRating_textView.text = curDish.rated
            deliveryTime_textView.text = curDish.deliveryTime

            val saleOff = curDish.salePercent.toInt()
            if(saleOff != 0) {
                saleOff_textView.visibility = View.VISIBLE
                saleOff_textView.text = " $saleOff% OFF "
            }

            setOnClickListener {
                val intent = Intent(context, FoodDetailActivity::class.java)
                intent.putExtra("curDish", curDish)
                val option: ActivityOptionsCompat? =
                    ViewCompat.getTransitionName(dishImage_imageView)?.let { it1 ->
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            context as Activity, dishImage_imageView, it1
                        )
                    }

                context.startActivity(intent, option?.toBundle())
            }
        }

    }

    override fun getItemCount(): Int {
        return dishList.size
    }

    private fun setMargins(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        if (view.layoutParams is ViewGroup.MarginLayoutParams) {
            val p = view.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(left, top, right, bottom)
            view.requestLayout()
        }
    }

}