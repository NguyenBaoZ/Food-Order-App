package com.example.orderfoodapp

import android.app.Activity
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_checkout.*
import kotlinx.android.synthetic.main.fragment_main_menu.*
import java.text.DecimalFormat
import java.util.*
import kotlin.math.roundToInt

class CheckoutActivity : AppCompatActivity() {

    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private var pay: Int = 0
    private val df = DecimalFormat("##.##")
    private var key = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        var subTotal = 0.0
        key = intent.getStringExtra("key").toString()
        val dbRef = FirebaseDatabase.getInstance().getReference("Bill/$key/subTotal")
        dbRef.get().addOnSuccessListener {
            subtotal_textView.text = it.value.toString()
            total_textView.text = it.value.toString()

            val a: Any = it.value as Any
            val type = a::class.simpleName
            if (type == "Long" || type == "Double")
                subTotal = a.toString().toDouble()

        }

        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
        getLocation()

        back_button.setOnClickListener() {
            finish()
        }

        standard_button.setOnClickListener() {
            calculateFee(it, subTotal)
        }

        premium_button.setOnClickListener() {
            calculateFee(it, subTotal)
        }

        cod_button.setOnClickListener() {
            pay = 1
        }

        myWallet_button.setOnClickListener() {
            pay = 2
        }

        confirm_button.setOnClickListener() {
            if(deliveryFee_textView.text == "0.00") {
                Toast.makeText(this, "Please choose a Delivery method!", Toast.LENGTH_LONG).show()
            }
            else if(pay == 0) {
                Toast.makeText(this, "Please choose a Payment method!", Toast.LENGTH_LONG).show()
            }
            else {
                checkout()
            }
        }
    }

    private fun checkout() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Bill/$key")
        val finalTotal = total_textView.text.toString().toDouble()
        dbRef.child("total").setValue(finalTotal)
        dbRef.child("status").setValue("done")
        findID()
        Toast.makeText(this, "Successfully!", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun findID() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Bill/$key/products")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    updateAmount(
                        data.child("id").value as String,
                        data.child("amount").value as Long
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun updateAmount(id: String, amount: Long) {
        var curAmount: Long
        val dbUpdate = FirebaseDatabase.getInstance().getReference("Product/$id")
        dbUpdate.child("amount").get().addOnSuccessListener {
            curAmount = it.value as Long
            val newAmount = curAmount - amount
            dbUpdate.child("amount").setValue(newAmount)
        }
    }

    private fun calculateFee(view: View, subTotal: Double) {
        var fee = 0.0
        when(view.id) {
            R.id.standard_button -> {
                fee = subTotal * 0.1
                deliveryFee_textView.text = df.format(fee)
            }
            R.id.premium_button -> {
                fee = subTotal * 0.15
                deliveryFee_textView.text = df.format(fee)
            }
        }
        val newTotal = subtotal_textView.text.toString().toDouble() + fee
        total_textView.text = df.format(newTotal)
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProvider.lastLocation.addOnCompleteListener {
            val location = it.result
            if(location != null) {
                val geocoder = Geocoder(this, Locale.getDefault())
                val address: List<Address> = geocoder.getFromLocation(
                    location.latitude, location.longitude, 1
                )
                address_textView.text = address[0].getAddressLine(0)
            }
            else {
                Toast.makeText(this, "Cannot get current location!", Toast.LENGTH_LONG).show()
            }
        }
    }
}