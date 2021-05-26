package com.example.orderfoodapp.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.orderfoodapp.Dish
import com.example.orderfoodapp.DishAdapter
import com.example.orderfoodapp.R
import kotlinx.android.synthetic.main.fragment_main_menu.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MainMenuFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var dishAdapterNearestRestaurants: DishAdapter
    private lateinit var dishAdapterTrendingNow: DishAdapter

    private val filterAllFoodFragment = FilterAllFoodFragment()
    private val filterPizzaFragment = FilterPizzaFragment()
    private val filterDrinkFragment = FilterDrinkFragment()
    private val filterAsianFoodFragment = FilterAsianFoodFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_menu, container, false)
    }

    override fun onResume() {
        super.onResume()

        //create adapter for nearestRestaurant_recyclerView
        dishAdapterNearestRestaurants = DishAdapter(mutableListOf())
        nearestRestaurants_recyclerView.adapter = dishAdapterNearestRestaurants

        //create adapter for trendingNow_recyclerView
        dishAdapterTrendingNow = DishAdapter(mutableListOf())
        trendingNow_recyclerView.adapter = dishAdapterTrendingNow

        val layoutManager1 = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        nearestRestaurants_recyclerView.layoutManager = layoutManager1

        val layoutManager2 = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        trendingNow_recyclerView.layoutManager = layoutManager2

        //temporary data for nearestRestaurants_recyclerView
        val itemNearestRestaurants = Dish(R.drawable.img_hamburger,"Hamburger","4.6","15 min")
        for(i in 0 until 7) {
            dishAdapterNearestRestaurants.addDish(itemNearestRestaurants)
        }

        //temporary data for trendingNow_recyclerView
        val itemTrendingNow = Dish(R.drawable.img_alaska_lobster,"Alaska Lobster","5.0","30 min")
        for(i in 0 until 7) {
            dishAdapterTrendingNow.addDish(itemTrendingNow)
        }


        filter_button.setOnClickListener {
            filterOnClick()
        }

        allFood_button.setOnClickListener {
            categoriesColorOnClick(it)
            replaceFragment(filterAllFoodFragment)
            filter_container_inside.visibility = View.GONE
        }

        pizza_button.setOnClickListener {
            categoriesColorOnClick(it)
            replaceFragment(filterPizzaFragment)
            filter_container_inside.visibility = View.GONE
        }

        beverages_button.setOnClickListener {
            categoriesColorOnClick(it)
            replaceFragment(filterDrinkFragment)
            filter_container_inside.visibility = View.GONE
        }

        asianFood_button.setOnClickListener {
            categoriesColorOnClick(it)
            replaceFragment(filterAsianFoodFragment)
            filter_container_inside.visibility = View.GONE
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.filter_container, fragment)
        transaction.commit()
    }

    private fun filterOnClick() {
        if(filter_layout.visibility == View.VISIBLE)
            filter_layout.visibility = View.GONE
        else
            filter_layout.visibility = View.VISIBLE
    }

    private fun categoriesColorOnClick(view: View) {

        resetCategoriesColor()

        when(view.id) {

            R.id.allFood_button -> {
                allFood_button.setBackgroundResource(R.drawable.bg_borderless_orange)
                allFood_icon.setColorFilter(Color.WHITE)
                allFood_textView.setTextColor(Color.parseColor("#FF8526"))
            }

            R.id.pizza_button -> {
                pizza_button.setBackgroundResource(R.drawable.bg_borderless_orange)
                pizza_icon.setColorFilter(Color.WHITE)
                pizza_textView.setTextColor(Color.parseColor("#FF8526"))
            }

            R.id.beverages_button -> {
                beverages_button.setBackgroundResource(R.drawable.bg_borderless_orange)
                beverages_icon.setColorFilter(Color.WHITE)
                beverages_textView.setTextColor(Color.parseColor("#FF8526"))
            }

            R.id.asianFood_button -> {
                asianFood_button.setBackgroundResource(R.drawable.bg_borderless_orange)
                asianFood_icon.setColorFilter(Color.WHITE)
                asianFood_textView.setTextColor(Color.parseColor("#FF8526"))
            }
        }
    }

    //this function reset all category buttons back to the original color (gray)
    private fun resetCategoriesColor() {

        allFood_button.setBackgroundResource(R.drawable.bg_borderless_edit_text)
        allFood_icon.setColorFilter(Color.parseColor("#838383"))
        allFood_textView.setTextColor(Color.parseColor("#838383"))

        pizza_button.setBackgroundResource(R.drawable.bg_borderless_edit_text)
        pizza_icon.setColorFilter(Color.parseColor("#838383"))
        pizza_textView.setTextColor(Color.parseColor("#838383"))

        beverages_button.setBackgroundResource(R.drawable.bg_borderless_edit_text)
        beverages_icon.setColorFilter(Color.parseColor("#838383"))
        beverages_textView.setTextColor(Color.parseColor("#838383"))

        asianFood_button.setBackgroundResource(R.drawable.bg_borderless_edit_text)
        asianFood_icon.setColorFilter(Color.parseColor("#838383"))
        asianFood_textView.setTextColor(Color.parseColor("#838383"))
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainMenuFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainMenuFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}