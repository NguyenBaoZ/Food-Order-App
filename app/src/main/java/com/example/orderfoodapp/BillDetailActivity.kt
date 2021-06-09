package com.example.orderfoodapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_bill_detail.*

class BillDetailActivity: AppCompatActivity() {
    private lateinit var billAdapter: BillAdapter
    private var id = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bill_detail)

        id = intent.getStringExtra("id").toString()

        billAdapter = BillAdapter(mutableListOf())
        billDetail_recyclerView.adapter = billAdapter

        val layoutManager = LinearLayoutManager(this)
        billDetail_recyclerView.layoutManager = layoutManager

        val dbRef = FirebaseDatabase.getInstance().getReference("Bill/$id/products")
        dbRef.get().addOnSuccessListener {
            billAdapter.deleteAll()
            for(data in it.children) {
                var unitPrice = 0.0
                val a: Any = data.child("unitPrice").value as Any
                val type = a::class.simpleName
                if(type == "Long" || type == "Double")
                    unitPrice = a.toString().toDouble()

                val bill = BillItem(
                    data.child("amount").value as Long,
                    data.child("name").value as String,
                    unitPrice
                )
                billAdapter.addBill(bill)
            }
        }

        val dbRef2 = FirebaseDatabase.getInstance().getReference("Bill/$id")
        dbRef2.get().addOnSuccessListener {
            val time = it.child("time").value as String
            val subTotal = it.child("subTotal").value as Double
            val total = it.child("total").value as Double
            val deliveryFee = total - subTotal

            subtotal_textView.text = "$$subTotal"
            total_textView.text = "$$total"
            deliveryFee_textView.text = "$$deliveryFee"
            time_textView.text = time
        }

        findCustomer(id)
    }

    private fun findCustomer(id: String) {
        val customerEmail = Firebase.auth.currentUser?.email.toString()
        val dbRef = FirebaseDatabase.getInstance().getReference("Customer")
        dbRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    if(data.child("email").value as String == customerEmail) {
                        orderID_textView.text = id
                        name_textView.text = data.child("fullName").value as String
                        phone_textView.text = data.child("phoneNumber").value as String
                        address_textView.text = data.child("address").value as String
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}