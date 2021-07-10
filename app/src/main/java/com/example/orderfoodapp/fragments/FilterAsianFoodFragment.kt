package com.example.orderfoodapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.orderfoodapp.models.Dish
import com.example.orderfoodapp.adapters.DishAdapter
import com.example.orderfoodapp.R
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_filter_asia.*
import java.util.*

class FilterAsianFoodFragment : Fragment() {

    private lateinit var dishAdapterAsianFood: DishAdapter

    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference

    private lateinit var map: HashMap<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        map = HashMap()
        val bundle = this.arguments
        if (bundle != null) {
            map = bundle.getSerializable("map") as HashMap<String, String>
        }
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

        database = FirebaseDatabase.getInstance()
        ref = database.getReference("Product")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dishAdapterAsianFood.deleteAll()
                for(data in snapshot.children) {
                    if((data.child("category").value as String) == "Asian") {
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
                            dishAdapterAsianFood.addDish(dish)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

}