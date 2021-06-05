package com.example.orderfoodapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.orderfoodapp.Dish
import com.example.orderfoodapp.DishAdapter
import com.example.orderfoodapp.R
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_filter_all_food.*

class FilterAllFoodFragment : Fragment() {

    private lateinit var dishAdapterAllFood: DishAdapter

    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_filter_all_food, container, false)
    }

    override fun onResume() {
        super.onResume()

        dishAdapterAllFood = DishAdapter(mutableListOf())
        allFood_recyclerView.adapter = dishAdapterAllFood

        val layoutManager = GridLayoutManager(context,2)
        allFood_recyclerView.layoutManager = layoutManager

        database = FirebaseDatabase.getInstance()
        ref = database.getReference("Product")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dishAdapterAllFood.deleteAll()
                for(data in snapshot.children) {
                    val dish = Dish(
                        data.child("id").value as String,
                        data.child("image").value as String,
                        data.child("name").value as String,
                        data.child("priceS").value as Double,
                        data.child("priceM").value as Double,
                        data.child("priceL").value as Double,
                        data.child("rated").value as Double,
                        data.child("deliveryTime").value as String,
                        data.child("category").value as String,
                        data.child("description").value as String,
                        data.child("salePercent").value as Long,
                        data.child("amount").value as Long,
                    )
                    dishAdapterAllFood.addDish(dish)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

}