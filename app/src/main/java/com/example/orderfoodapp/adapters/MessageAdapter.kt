package com.example.orderfoodapp.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.orderfoodapp.R
import com.example.orderfoodapp.models.ChatItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.item_message_left.view.*
import kotlinx.android.synthetic.main.item_message_right.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private val messageList: MutableList<ChatItem>
): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val MESSAGE_LEFT = 0
    private val MESSAGE_RIGHT = 1
    private lateinit var image: Bitmap

    class MessageViewHolder (itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        if(viewType == MESSAGE_RIGHT) {
            return MessageViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_message_right,
                    parent,
                    false
                )
            )
        }
        else {
            return MessageViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_message_left,
                    parent,
                    false
                )
            )
        }
    }

    fun addMessage(chat: ChatItem) {
        messageList.add(chat)
        notifyItemInserted(messageList.size - 1)
    }

    fun deleteAll() {
        messageList.clear();
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val curChat = messageList[position]
        val curEmail = Firebase.auth.currentUser?.email.toString()
        val curTime = Calendar.getInstance().time
        val sdfDate = SimpleDateFormat("dd/MM/yyyy")
        val curDate = sdfDate.format(curTime)

        var desEmail = curChat.senderEmail
        if(desEmail == curEmail)
            desEmail = curChat.receiverEmail

        if(curChat.senderEmail == curEmail) {
            holder.itemView.apply {
                val message_textView = findViewById<TextView>(R.id.messageRight_textView)
                val time_textView = findViewById<TextView>(R.id.time_textView)
                val date_textView = findViewById<TextView>(R.id.date_textView)
                val date_layout = findViewById<LinearLayout>(R.id.date_layout)
                val messageRight_image = findViewById<ImageView>(R.id.messageRight_image)

                message_textView.text = curChat.message
                time_textView.text = curChat.time

                if(curDate == curChat.date)
                    date_textView.text = "Today"
                else
                    date_textView.text = curChat.date

                if(curChat.image.isNotEmpty()) {
                    val storageRef = FirebaseStorage.getInstance().getReference("chat_image/${curChat.image}.jpg")
                    val localFile = File.createTempFile("tempfile", ".jpg")
                    storageRef.getFile(localFile).addOnSuccessListener {
                        val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                        messageRight_image.setImageBitmap(bitmap)
                    }

                    if(position == 0) {
                        messageRight_image.visibility = View.VISIBLE
                        time_textView.visibility = View.VISIBLE
                        message_textView.visibility = View.GONE
                        date_layout.visibility = View.VISIBLE
                    }
                    else if(messageList[position].date == messageList[position - 1].date) {
                        //same date with previous
                        messageRight_image.visibility = View.VISIBLE
                        time_textView.visibility = View.VISIBLE
                        message_textView.visibility = View.GONE
                        date_layout.visibility = View.GONE
                    }
                    else if(messageList[position].date != messageList[position - 1].date) {
                        messageRight_image.visibility = View.VISIBLE
                        time_textView.visibility = View.VISIBLE
                        message_textView.visibility = View.GONE
                        date_layout.visibility = View.VISIBLE
                    }

                    return
                }
                else {
                    messageRight_image.visibility = View.GONE
                    message_textView.visibility = View.VISIBLE
                }

                //if this curChat doesn't have image, load data for message
                if(position == 0
                || messageList[position].senderEmail != messageList[position - 1].senderEmail) {
                    if(position + 1 != messageList.size) {
                        //message
                        if(messageList[position].senderEmail != messageList[position + 1].senderEmail) {
                            message_textView.setBackgroundResource(R.drawable.bg_message_right_top)
                            time_textView.visibility = View.VISIBLE
                        }
                        else {
                            if(messageList[position].date == messageList[position + 1].date) {
                                message_textView.setBackgroundResource(R.drawable.bg_message_right_top)
                                time_textView.visibility = View.GONE
                            }
                            else {
                                message_textView.setBackgroundResource(R.drawable.bg_message_right_top)
                                time_textView.visibility = View.VISIBLE
                                date_layout.visibility = View.GONE
                            }
                        }

                    }
                    //date
                    if(position == 0
                        || messageList[position].date != messageList[position - 1].date) {
                        date_layout.visibility = View.VISIBLE
                    }
                    else if(messageList[position].date == messageList[position - 1].date) {
                        message_textView.setBackgroundResource(R.drawable.bg_message_right_top)
                        date_layout.visibility = View.GONE
                    }
                }
                else if(position == messageList.size - 1
                || messageList[position].senderEmail != messageList[position + 1].senderEmail) {
                    //in case of the same date with previous message
                    if(messageList[position].date == messageList[position - 1].date) {
                        message_textView.setBackgroundResource(R.drawable.bg_message_right_bottom)
                        time_textView.visibility = View.VISIBLE
                        date_layout.visibility = View.GONE
                    }
                    else {
                        message_textView.setBackgroundResource(R.drawable.bg_message_right_top)
                        time_textView.visibility = View.VISIBLE
                        date_layout.visibility = View.VISIBLE
                    }
                }
                else if(messageList[position].senderEmail == messageList[position - 1].senderEmail
                     && messageList[position].senderEmail == messageList[position + 1].senderEmail) {
                    if(messageList[position].date == messageList[position - 1].date
                    && messageList[position].date == messageList[position + 1].date) {
                        //same with previous and next
                        message_textView.setBackgroundResource(R.drawable.bg_message_right_middle)
                        time_textView.visibility = View.GONE
                        date_layout.visibility = View.GONE
                    }
                    else if(messageList[position].date != messageList[position - 1].date
                         && messageList[position].date == messageList[position + 1].date) {
                        //different from previous, same with next
                        message_textView.setBackgroundResource(R.drawable.bg_message_right_top)
                        time_textView.visibility = View.GONE
                        date_layout.visibility = View.VISIBLE
                    }
                    else if(messageList[position].date == messageList[position - 1].date
                         && messageList[position].date != messageList[position + 1].date) {
                        //different from next, same with previous
                        message_textView.setBackgroundResource(R.drawable.bg_message_right_bottom)
                        time_textView.visibility = View.VISIBLE
                        date_layout.visibility = View.GONE
                    }
                }
            }
        }
        else {
            holder.itemView.apply {
                val message_textView = findViewById<TextView>(R.id.messageLeft_textView)
                val time_textView = findViewById<TextView>(R.id.time_textView)
                val avatar_image = findViewById<View>(R.id.avatar_image)
                val date_textView = findViewById<TextView>(R.id.date_textView)
                val date_layout = findViewById<LinearLayout>(R.id.date_layout)
                val messageLeft_image = findViewById<ImageView>(R.id.messageLeft_image)

                message_textView.text = curChat.message
                time_textView.text = curChat.time

                if(curDate == curChat.date)
                    date_textView.text = "Today"
                else
                    date_textView.text = curChat.date

                val imageName = desEmail.replace(".", "_")
                val storageRef = FirebaseStorage.getInstance().getReference("avatar_image/$imageName.jpg")
                try {
                    val localFile = File.createTempFile("tempfile", ".jpg")
                    storageRef.getFile(localFile).addOnSuccessListener {
                        val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                        holder.itemView.avatar_image.setImageBitmap(bitmap)
                    }
                }
                catch (e: Exception) {
                    e.printStackTrace()
                }

                //load image of message
                if(curChat.image.isNotEmpty()) {
                    val storageRef2 = FirebaseStorage.getInstance().getReference("chat_image/${curChat.image}.jpg")
                    val localFile = File.createTempFile("tempfile", ".jpg")
                    storageRef2.getFile(localFile).addOnSuccessListener {
                        val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                        messageRight_image.setImageBitmap(bitmap)
                    }

                    if(position == 0
                    || messageList[position].date != messageList[position - 1].date) {
                        messageLeft_image.visibility = View.VISIBLE
                        time_textView.visibility = View.VISIBLE
                        message_textView.visibility = View.GONE
                        date_layout.visibility = View.VISIBLE
                        avatar_image.visibility = View.VISIBLE
                    }
                    else if(messageList[position].date == messageList[position - 1].date) {
                        //same date with previous
                        messageLeft_image.visibility = View.VISIBLE
                        time_textView.visibility = View.VISIBLE
                        message_textView.visibility = View.GONE
                        date_layout.visibility = View.GONE
                        avatar_image.visibility = View.VISIBLE
                    }

                    return
                }
                else {
                    messageLeft_image.visibility = View.GONE
                    message_textView.visibility = View.VISIBLE
                }

                //first message
                if(position == 0
                || messageList[position].senderEmail != messageList[position - 1].senderEmail) {
                    if(messageList[position].date != messageList[position - 1].date) {
                        avatar_image.visibility = View.VISIBLE
                        time_textView.visibility = View.VISIBLE
                        date_layout.visibility = View.VISIBLE
                    }
                    else {
                        date_layout.visibility = View.GONE
                    }
                }
                else if(messageList[position].senderEmail == messageList[position - 1].senderEmail) {
                    if(messageList[position].date == messageList[position - 1].date) {
                        avatar_image.visibility = View.GONE
                        time_textView.visibility = View.GONE
                        date_layout.visibility = View.GONE
                    }
                    else {
                        avatar_image.visibility = View.VISIBLE
                        time_textView.visibility = View.VISIBLE
                        date_layout.visibility = View.VISIBLE
                    }
                }
            }
        }


    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        val curEmail = Firebase.auth.currentUser?.email.toString()
        return if(messageList[position].senderEmail == curEmail)
            MESSAGE_RIGHT
        else
            MESSAGE_LEFT
    }

}