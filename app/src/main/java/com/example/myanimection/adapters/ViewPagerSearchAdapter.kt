package com.example.myanimection.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myanimection.views.SearchAnimeFragment
import com.example.myanimection.views.SearchUserFragment

/** Adaptador para el ViewPager que muestra los fragments en la pantalla de búsqueda.
 * @param fragmentActivity La actividad que contiene el ViewPager.
 */
class ViewPagerSearchAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 2
    }

    /** Crea y devuelve el fragment que corresponde a la posición del TabLayout asociado.
     * @param position La posición del fragmento.
     * @return El fragment creado.
     */
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SearchAnimeFragment()
            else -> SearchUserFragment()
        }
    }
}