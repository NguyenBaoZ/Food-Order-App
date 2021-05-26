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
import kotlinx.android.synthetic.main.fragment_filter_all_food.*
import kotlinx.android.synthetic.main.fragment_filter_all_food.allFood_recyclerView
import kotlinx.android.synthetic.main.fragment_filter_asia.*

class FilterAsianFoodFragment : Fragment() {

    private lateinit var dishAdapterAsianFood: DishAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_filter_asia, container, false)
    }

    override fun onResume() {
        super.onResume()

        dishAdapterAsianFood = DishAdapter(mutableListOf())
        asianFood_recyclerView.adapter = dishAdapterAsianFood

        val layoutManager = GridLayoutManager(context,2)
        asianFood_recyclerView.layoutManager = layoutManager

        //hardcore data
        val dish = Dish(R.drawable.img_shushi,"Shushi","4.7","13 min")
        for(i in 0 until 7) {
            dishAdapterAsianFood.addDish(dish)
        }
    }

}