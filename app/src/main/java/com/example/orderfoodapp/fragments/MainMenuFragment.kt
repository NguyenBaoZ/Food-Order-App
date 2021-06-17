package com.example.orderfoodapp.fragments

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.orderfoodapp.Dish
import com.example.orderfoodapp.DishAdapter
import com.example.orderfoodapp.EstimateTime
import com.example.orderfoodapp.R
import com.google.android.gms.location.*
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_main_menu.*
import java.util.*
import kotlin.collections.HashMap


class MainMenuFragment : Fragment() {

    private lateinit var dishAdapterNearestRestaurants: DishAdapter
    private lateinit var dishAdapterTrendingNow: DishAdapter

    private val filterAllFoodFragment = FilterAllFoodFragment()
    private val filterWesternFragment = FilterWesternFragment()
    private val filterDrinkFragment = FilterDrinkFragment()
    private val filterAsianFoodFragment = FilterAsianFoodFragment()
    private var searchFragment = SearchFragment()
    private var curFragment = Fragment()

    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference

    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private lateinit var map: HashMap<String, String>

    private var curLat = 0.0
    private var curLon = 0.0
    private var providerLat = 0.0
    private var providerLon = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        map = HashMap()
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(context as Activity)
        getLocation()
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

        showSlider()

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

        estimateTime()

        filter_button.setOnClickListener {
            filterOnClick()
        }

        allFood_button.setOnClickListener {
            curFragment = filterAllFoodFragment
            categoriesColorOnClick(it)
            replaceFragment(filterAllFoodFragment)
            filter_container_inside.visibility = View.GONE
        }

        western_button.setOnClickListener {
            curFragment = filterWesternFragment
            categoriesColorOnClick(it)
            replaceFragment(filterWesternFragment)
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
                replaceFragment(searchFragment, search_editText.text.toString())

                resetCategoriesColor()
                search_editText.clearFocus()
                filter_container_inside.visibility = View.GONE
            }
            else {
                Toast.makeText(context, "Please enter the food's name", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun replaceFragment(fragment: Fragment, searchText: String = "") {
        if(searchText.isEmpty()) {
            val bundle = Bundle()
            bundle.putSerializable("map", map)
            fragment.arguments = bundle
        }
        else {
            val bundle = Bundle()
            bundle.putString("searchText", searchText)
            bundle.putSerializable("map", map)
            fragment.arguments = bundle
        }
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.filter_container, fragment)
        transaction.commit()
    }

    private fun filterOnClick() {
        if(filter_layout.visibility == View.VISIBLE) {
            if( curFragment == filterAllFoodFragment ||
                curFragment == filterWesternFragment ||
                curFragment == filterDrinkFragment ||
                curFragment == filterAsianFoodFragment ||
                curFragment == searchFragment) {
                childFragmentManager.beginTransaction().remove(curFragment).commit()
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

            R.id.western_button -> {
                western_button.setBackgroundResource(R.drawable.bg_borderless_orange)
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

        western_button.setBackgroundResource(R.drawable.bg_borderless_edit_text)
        pizza_icon.setColorFilter(Color.parseColor("#838383"))
        pizza_textView.setTextColor(Color.parseColor("#838383"))

        beverages_button.setBackgroundResource(R.drawable.bg_borderless_edit_text)
        beverages_icon.setColorFilter(Color.parseColor("#838383"))
        beverages_textView.setTextColor(Color.parseColor("#838383"))

        asianFood_button.setBackgroundResource(R.drawable.bg_borderless_edit_text)
        asianFood_icon.setColorFilter(Color.parseColor("#838383"))
        asianFood_textView.setTextColor(Color.parseColor("#838383"))
    }

    private fun loadData() {
        database = FirebaseDatabase.getInstance()
        ref = database.getReference("Product")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dishAdapterNearestRestaurants.deleteAll()
                dishAdapterTrendingNow.deleteAll()
                for(data in snapshot.children) {
                    val prName = data.child("provider").value as String
                    if(map.containsKey(prName)) {
                        val deliveryTime = map[prName]
                        val dish = Dish(
                            data.child("id").value as String,
                            data.child("image").value as String,
                            data.child("name").value as String,
                            data.child("priceS").value as Double,
                            data.child("priceM").value as Double,
                            data.child("priceL").value as Double,
                            data.child("rated").value as Double,
                            deliveryTime!!,
                            data.child("category").value as String,
                            data.child("description").value as String,
                            data.child("salePercent").value as Long,
                            data.child("amount").value as Long,
                        )
                        dishAdapterNearestRestaurants.addDish(dish)
                        dishAdapterTrendingNow.addDish(dish)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                context as Activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context as Activity,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProvider.lastLocation.addOnCompleteListener {
            val location = it.result
            if(location != null) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val address: List<Address> = geocoder.getFromLocation(
                    location.latitude, location.longitude, 1
                )
                curLat = address[0].latitude
                curLon = address[0].longitude
                location_textView.text = address[0].getAddressLine(0)
            }
            else {
                Toast.makeText(context, "Cannot get current location!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun convertLocation(myLocation: String) {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses: List<Address> = geocoder.getFromLocationName(myLocation, 1)
            val address: Address = addresses[0]
            providerLat = address.latitude
            providerLon = address.longitude
        }
        catch (e: Exception) {
            Log.i("exception", e.printStackTrace().toString())
        }
    }

    private fun estimateTime() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Provider")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    val prLocation = data.child("location").value as String
                    convertLocation(prLocation)
                    val est = EstimateTime()
                    val deliveryTime = est.estimateTime(curLat, curLon, providerLat, providerLon)
                    map[data.key.toString()] = deliveryTime
                }
                loadData()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun showSlider() {
        val imageList = ArrayList<SlideModel>()
        imageList.add(SlideModel(R.drawable.banner1, ScaleTypes.CENTER_CROP))
        imageList.add(SlideModel(R.drawable.banner2))
        imageList.add(SlideModel(R.drawable.banner3, ScaleTypes.CENTER_CROP))
        imageList.add(SlideModel(R.drawable.banner4))

        banner_slider.setImageList(imageList)
    }

}