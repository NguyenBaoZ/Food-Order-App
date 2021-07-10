package com.example.orderfoodapp.adapters

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.orderfoodapp.fragments.LoginFragment
import com.example.orderfoodapp.fragments.SignUpFragment
import org.jetbrains.annotations.NotNull

class LoginAdapter(
    val items: ArrayList<Fragment>,
    activity: AppCompatActivity
): FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return items[position]
    }


}