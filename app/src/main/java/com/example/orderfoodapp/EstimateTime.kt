package com.example.orderfoodapp

import android.app.Activity
import android.location.Address
import android.location.Geocoder
import java.util.*
import kotlin.math.*

class EstimateTime {
    private val speedPerMin = 500


    fun estimateTime(
        userLat: Double,
        userLng: Double,
        venueLat: Double,
        venueLng: Double
    ): String {
        val latDistance = Math.toRadians(userLat - venueLat)
        val lngDistance = Math.toRadians(userLng - venueLng)

        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(userLat)) *
                cos(Math.toRadians(venueLat)) *
                sin(lngDistance / 2) *
                sin(lngDistance / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        val meters = 6371 * c * 1000
        val time = (meters / speedPerMin).roundToInt()


        return if (time <= 5)
            "Closely"
        else
            "$time min"
    }
}