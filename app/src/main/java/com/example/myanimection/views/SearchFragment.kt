package com.example.myanimection.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.myanimection.R
import com.example.myanimection.adapters.ViewPagerSearchAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/** Fragment que contiene las dos pestañas de búsqueda principales de la aplicación: una para animes, y otra para usuarios.
 */
class SearchFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        viewPager = view.findViewById(R.id.viewPagerSearch)
        tabLayout = view.findViewById(R.id.tabLayoutSearch)
        val mainActivity = requireActivity() as MainActivity

        //  Configuración de la vista de las pestañas de búsqueda de animes y usuarios.
        viewPager.adapter = ViewPagerSearchAdapter(mainActivity)
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    viewPager.currentItem = tab.position
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
        //  Unión del TabLayout con el ViewPager.
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when(position) {
                0 -> tab.text = "ANIME"
                1 -> tab.text = "USUARIOS"
            }
        }.attach()

        return view
    }

}