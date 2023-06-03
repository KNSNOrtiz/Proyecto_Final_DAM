package com.example.myanimection.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myanimection.views.SearchAnimeFragment
import com.example.myanimection.views.SearchUserFragment

class ViewPagerSearchAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SearchAnimeFragment()
            else -> SearchUserFragment()
        }
    }
}