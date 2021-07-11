package com.example.orderfoodapp.adapters

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.orderfoodapp.R
import com.example.orderfoodapp.activities.MainMenuActivity
import com.example.orderfoodapp.models.CommentItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.item_comment.view.*
import java.io.File

class CommentAdapter (
    private val commentList: MutableList<CommentItem>
): RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {
    private var name: String = ""

    class CommentViewHolder (itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder (
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_comment,
                parent,
                false
            )
        )
    }

    fun addComment(comment: CommentItem) {
        commentList.add(0, comment)
        notifyItemInserted(0)
    }

    fun deleteAll() {
        commentList.clear()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val curComment = commentList[position]
        val dbRef = FirebaseDatabase.getInstance().getReference("Customer")
        dbRef.get().addOnSuccessListener {
            for(data in it.children) {
                if(data.child("email").value as String == curComment.customerEmail) {
                    name = data.child("fullName").value as String

                    var imageName = data.child("email").value as String
                    imageName = imageName.replace(".", "_")

                    val storageRef = FirebaseStorage.getInstance().getReference("avatar_image/$imageName.jpg")
                    try {
                        val localFile = File.createTempFile("tempfile", ".jpg")
                        storageRef.getFile(localFile)
                            .addOnSuccessListener {
                                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                                holder.itemView.circleImageView.setImageBitmap(bitmap)
                            }
                            .addOnFailureListener {
                                holder.itemView.circleImageView.setImageResource(R.drawable.img_test_avatar)
                            }
                    }
                    catch (e: Exception) {
                        e.printStackTrace()
                    }

                    holder.itemView.apply {
                        name_textView.text = name
                        time_textView.text = curComment.time
                        content_textView.text = curComment.comment
                        showRatingImage(holder, curComment.rating)
                    }

                    break
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    private fun showRatingImage(holder: CommentViewHolder, rating: Long) {
        when(rating) {
            1L -> {
                holder.itemView.status_textView.text = "Awful"
                holder.itemView.star1_image.setImageResource(R.drawable.ic_star)
                holder.itemView.star2_image.setImageResource(R.drawable.ic_empty_star)
                holder.itemView.star3_image.setImageResource(R.drawable.ic_empty_star)
                holder.itemView.star4_image.setImageResource(R.drawable.ic_empty_star)
                holder.itemView.star5_image.setImageResource(R.drawable.ic_empty_star)
            }

            2L -> {
                holder.itemView.status_textView.text = "Bad"
                holder.itemView.star1_image.setImageResource(R.drawable.ic_star)
                holder.itemView.star2_image.setImageResource(R.drawable.ic_star)
                holder.itemView.star3_image.setImageResource(R.drawable.ic_empty_star)
                holder.itemView.star4_image.setImageResource(R.drawable.ic_empty_star)
                holder.itemView.star5_image.setImageResource(R.drawable.ic_empty_star)
            }

            3L -> {
                holder.itemView.status_textView.text = "Normal"
                holder.itemView.star1_image.setImageResource(R.drawable.ic_star)
                holder.itemView.star2_image.setImageResource(R.drawable.ic_star)
                holder.itemView.star3_image.setImageResource(R.drawable.ic_star)
                holder.itemView.star4_image.setImageResource(R.drawable.ic_empty_star)
                holder.itemView.star5_image.setImageResource(R.drawable.ic_empty_star)
            }

            4L -> {
                holder.itemView.status_textView.text = "Good"
                holder.itemView.star1_image.setImageResource(R.drawable.ic_star)
                holder.itemView.star2_image.setImageResource(R.drawable.ic_star)
                holder.itemView.star3_image.setImageResource(R.drawable.ic_star)
                holder.itemView.star4_image.setImageResource(R.drawable.ic_star)
                holder.itemView.star5_image.setImageResource(R.drawable.ic_empty_star)
            }

            5L -> {
                holder.itemView.status_textView.text = "Awesome"
                holder.itemView.star1_image.setImageResource(R.drawable.ic_star)
                holder.itemView.star2_image.setImageResource(R.drawable.ic_star)
                holder.itemView.star3_image.setImageResource(R.drawable.ic_star)
                holder.itemView.star4_image.setImageResource(R.drawable.ic_star)
                holder.itemView.star5_image.setImageResource(R.drawable.ic_star)
            }
        }
    }

}