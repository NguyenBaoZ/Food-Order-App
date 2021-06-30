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
    var rated: String,
    var deliveryTime: String,
    var category: String,
    var description: String,
    var salePercent: Long,
    var provider: String,
    var amountS: Long,
    var amountSsold: Long,
    var amountM: Long,
    var amountMsold: Long,
    var amountL: Long,
    var amountLsold: Long
): Parcelable {

    //    constructor() {
//        // Default constructor required for calls to DataSnapshot.getValue(Message.class)
//    }

}
