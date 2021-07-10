package com.example.orderfoodapp.adapters

import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.orderfoodapp.R
import com.example.orderfoodapp.activities.FoodDetailActivity
import com.example.orderfoodapp.models.Dish
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.favorite_item.view.*
import java.io.File

class FavouriteAdapter (
    private val favList: MutableList<Dish>
): RecyclerView.Adapter<FavouriteAdapter.FavViewHolder>() {

    class FavViewHolder (itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavViewHolder {
        return FavViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_favorite,
                parent,
                false
            )
        )
    }

    fun addFav(dish: Dish) {
        favList.add(dish)
        notifyItemInserted(favList.size - 1)
    }

    fun deleteAll() {
        favList.clear()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: FavViewHolder, position: Int) {
        val curFav = favList[position]

        val storageRef = FirebaseStorage.getInstance().getReference("dish_image/${curFav.id}.jpg")
        try {
            val localFile = File.createTempFile("tempfile", ".jpg")
            storageRef.getFile(localFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                holder.itemView.item_image.setImageBitmap(bitmap)
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }

        holder.itemView.apply {
            itemName_text.text = curFav.name
            item_price.text = curFav.priceS.toString()
            star_rating_text.text = curFav.rated.toString()

            setOnClickListener {
                val intent = Intent(context, FoodDetailActivity::class.java)
                intent.putExtra("curDish", curFav)
                context.startActivities(arrayOf(intent))
            }
        }
    }

    override fun getItemCount(): Int {
        return favList.size
    }
}