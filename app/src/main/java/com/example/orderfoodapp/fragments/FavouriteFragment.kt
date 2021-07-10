package com.example.orderfoodapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.orderfoodapp.models.Dish
import com.example.orderfoodapp.adapters.FavouriteAdapter
import com.example.orderfoodapp.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_favourite.*

class FavouriteFragment : Fragment() {

    private lateinit var favouriteAdapter: FavouriteAdapter
    private var customerEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favourite, container, false)
    }

    override fun onResume() {
        super.onResume()
        customerEmail = Firebase.auth.currentUser?.email.toString()

        favouriteAdapter = FavouriteAdapter(mutableListOf())
        favourite_recyclerView.adapter = favouriteAdapter

        val layoutManager = LinearLayoutManager(context)
        favourite_recyclerView.layoutManager = layoutManager

        val list: MutableList<String> = mutableListOf()

        val dbRef = FirebaseDatabase.getInstance().getReference("Favourite")
        dbRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    if(data.child("customerEmail").value as String == customerEmail) {
                        val dbRef2 = FirebaseDatabase.getInstance().getReference("Favourite/${data.key}/products")
                        dbRef2.get().addOnSuccessListener {
                            for(data2 in it.children) {
                                list.add(data2.value as String)
                            }
                            loadFav(list)
                        }
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun loadFav(list: MutableList<String>) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Product")
        dbRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                    if(list.contains(data.key.toString())) {
                        val dish = Dish(
                            data.child("id").value as String,
                            data.child("name").value as String,
                            data.child("priceS").value as Double,
                            data.child("priceM").value as Double,
                            data.child("priceL").value as Double,
                            data.child("rated").value as String,
                            "Closely",
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
                        favouriteAdapter.addFav(dish)
                    }
                }
                if(favouriteAdapter.itemCount == 0) {
                    empty_background.visibility = View.VISIBLE
                }
                else {
                    //set animation
                    val layoutAnim = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_anim_left_to_right)
                    favourite_recyclerView.layoutAnimation = layoutAnim
                    empty_background.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Cannot load favourite list!", Toast.LENGTH_LONG).show()
            }

        })
    }

}