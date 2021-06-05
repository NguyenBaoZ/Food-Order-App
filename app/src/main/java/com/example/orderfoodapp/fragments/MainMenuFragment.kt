package com.example.orderfoodapp.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.orderfoodapp.Dish
import com.example.orderfoodapp.DishAdapter
import com.example.orderfoodapp.R
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_main_menu.*

class MainMenuFragment : Fragment() {

    private lateinit var dishAdapterNearestRestaurants: DishAdapter
    private lateinit var dishAdapterTrendingNow: DishAdapter

    private val filterAllFoodFragment = FilterAllFoodFragment()
    private val filterPizzaFragment = FilterPizzaFragment()
    private val filterDrinkFragment = FilterDrinkFragment()
    private val filterAsianFoodFragment = FilterAsianFoodFragment()
    private var searchFragment = SearchFragment()
    private var curFragment = Fragment()

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

        database = FirebaseDatabase.getInstance()
        ref = database.getReference("Product")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
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
                    dishAdapterNearestRestaurants.addDish(dish)
                    dishAdapterTrendingNow.addDish(dish)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


        filter_button.setOnClickListener {
            filterOnClick()
        }

        allFood_button.setOnClickListener {
            curFragment = filterAllFoodFragment
            categoriesColorOnClick(it)
            replaceFragment(filterAllFoodFragment)
            filter_container_inside.visibility = View.GONE
        }

        pizza_button.setOnClickListener {
            curFragment = filterPizzaFragment
            categoriesColorOnClick(it)
            replaceFragment(filterPizzaFragment)
            filter_container_inside.visibility = View.GONE
        }

        beverages_button.setOnClickListener {
            curFragment = filterDrinkFragment
            categoriesColorOnClick(it)
            replaceFragment(filterDrinkFragment)
            filter_container_inside.visibility = View.GONE
        }

        asianFood_button.setOnClickListener {
            curFragment = filterAsianFoodFragment
            categoriesColorOnClick(it)
            replaceFragment(filterAsianFoodFragment)
            filter_container_inside.visibility = View.GONE
        }

        search_button.setOnClickListener() {


            if(searchFragment.isAdded || search_editText.text.isNotEmpty()) {
                searchFragment = SearchFragment()
                curFragment = searchFragment
            }
            else
                Toast.makeText(context, "Please enter the food's name", Toast.LENGTH_LONG).show()

            if(search_editText.text.isNotEmpty()) {
                val bundle = Bundle()
                bundle.putString("searchText", search_editText.text.toString())
                searchFragment.arguments = bundle
                replaceFragment(searchFragment)

                resetCategoriesColor()
                search_editText.clearFocus()
                filter_container_inside.visibility = View.GONE
            }
            else {
                Toast.makeText(context, "Please enter the food's name", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.filter_container, fragment)
        transaction.commit()
    }

    private fun filterOnClick() {
        if(filter_layout.visibility == View.VISIBLE) {
            if( curFragment == filterAllFoodFragment ||
                curFragment == filterPizzaFragment ||
                curFragment == filterDrinkFragment ||
                curFragment == filterAsianFoodFragment ) {
                childFragmentManager.beginTransaction().hide(curFragment).commit()
            }
            resetCategoriesColor()
            search_editText.text.clear()
            filter_layout.visibility = View.GONE
            filter_container_inside.visibility = View.VISIBLE
        }
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

}