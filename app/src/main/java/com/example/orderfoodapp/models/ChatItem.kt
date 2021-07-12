package com.example.orderfoodapp.models

data class ChatItem(
    var senderEmail: String = "",
    var receiverEmail: String = "",
    var image: String = "",
    var message: String = "",
    var date: String = "",
    var time: String = ""
)
