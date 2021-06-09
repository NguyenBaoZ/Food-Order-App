package com.example.orderfoodapp

data class CartItem(
    var cartID: String,
    var cartSize: String,
    var cartItemImage: String,
    var cartItemName: String,
    var cartItemAmount: Long,
    var cartItemPrice: Double
)
