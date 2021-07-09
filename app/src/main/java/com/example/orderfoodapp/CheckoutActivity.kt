package com.example.orderfoodapp

import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_checkout.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class CheckoutActivity : AppCompatActivity() {

    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private var pay: Int = 0
    private val df = DecimalFormat("##.##")
    private val sdf = SimpleDateFormat("EEE, d MMM yyyy")
    private var key = ""
    private var isBuyNow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        var subTotal = 0.0

        val bundle = intent.extras
        if(bundle != null) {
            key = bundle.getString("key").toString()
            isBuyNow = bundle.getBoolean("isBuyNow")
        }

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
            //delete this bill if it is buy now
            if(isBuyNow) {
                deleteBuyNow()
            }

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

    private fun deleteBuyNow() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Bill/$key")
        dbRef.get().addOnSuccessListener {
            it.ref.removeValue()
        }
    }

    private fun checkout() {
        val finalTotal = convertToDoubleFormat(total_textView.text.toString())
        val customerEmail = Firebase.auth.currentUser?.email.toString()

        val dbRef = FirebaseDatabase.getInstance().getReference("Customer")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    if(data.child("email").value as String == customerEmail) {

                        val a: Any = data.child("balance").value as Any
                        val type = a::class.simpleName
                        var curBalance = 0.0
                        if(type == "Long" || type == "Double")
                            curBalance = a.toString().toDouble()

                        val newBalance = curBalance - finalTotal

                        if(myWallet_button.isChecked) {
                            //update user balance if it equal or greater than bought money
                            if(newBalance >= 0) {
                                val dbUpdate = FirebaseDatabase.getInstance().getReference("Customer/${data.key}")
                                dbUpdate.child("balance").setValue(newBalance)
                                checkoutSuccess(finalTotal)
                            }
                            else {
                                Toast.makeText(this@CheckoutActivity, "Current balance is not enough", Toast.LENGTH_LONG).show()
                            }
                        }
                        else {
                            checkoutSuccess(finalTotal)
                        }

                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun checkoutSuccess(finalTotal: Double) {
        val now = sdf.format(Calendar.getInstance().time)
        val dbRef = FirebaseDatabase.getInstance().getReference("Bill/$key")
        dbRef.child("total").setValue(finalTotal)
        dbRef.child("status").setValue("done")
        dbRef.child("time").setValue(now)
        findProductID()
    }

    private fun findProductID() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Bill/$key/products")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    updateAmount(
                        data.child("id").value as String,
                        data.child("amount").value as Long,
                        data.child("size").value as String
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun updateAmount(id: String, amount: Long, size: String) {
        var curAmount: Long
        var amountSold: Long
        val dbUpdate = FirebaseDatabase.getInstance().getReference("Product/$id")

        //update amount left with corresponding size
        dbUpdate.child("amount$size").get().addOnSuccessListener {
            curAmount = it.value as Long
            val newAmount = curAmount - amount
            dbUpdate.child("amount$size").setValue(newAmount)
        }

        //update amount sold
        dbUpdate.child("amount${size}sold").get().addOnSuccessListener {
            amountSold = it.value as Long
            val newAmount = amountSold + amount
            dbUpdate.child("amount${size}sold").setValue(newAmount)
        }

        //successfully update data
        showDialog()
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

    private fun showDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_order_success)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val ok_button = dialog.findViewById<Button>(R.id.ok_button)
        ok_button.setOnClickListener() {
            dialog.dismiss()
            finish()
        }
        dialog.show()
    }

    private fun convertToDoubleFormat(str: String): Double {
        var strNum = str
        return if(strNum.contains(",")) {
            strNum = strNum.replace(",", ".")
            strNum.toDouble()
        } else
            strNum.toDouble()
    }
}