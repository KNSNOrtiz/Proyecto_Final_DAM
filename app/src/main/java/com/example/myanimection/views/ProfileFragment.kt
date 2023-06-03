package com.example.myanimection.views

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import coil.load
import coil.transform.CircleCropTransformation
import com.example.myanimection.R
import com.example.myanimection.adapters.ViewPagerProfileAdapter
import com.example.myanimection.controllers.FirestoreQueryCallback
import com.example.myanimection.controllers.UserController
import com.example.myanimection.models.User
import com.example.myanimection.utils.Notifications
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.GONE
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ProfileFragment : Fragment() {

    private val userController = UserController()
    private  var selectedImageUri: Uri? = null
    private lateinit var imageChooseLauncher: ActivityResultLauncher<Intent>
    private var profileUID = ""
    private lateinit var userData: User
    private lateinit var imgProfilePic: ImageView
    private lateinit var btnChangePic: Button
    private lateinit var txtDisplayName: TextView
    private lateinit var txtEmail: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageChooseLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK ) {
                selectedImageUri = result.data?.data
                if (profileUID.isNotEmpty() && selectedImageUri != null) {
                    userController.setUserProfilePic(profileUID, selectedImageUri!!, object: FirestoreQueryCallback {
                        override fun onQueryComplete(success: Boolean) {
                            if (success) {
                                Notifications.shortToast(view!!.context, "Imagen actualizada.")
                                loadUserPic()
                            } else {
                                Notifications.shortToast(view!!.context, "No se pudo cambiar la imagen.")
                            }
                        }
                        override fun onQueryFailure(exception: Exception) {
                            Notifications.shortToast(view!!.context, "Error al subir la imagen.")
                        }
                    })
                }
             }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mainActivity = requireActivity() as MainActivity
        profileUID = arguments?.getString("uid", "") ?: Firebase.auth.currentUser!!.uid
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        imgProfilePic = view.findViewById(R.id.imgProfilePicture)
        btnChangePic = view.findViewById(R.id.btnProfileChangePicture)
        txtDisplayName = view.findViewById(R.id.txtProfileDisplayName)
        txtEmail = view.findViewById(R.id.txtProfileEmail)
        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewpagerProfile)

        btnChangePic.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            imageChooseLauncher.launch(intent)
        }
        if (Firebase.auth.currentUser != null ) {
            if (profileUID != Firebase.auth.currentUser!!.uid) {
                btnChangePic.visibility = View.GONE
            }
        }

        viewPager.adapter = ViewPagerProfileAdapter(mainActivity, profileUID)
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

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when(position) {
                0 -> tab.text = "ANIME"
                1 -> tab.text = "RESEÑAS"
            }
        }.attach()

        if (profileUID.isNotEmpty()) {
            userController.getUser(profileUID, object: UserController.UserQueryCallback {
                override fun onQueryComplete(result: User) {
                    userData = result
                    txtDisplayName.text = userData.userName
                    txtEmail.text = userData.email
                }
                override fun onQueryFailure(exception: Exception) {
                    Log.e("PROFILE", exception.message.toString())
                }
            })

            loadUserPic()
        }
        return view
    }

    private fun loadUserPic() {
        if (profileUID.isNotEmpty()) {
            userController.getUserProfilePic(profileUID).downloadUrl.addOnCompleteListener {
                if (it.isSuccessful) {
                    imgProfilePic.load(it.result) {
                        transformations(CircleCropTransformation())
                        error(R.drawable.ic_profile)
                        placeholder(R.drawable.ic_profile)
                    }
                } else {
                    imgProfilePic.load(R.drawable.ic_profile) {
                        transformations(CircleCropTransformation())
                        error(R.drawable.ic_profile)
                        placeholder(R.drawable.ic_profile)
                    }
                }
            }
        }

    }



}