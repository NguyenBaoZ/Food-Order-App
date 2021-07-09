package com.example.orderfoodapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.orderfoodapp.ChatAdapter
import com.example.orderfoodapp.ChatItem
import com.example.orderfoodapp.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_chat.*

class ChatFragment : Fragment() {
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onResume() {
        super.onResume()

        chatAdapter = ChatAdapter(mutableListOf())
        chat_recyclerView.adapter = chatAdapter

        val layoutManager = LinearLayoutManager(context)
        chat_recyclerView.layoutManager = layoutManager

        val customerEmail = Firebase.auth.currentUser?.email.toString()
        val hashMap = HashMap<String, ChatItem>()

        val dbRef = FirebaseDatabase.getInstance().getReference("Chat")
        dbRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                chatAdapter.deleteAll()
                for(data in snapshot.children) {
                    if(data.child("senderEmail").value as String == customerEmail
                    ||data.child("receiverEmail").value as String == customerEmail) {
                        val item = data.getValue(ChatItem::class.java)

                        var providerEmail = data.child("senderEmail").value as String
                        if(providerEmail == customerEmail)
                            providerEmail = data.child("receiverEmail").value as String

                        if (item != null) {
                            hashMap[providerEmail] = item
                        }
                    }
                }

                if(hashMap.isNotEmpty()) {
                    empty_background.visibility = View.GONE
                    loadData(hashMap)
                }
                else {
                    empty_background.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun convertDate(date: String, time: String): String {
        val arrayDate = date.split("/")
        val arrayTime = time.split(":")
        return arrayDate[2] + arrayDate[1] + arrayDate[0] + arrayTime[0] + arrayTime[1]
    }

    private fun loadData(hashMap: HashMap<String, ChatItem>) {
        val result = hashMap.toList().sortedBy { (_, value) -> convertDate(value.date, value.time)}.toMap()

        for (entry in result) {
            chatAdapter.addChat(entry.value)
        }

        val layoutAnim = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_anim_right_to_left)
        chat_recyclerView.layoutAnimation = layoutAnim
    }
}