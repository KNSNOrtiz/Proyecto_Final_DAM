package com.example.myanimection.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myanimection.views.ProfileAnimesFragment
import com.example.myanimection.views.ProfileReviewsFragment

class ViewPagerProfileAdapter(fragmentActivity: FragmentActivity, private val uidUserToLoad: String) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ProfileAnimesFragment(uidUserToLoad)
            else -> ProfileReviewsFragment(uidUserToLoad)
        }
    }
}