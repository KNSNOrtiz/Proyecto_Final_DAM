package com.example.myanimection

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.widget.ViewPager2
import coil.load
import coil.transform.CircleCropTransformation
import com.example.myanimection.adapters.ViewPagerProfileAdapter
import com.example.myanimection.controllers.UserController
import com.example.myanimection.views.MainActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private val userController = UserController()
    private lateinit var imgProfilePic: ImageView
    private lateinit var txtDisplayName: TextView
    private lateinit var txtEmail: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).supportActionBar?.hide()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        imgProfilePic = view.findViewById(R.id.imgProfilePicture)
        txtDisplayName = view.findViewById(R.id.txtProfileDisplayName)
        txtEmail = view.findViewById(R.id.txtProfileEmail)
        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewpagerProfile)
        viewPager.adapter = ViewPagerProfileAdapter(this.requireActivity())
        tabLayout.addOnTabSelectedListener(object: OnTabSelectedListener {
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

        userController.getUserProfilePic().downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                imgProfilePic.load(it.result) {
                    transformations(CircleCropTransformation())
                }
            }
        }
        txtDisplayName.text = Firebase.auth.currentUser?.displayName
        txtEmail.text = Firebase.auth.currentUser?.email
        return view
    }

    private fun setupMenu() {
        (requireActivity() as MainActivity).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {

            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }


}