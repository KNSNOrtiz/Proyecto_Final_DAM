package com.example.myanimection

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myanimection.adapters.RecyclerListedAnimeAdapter
import com.example.myanimection.controllers.UserController
import com.example.myanimection.models.ListedAnimeMedia
import com.example.myanimection.utils.GridSpacingItemDecorator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileAnimesFragment : Fragment() {

    private lateinit var rvListedAnimes: RecyclerView
    private var listedAnimes: ArrayList<ListedAnimeMedia> = arrayListOf()
    private val listedAnimesAdapter = RecyclerListedAnimeAdapter(listedAnimes)
    private val userController = UserController()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_animes, container, false)
        rvListedAnimes = view.findViewById(R.id.rvListedAnimes)
        rvListedAnimes.layoutManager = LinearLayoutManager(context)
        rvListedAnimes.adapter = listedAnimesAdapter
        rvListedAnimes.addItemDecoration(GridSpacingItemDecorator(1, 20, false))

        userController.getUserAnimes(Firebase.auth.currentUser!!.uid, UserController.ANIMELIST.WATCHING, object: UserController.FirestoreListedAnimesQueryCallback {
            override fun onQueryComplete(result: ArrayList<ListedAnimeMedia>) {
                listedAnimes.addAll(result)
                listedAnimesAdapter.notifyItemRangeInserted(0, listedAnimes.size-1)
                Log.d("ANIME QUERY RESULT", result.toString())
            }
            override fun onQueryFailure(exception: Exception) {
                Log.d("ANIME QUERY EXCEPTION", exception.message!!)
            }
        })

        return view
    }
}