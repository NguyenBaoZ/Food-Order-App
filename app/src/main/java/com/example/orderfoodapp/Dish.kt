package com.example.orderfoodapp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Dish(
    var dishImage: Int,
    var dishName: String,
    var dishRating: String,
    var deliveryTime: String
): Parcelable
