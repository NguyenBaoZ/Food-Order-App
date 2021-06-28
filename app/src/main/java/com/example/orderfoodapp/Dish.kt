package com.example.orderfoodapp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Dish(
    var id: String,
    var name: String,
    var priceS: Double,
    var priceM: Double,
    var priceL: Double,
    var rated: Double,
    var deliveryTime: String,
    var category: String,
    var description: String,
    var salePercent: Long,
    var amount: Long
): Parcelable {

    //    constructor() {
//        // Default constructor required for calls to DataSnapshot.getValue(Message.class)
//    }

}
