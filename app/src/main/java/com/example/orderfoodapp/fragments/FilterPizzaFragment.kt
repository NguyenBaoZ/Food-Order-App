package com.example.orderfoodapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.orderfoodapp.Dish
import com.example.orderfoodapp.DishAdapter
import com.example.orderfoodapp.R
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_filter_all_food.*
import kotlinx.android.synthetic.main.fragment_filter_all_food.allFood_recyclerView
import kotlinx.android.synthetic.main.fragment_filter_pizza.*


class FilterPizzaFragment : Fragment() {

    private lateinit var dishAdapterPizza: DishAdapter

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
        return inflater.inflate(R.layout.fragment_filter_pizza, container, false)
    }

    override fun onResume() {
        super.onResume()

        dishAdapterPizza = DishAdapter(mutableListOf())
        pizza_recyclerView.adapter = dishAdapterPizza

        val layoutManager = GridLayoutManager(context,2)
        pizza_recyclerView.layoutManager = layoutManager

        database = FirebaseDatabase.getInstance()
        ref = database.getReference("Product")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dishAdapterPizza.deleteAll()
                for(data in snapshot.children) {
                    if((data.child("category").value as String) == "Pizza") {
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
                        dishAdapterPizza.addDish(dish)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}