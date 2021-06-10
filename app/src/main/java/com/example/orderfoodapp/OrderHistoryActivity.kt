package com.example.orderfoodapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_order_history.*
import kotlinx.android.synthetic.main.fragment_filter_all_food.*

class OrderHistoryActivity : AppCompatActivity() {
    private lateinit var orderHisAdapter: OrderHisAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        val customerEmail = Firebase.auth.currentUser?.email.toString()

        orderHisAdapter = OrderHisAdapter(mutableListOf())
        orderHis_recyclerView.adapter = orderHisAdapter

        val layoutManager = LinearLayoutManager(this)
        orderHis_recyclerView.layoutManager = layoutManager

        val dbRef = FirebaseDatabase.getInstance().getReference("Bill")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orderHisAdapter.deleteAll()
                for (data in snapshot.children) {
                    if ((data.child("customerEmail").value)?.equals(customerEmail) == true
                        && data.child("status").value?.equals("done") == true) {

                        var total = 0.0
                        val a: Any = data.child("total").value as Any
                        val type = a::class.simpleName
                        if(type == "Long" || type == "Double")
                            total = a.toString().toDouble()

                        val dbRef2 = FirebaseDatabase.getInstance().getReference("Bill/${data.key}/products")
                        dbRef2.get().addOnSuccessListener {
                            val count = it.childrenCount.toInt()
                            val order = OrderHis(
                                data.key.toString(),
                                total,
                                count,
                                data.child("time").value as String
                            )
                            orderHisAdapter.addOrder(order)
                        }

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        back_layout.setOnClickListener() {
            finish()
        }
    }
}