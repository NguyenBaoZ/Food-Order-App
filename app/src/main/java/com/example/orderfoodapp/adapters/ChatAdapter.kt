package com.example.orderfoodapp.adapters

import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.orderfoodapp.R
import com.example.orderfoodapp.activities.MessageChatActivity
import com.example.orderfoodapp.models.ChatItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_message.view.avatar_image
import kotlinx.android.synthetic.main.item_chat.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val chatList: MutableList<ChatItem>
): RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder (itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_chat,
                parent,
                false
            )
        )
    }

    fun addChat(chat: ChatItem) {
        chatList.add(0, chat)
        notifyItemInserted(0)
    }

    fun deleteAll() {
        chatList.clear()
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val curChat = chatList[position]
        val curEmail = Firebase.auth.currentUser?.email.toString()
        val curTime = Calendar.getInstance().time
        val sdfDate = SimpleDateFormat("dd/MM/yyyy")
        val curDate = sdfDate.format(curTime)

        var desEmail = curChat.senderEmail
        if(desEmail == curEmail)
            desEmail = curChat.receiverEmail

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

        holder.itemView.apply {
            time_textView.text = curChat.time

            if(curDate != curChat.date) {
                date_textView.text = curChat.date
                date_textView.visibility = View.VISIBLE
            }
            else
                date_textView.visibility = View.GONE

            if(curEmail == curChat.senderEmail) {
                if(curChat.image.isNotEmpty())
                    message_textView.text = "You: [image]"
                else
                    message_textView.text = "You: ${curChat.message}"
            }
            else {
                if(curChat.image.isNotEmpty())
                    message_textView.text = "[image]"
                else
                    message_textView.text = curChat.message
            }

//            val dbRef = FirebaseDatabase.getInstance().getReference("Provider").orderByChild("email").equalTo(desEmail)
//            dbRef.get().addOnSuccessListener {
//                name_textView.text = it.child("name").value as String
//            }

            val dbRef = FirebaseDatabase.getInstance().getReference("Provider")
            dbRef.get().addOnSuccessListener {
                for(data in it.children) {
                    if(data.child("email").value as String == desEmail) {
                        name_textView.text = data.child("name").value as String
                        break
                    }
                }
            }

            setOnClickListener() {
                val intent = Intent(context, MessageChatActivity::class.java)
                intent.putExtra("providerEmail", desEmail)
                context.startActivities(arrayOf(intent))
            }
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }


}