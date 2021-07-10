package com.example.orderfoodapp.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.orderfoodapp.fragments.SignUpFragment
import org.jetbrains.annotations.NotNull

class LoginAdapter(@NotNull fm: FragmentManager,behavior: Int): FragmentStatePagerAdapter(fm) {

    override fun getCount(): Int {
        return 2
    }

    @NotNull
    override fun getItem(position: Int): Fragment {
        if(position == 0)
            return com.example.orderfoodapp.fragments.LoginFragment()
        else
            return SignUpFragment()
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return if(position == 0)
            "Login"
        else
            "Signup"
    }
}