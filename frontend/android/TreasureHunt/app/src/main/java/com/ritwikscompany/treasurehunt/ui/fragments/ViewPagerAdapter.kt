package com.ritwikscompany.treasurehunt.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(fm: FragmentManager, numOfTabs: Int) : FragmentPagerAdapter(fm) {
    var numOfTabs: Int = numOfTabs

    override fun getCount(): Int {
        return this.numOfTabs
    }


    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                CreateGroupFragment()
            }

            1 -> {
                JoinGroupFragment()
            }

            else -> {
                MyGroupFragment()
            }
        }
    }
}