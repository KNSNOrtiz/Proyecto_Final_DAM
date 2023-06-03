package com.example.myanimection.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myanimection.views.ProfileAnimesFragment
import com.example.myanimection.views.ProfileReviewsFragment

/** Adaptador para el ViewPager que muestra los fragments en el perfil del usuario.
 * @param fragmentActivity La actividad que contiene el ViewPager.
 * @param uidUserToLoad El UID del usuario cuyo perfil se está visitando.
 */
class ViewPagerProfileAdapter(fragmentActivity: FragmentActivity, private val uidUserToLoad: String) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 2
    }
    /** Crea y devuelve el fragment que corresponde a la posición del TabLayout asociado.
     * @param position La posición del fragmento.
     * @return El fragment creado.
     */
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ProfileAnimesFragment(uidUserToLoad)
            else -> ProfileReviewsFragment(uidUserToLoad)
        }
    }
}