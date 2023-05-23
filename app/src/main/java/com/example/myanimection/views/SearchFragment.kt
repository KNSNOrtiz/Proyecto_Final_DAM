package com.example.myanimection.views

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo3.api.Optional
import com.example.myanimection.R
import com.example.myanimection.SearchAnimesQuery
import com.example.myanimection.adapters.RecyclerAnimeMediaAdapter
import com.example.myanimection.controllers.AnimeMediaController
import com.example.myanimection.models.AnimeMedia
import com.example.myanimection.repositories.AnimeMediaRepository
import com.example.myanimection.utils.GridSpacingItemDecorator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class SearchFragment : Fragment() {

    private val myAnimeList = ArrayList<AnimeMedia?>()
    private val animeMediaController = AnimeMediaController(AnimeMediaRepository())
    private val animeMediaAdapter = RecyclerAnimeMediaAdapter(ArrayList<AnimeMedia?>())

    private lateinit var txtSearch: TextView
    private lateinit var rvSearchAnime: RecyclerView
    private lateinit var spinGenre1: Spinner
    private lateinit var spinGenre2: Spinner
    private lateinit var spinGenre3: Spinner
    private lateinit var btnFilter: Button

    private var currentPage = 1
    private var totalPages = 0
    private var nextPage = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        (activity as MainActivity).supportActionBar?.hide()
        txtSearch = view.findViewById(R.id.txtSearch)
        spinGenre1 = view.findViewById(R.id.spinGenre1)
        spinGenre2= view.findViewById(R.id.spinGenre2)
        spinGenre3 = view.findViewById(R.id.spinGenre3)
        btnFilter = view.findViewById(R.id.btnSearchFilter)
        ArrayAdapter.createFromResource(view.context, R.array.genres, android.R.layout.simple_spinner_item).also { adapter ->
            adapter.setDropDownViewResource(com.google.android.material.R.layout.support_simple_spinner_dropdown_item)
            spinGenre1.adapter = adapter
            spinGenre2.adapter = adapter
            spinGenre3.adapter = adapter
        }
        btnFilter.setOnClickListener { launchSearchQuery(true) }

        rvSearchAnime = view.findViewById(R.id.rvSearchAnime)
        rvSearchAnime.adapter = animeMediaAdapter
        rvSearchAnime.addItemDecoration(GridSpacingItemDecorator(2, 30, false))
        rvSearchAnime.layoutManager = GridLayoutManager(context, 2)
        txtSearch.addTextChangedListener (object: TextWatcher {
            private val DELAY_MILLIS = 500L
            private var timer: Timer? = null

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                timer?.cancel()
                timer = Timer()
                timer?.schedule(object : TimerTask() {
                    override fun run() {
                        if (s?.length!! > 0){
                            launchSearchQuery(false)
                        }
                    }
                }, DELAY_MILLIS)
            }

        })
        return view
    }

    private fun launchSearchQuery(filter: Boolean) = lifecycleScope.launch(Dispatchers.IO) {
        myAnimeList.clear()
        var genresFilter = mutableListOf("${spinGenre1.selectedItem}", "${spinGenre2.selectedItem}","${spinGenre3.selectedItem}")
        genresFilter = genresFilter.filter { genre ->  !genre.equals("Sin género")}.toMutableList()
        val response: SearchAnimesQuery.Page?
        if (genresFilter.isNotEmpty()) {
            if (txtSearch.text.trim().toString().isNotEmpty()) {
                Log.d("Genres filter", genresFilter.toString())
                response = animeMediaController.getSearchAnimes(Optional.present(txtSearch.text.trim().toString()), Optional.present(genresFilter))
            } else {
                response = animeMediaController.getSearchAnimes(Optional.absent(), Optional.present(genresFilter))
            }
        } else {
            response = animeMediaController.getSearchAnimes(Optional.present(txtSearch.text.trim().toString()), Optional.absent())
        }
        Log.d("Response Search", response.toString())
        totalPages = response?.pageInfo?.lastPage!!
        nextPage = response.pageInfo.hasNextPage!!
        response.media?.forEach {
            if (it != null) {
                myAnimeList.add(
                    AnimeMedia(
                        it.id,
                        it.title?.romaji.toString(),
                        it.title?.native.toString(),
                        it.coverImage?.large.toString(),
                        it.genres
                    )
                )
            }
        }
        refreshRecyclerView()
    }

    private fun refreshRecyclerView() = lifecycleScope.launch(Dispatchers.Main) {
        rvSearchAnime.recycledViewPool.clear()
        val previousSize = animeMediaAdapter.itemCount
        animeMediaAdapter.data.clear()
        animeMediaAdapter.notifyItemRangeRemoved(0, previousSize)
        animeMediaAdapter.data.addAll(myAnimeList)
        animeMediaAdapter.notifyItemRangeInserted(0, animeMediaAdapter.itemCount)
        }

}