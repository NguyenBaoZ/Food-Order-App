package com.example.orderfoodapp.fragments

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.orderfoodapp.R
import com.example.orderfoodapp.adapters.DishAdapter
import com.example.orderfoodapp.adapters.NewsAdapter
import com.example.orderfoodapp.fragments.MainMenuFragment.KotlinConstantClass.Companion.COMPANION_OBJECT_LIST_DISH
import com.example.orderfoodapp.models.Dish
import com.example.orderfoodapp.models.NewsItem
import com.example.orderfoodapp.others.EstimateTime
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.*
import com.schibstedspain.leku.*
import kotlinx.android.synthetic.main.fragment_main_menu.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainMenuFragment : Fragment() {
    private val PLACE_PICKER_REQUEST = 1

    private lateinit var dishAdapterNearestRestaurants: DishAdapter
    private lateinit var dishAdapterTopRating: DishAdapter
    private lateinit var dishAdapterOnSale: DishAdapter
    private lateinit var newsAdapter: NewsAdapter

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

    private var curLat = 10.8436
    private var curLon = 106.7716
    private var curAddress = ""
    private var providerLat = 0.0
    private var providerLon = 0.0

    private var listDish = ArrayList<Dish>()
    private lateinit var dialog: Dialog

    class KotlinConstantClass {
        companion object {
            var COMPANION_OBJECT_ADDRESS = ""
            var COMPANION_OBJECT_LIST_DISH = ArrayList<Dish>()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //show loading dialog
        dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.setContentView(R.layout.dialog_loading_menu)
        dialog.show()

        map = HashMap()
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(context as Activity)
        getLocation()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_menu, container, false)
    }

    override fun onResume() {
        super.onResume()

        location_textView.text = curAddress
        KotlinConstantClass.COMPANION_OBJECT_ADDRESS = curAddress

        //create adapter for nearestRestaurant_recyclerView
        dishAdapterNearestRestaurants = DishAdapter(mutableListOf())
        nearestRestaurants_recyclerView.adapter = dishAdapterNearestRestaurants

        //create adapter for trendingNow_recyclerView
        dishAdapterTopRating = DishAdapter(mutableListOf())
        topRating_recyclerView.adapter = dishAdapterTopRating

        //create adapter for onSale_recyclerView
        dishAdapterOnSale = DishAdapter(mutableListOf())
        onSale_recyclerView.adapter = dishAdapterOnSale

        //create adapter for news
        newsAdapter = NewsAdapter(mutableListOf())
        news_recyclerView.adapter = newsAdapter

        val layoutManager1 = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        nearestRestaurants_recyclerView.layoutManager = layoutManager1

        val layoutManager2 = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        topRating_recyclerView.layoutManager = layoutManager2

        val layoutManager3 = GridLayoutManager(context,2)
        onSale_recyclerView.layoutManager = layoutManager3

        val layoutManager4 = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
        news_recyclerView.layoutManager = layoutManager4

        showSlider()
        showNews()
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

        location_button.setOnClickListener() {
            showMap()
        }

    }

    private fun showMap() {
        val locationPickerIntent = LocationPickerActivity.Builder()
            .withLocation(curLat, curLon)
            .withGooglePlacesApiKey("AIzaSyCX19-6alLJB1jznKTALsmaWQ2FkoKutA8")
            .withSearchZone("vi-VN")
            .build(requireContext())

        startActivityForResult(locationPickerIntent, PLACE_PICKER_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == 1) {
                curLat = data.getDoubleExtra(LATITUDE, 0.0)
                curLon = data.getDoubleExtra(LONGITUDE, 0.0)
                curAddress = data.getStringExtra(LOCATION_ADDRESS).toString()
                if(curAddress.isEmpty())
                    convertLocationFromCoordination()
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
                listDish.clear()
                dishAdapterNearestRestaurants.deleteAll()
                dishAdapterTopRating.deleteAll()
                dishAdapterOnSale.deleteAll()
                for(data in snapshot.children) {
                    val prName = data.child("provider").value as String
                    if(map.containsKey(prName)) {
                        val deliveryTime = map[prName]
                        val dish = Dish(
                            data.child("id").value as String,
                            data.child("name").value as String,
                            data.child("priceS").value as Double,
                            data.child("priceM").value as Double,
                            data.child("priceL").value as Double,
                            data.child("rated").value as String,
                            deliveryTime!!,
                            data.child("category").value as String,
                            data.child("description").value as String,
                            data.child("salePercent").value as Long,
                            data.child("provider").value as String,
                            data.child("amountS").value as Long,
                            data.child("amountSsold").value as Long,
                            data.child("amountM").value as Long,
                            data.child("amountMsold").value as Long,
                            data.child("amountL").value as Long,
                            data.child("amountLsold").value as Long,
                        )
                        listDish.add(dish)
                    }
                }
                loadDataNearestRestaurant()
                loadDataTopRating()
                loadDataOnSale()

                //copy value from listDish
                COMPANION_OBJECT_LIST_DISH = listDish

                if(dialog.isShowing) {
                    dialog.dismiss()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun getLocation() {
        val task = fusedLocationProvider.lastLocation
        if (ActivityCompat.checkSelfPermission(
                context as Activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context as Activity,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }
        task.addOnSuccessListener {
            val location = it
            if(location != null) {
                val geocoder = Geocoder(context, Locale.getDefault())
                val address: List<Address> = geocoder.getFromLocation(
                    location.latitude, location.longitude, 1
                )
                curLat = address[0].latitude
                curLon = address[0].longitude
                curAddress = address[0].getAddressLine(0)
                location_textView.text = curAddress
                KotlinConstantClass.COMPANION_OBJECT_ADDRESS = curAddress
            }
            else {
                Toast.makeText(context, "Cannot get current location! Using default...", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun convertLocationFromAddress(myLocation: String) {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses: List<Address> = geocoder.getFromLocationName(myLocation, 1)
            val address: Address = addresses[0]
            providerLat = address.latitude
            providerLon = address.longitude
        }
        catch (e: Exception) {
            Toast.makeText(context, "Issue with gps, try later!", Toast.LENGTH_LONG).show()
        }
    }

    private fun convertLocationFromCoordination() {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses: List<Address> = geocoder.getFromLocation(curLat, curLon, 1)
            val address = addresses[0].getAddressLine(0)
            curAddress = address
        }
        catch (e: Exception) {
            Toast.makeText(context, "Issue with gps, try later!", Toast.LENGTH_LONG).show()
        }
    }

    private fun estimateTime() {
        val dbRef = FirebaseDatabase.getInstance().getReference("Provider")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    val prLocation = data.child("location").value as String
                    convertLocationFromAddress(prLocation)
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

    private fun loadDataNearestRestaurant() {
        for(item in listDish) {
            if(item.deliveryTime == "Closely") {
                dishAdapterNearestRestaurants.addDish(item)
            }
        }
    }

    private fun loadDataTopRating() {
        for(item in listDish) {
            dishAdapterTopRating.addDishTopRating(item)
        }
    }

    private fun loadDataOnSale() {
        for(item in listDish) {
            if(item.salePercent != 0.toLong()) {
                dishAdapterOnSale.addDish(item)
            }
        }
    }

    private fun showNews() {
        val news1 = NewsItem(
            R.drawable.news1,
            "Best local Burger contest",
            "Just come here and vote for your\nfavourite burger restaurant to win\nvarious prize sponsored by M.T.L Restaurant"
        )

        val news2 = NewsItem(
            R.drawable.news2,
            "Decoding FAD-FOOD-NEWS",
            "How do we make educated choices\nabout which voices to listen to?\nSometimes, it can be very important"
        )

        val news3 = NewsItem(
            R.drawable.news3,
            "World food safety day",
            "You're a food lover? You don't want to\nmiss any hot news about cuisine all\naround the world? Come here!"
        )

        val news4 = NewsItem(
            R.drawable.news4,
            "Best food news websites",
            "Every one has a right to safe, healthy\nand nutritious food, but not everyone\nknow how to do that correctly"
        )

        newsAdapter.addNews(news1)
        newsAdapter.addNews(news2)
        newsAdapter.addNews(news3)
        newsAdapter.addNews(news4)
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