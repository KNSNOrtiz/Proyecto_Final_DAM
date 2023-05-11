package com.example.myanimection.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import coil.Coil
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.apollographql.apollo3.api.Optional
import com.example.myanimection.R
import com.example.myanimection.adapters.RecyclerCharacterAdapter
import com.example.myanimection.adapters.RecyclerEpisodeAdapter
import com.example.myanimection.controllers.AnimeMediaController
import com.example.myanimection.repositories.AnimeMediaRepository
import com.example.myanimection.utils.GridSpacingItemDecorator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AnimeDetailFragment : Fragment() {

    private val animeMediaController = AnimeMediaController(AnimeMediaRepository())
    private val rvCharacterAdapter: RecyclerCharacterAdapter = RecyclerCharacterAdapter(arrayListOf())
    private val rvEpisodesAdapter: RecyclerEpisodeAdapter = RecyclerEpisodeAdapter(arrayListOf())

    private lateinit var imgPortrait: ImageView
    private lateinit var txtRomajiTitle: TextView
    private lateinit var txtNativeTitle: TextView
    private lateinit var txtDescription: TextView
    private lateinit var txtGenres: TextView
    private lateinit var txtStudio: TextView
    private lateinit var txtStartDate: TextView
    private lateinit var txtEndDate: TextView
    private lateinit var txtEpisodes: TextView
    private lateinit var txtStatus: TextView
    private lateinit var rvCharacters: RecyclerView
    private lateinit var rvEpisodes: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_anime_detail, container, false)

        imgPortrait = view.findViewById(R.id.imgAnimeDetailPortrait)
        txtRomajiTitle = view.findViewById(R.id.txtAnimeDetailTitleRomaji)
        txtNativeTitle = view.findViewById(R.id.txtAnimeDetailTitleNative)
        txtDescription = view.findViewById(R.id.txtAnimeDetailDescription)
        txtGenres = view.findViewById(R.id.txtAnimeDetailGenres)
        txtStudio = view.findViewById(R.id.txtAnimeDetailStudio)
        txtStartDate = view.findViewById(R.id.txtAnimeDetailStartDate)
        txtEndDate = view.findViewById(R.id.txtAnimeDetailEndDate)
        txtEpisodes = view.findViewById(R.id.txtAnimeDetailEpisodes)
        txtStatus = view.findViewById(R.id.txtAnimeDetailStatus)
        rvCharacters = view.findViewById(R.id.rvAnimeDetailCharacters)
        rvCharacters.adapter = rvCharacterAdapter
        rvCharacters.addItemDecoration(GridSpacingItemDecorator(3, 20, false))
        rvCharacters.layoutManager = GridLayoutManager(context, 3)
        rvEpisodes = view.findViewById(R.id.rvAnimeDetailEpisodes)
        rvEpisodes.adapter = rvEpisodesAdapter
        rvEpisodes.addItemDecoration(GridSpacingItemDecorator(3, 20, false))
        rvEpisodes.layoutManager = GridLayoutManager(context, 3)


        launchSingleAnimeQuery()

        return view
    }

    private fun launchSingleAnimeQuery() = lifecycleScope.launch (Dispatchers.IO) {
        val animeMediaId = arguments?.getInt("animeId", 1) ?: 1     //  Ternaria para que en caso de no encontrar ningún parámetro por defecto sea 1.
        val response = animeMediaController.getSingleAnime(Optional.present(animeMediaId))
        rvCharacterAdapter.data.addAll(response!!.characters)
        rvEpisodesAdapter.data.addAll(response.streamingEpisode?.filterNotNull()!!.toList())
        lifecycleScope.launch(Dispatchers.Main) {
            val request = ImageRequest.Builder(requireContext())
                .data(response.bannerImageURl)
                .transformations(RoundedCornersTransformation(10f))
                .target {
                    imgPortrait.setImageDrawable(it)
                }
                .build()
            Coil.imageLoader(requireContext()).enqueue(request)
            txtRomajiTitle.text = response.romajiTitle
            txtNativeTitle.text = response.nativeTitle
            txtDescription.text = response.description
            var genres = ""
            for (i in response.genres!!.indices) {
                if (i != response.genres.size-1) {
                    genres += response.genres[i] + ", "
                } else{
                    genres += response.genres[i]
                }
            }
            txtStudio.text = response.animationStudio
            txtGenres.text = genres
            txtStartDate.text = response.startDate
            txtEndDate.text = response.endDate
            txtStatus.text = response.status!!.name
            txtEpisodes.text = response.episodes.toString()
            rvCharacterAdapter.notifyItemRangeInserted(0, rvCharacterAdapter.itemCount-1)
            rvEpisodesAdapter.notifyItemRangeInserted(0, rvEpisodesAdapter.itemCount-1)
        }
    }

}