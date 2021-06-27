package com.example.orderfoodapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.comment_item.view.*

class CommentAdapter (
    private val commentList: MutableList<CommentItem>
): RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {
    private var name: String = ""

    class CommentViewHolder (itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder (
            LayoutInflater.from(parent.context).inflate(
                R.layout.comment_item,
                parent,
                false
            )
        )
    }

    fun addComment(comment: CommentItem) {
        commentList.add(comment)
        notifyItemInserted(commentList.size - 1)
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
                    name = if(data.child("fullName").value as String != "edit here")
                        data.child("fullName").value as String
                    else
                        "Unknown"

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