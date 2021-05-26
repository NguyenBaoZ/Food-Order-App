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
import kotlinx.android.synthetic.main.fragment_drink.*
import kotlinx.android.synthetic.main.fragment_filter_all_food.*
import kotlinx.android.synthetic.main.fragment_filter_all_food.allFood_recyclerView

class FilterDrinkFragment : Fragment() {

    private lateinit var dishAdapterDrink: DishAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_drink, container, false)
    }

    override fun onResume() {
        super.onResume()

        dishAdapterDrink = DishAdapter(mutableListOf())
        drink_recyclerView.adapter = dishAdapterDrink

        val layoutManager = GridLayoutManager(context,2)
        drink_recyclerView.layoutManager = layoutManager

        //hardcore data
        val dish = Dish(R.drawable.img_peach_tea,"Peach tea","4.7","10 min")
        for(i in 0 until 7) {
            dishAdapterDrink.addDish(dish)
        }
    }

}