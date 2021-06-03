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

class FilterAllFoodFragment : Fragment() {

    private lateinit var dishAdapterAllFood: DishAdapter

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


        //fake data
        val dish1 = Dish(R.drawable.img_hamburger,"Hamburger","4.6","15 min")
        val dish2 = Dish(R.drawable.img_alaska_lobster,"Alaska Lobster","5.0","30 min")
        val dish3 = Dish(R.drawable.img_pizza,"Pizza","4.5","20 min")
        val dish4 = Dish(R.drawable.img_peach_tea,"Peach tea","4.7","10 min")
        val dish5 = Dish(R.drawable.img_shushi,"Shushi","4.7","13 min")

        dishAdapterAllFood.addDish(dish1)
        dishAdapterAllFood.addDish(dish2)
        dishAdapterAllFood.addDish(dish3)
        dishAdapterAllFood.addDish(dish4)
        dishAdapterAllFood.addDish(dish5)
    }

}