package com.example.myanimection

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myanimection.adapters.RecyclerListedAnimeAdapter
import com.example.myanimection.controllers.UserController
import com.example.myanimection.models.AnimeCategory
import com.example.myanimection.models.ListedAnimeMedia
import com.example.myanimection.utils.GridSpacingItemDecorator
import com.example.myanimection.utils.Utilities
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileAnimesFragment : Fragment() {

    private lateinit var spinListedAnimeCategories: Spinner
    private lateinit var rvListedAnimes: RecyclerView
    private lateinit var btnRefresh: ImageButton
    private var listedAnimes: ArrayList<ListedAnimeMedia> = arrayListOf()
    private val listedAnimesAdapter = RecyclerListedAnimeAdapter(listedAnimes)
    private val userController = UserController()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_animes, container, false)
        spinListedAnimeCategories = view.findViewById(R.id.spinListedAnimeCategories)
        rvListedAnimes = view.findViewById(R.id.rvListedAnimes)
        btnRefresh = view.findViewById(R.id.btnListedAnimeRefresh)
        rvListedAnimes.layoutManager = LinearLayoutManager(context)
        listedAnimesAdapter.categoryChangedListener = object : RecyclerListedAnimeAdapter.CategoryChangedListener {
            override fun notifyRecyclerView() {
                refreshList()
            }
        }
        rvListedAnimes.adapter = listedAnimesAdapter
        rvListedAnimes.addItemDecoration(GridSpacingItemDecorator(1, 20, false))

        ArrayAdapter.createFromResource(view.context, R.array.listed_categories, R.layout.ani_spin_item).also { adapter ->
            adapter.setDropDownViewResource(R.layout.ani_spin_dropdown_item)
            spinListedAnimeCategories.adapter = adapter
        }


        spinListedAnimeCategories.onItemSelectedListener = object: OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                refreshList()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        btnRefresh.setOnClickListener { refreshList() }
        return view
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }


    fun refreshList() {
        val category = enumValueOf<AnimeCategory>(spinListedAnimeCategories.selectedItem.toString())
        userController.getUserAnimes(Firebase.auth.currentUser!!.uid, category, object: UserController.FirestoreListedAnimesQueryCallback {
            override fun onQueryComplete(result: ArrayList<ListedAnimeMedia>) {
                Log.d("Animes listados", "Animes listados")
                rvListedAnimes.recycledViewPool.clear()
                val previousSize = listedAnimesAdapter.itemCount
                listedAnimes.clear()
                listedAnimesAdapter.notifyItemRangeRemoved(0, previousSize)
                listedAnimes.addAll(result)
                listedAnimesAdapter.notifyDataSetChanged()
                Log.d("ANIME QUERY RESULT", listedAnimes.toString())
            }
            override fun onQueryFailure(exception: Exception) {
                Log.d("ANIME QUERY EXCEPTION", exception.message!!)
            }
        })
    }
}