package com.example.orderfoodapp.models

data class CartItem(
    var cartID: String,
    var cartSize: String,
    var cartItemName: String,
    var cartItemAmount: Long,
    var cartItemPrice: Double
)
