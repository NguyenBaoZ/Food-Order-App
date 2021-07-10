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
import kotlinx.android.synthetic.main.fragment_search.*
import java.util.HashMap

class SearchFragment : Fragment() {

    private lateinit var dishAdapterSearch: DishAdapter

    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference

    private lateinit var searchText: String
    private lateinit var map: HashMap<String, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        map = HashMap()
        val bundle = this.arguments
        if (bundle != null) {
            searchText = bundle.getString("searchText").toString()
            map = bundle.getSerializable("map") as HashMap<String, String>
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onResume() {
        super.onResume()

        dishAdapterSearch = DishAdapter(mutableListOf())
        search_recyclerView.adapter = dishAdapterSearch

        val layoutManager = GridLayoutManager(context,2)
        search_recyclerView.layoutManager = layoutManager

        var count = 0

        database = FirebaseDatabase.getInstance()
        ref = database.getReference("Product")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dishAdapterSearch.deleteAll()
                for(data in snapshot.children) {
                    if((data.child("name").value as String).contains(searchText)) {
                        val prName = data.child("provider").value as String
                        if(map.containsKey(prName)) {
                            count++
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
                            dishAdapterSearch.addDish(dish)
                        }
                    }
                }
                numSearch_textView.text = "Result found: $count"
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}