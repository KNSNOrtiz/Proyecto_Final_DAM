package com.example.myanimection.views

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.Coil
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.apollographql.apollo3.api.Optional
import com.example.myanimection.R
import com.example.myanimection.adapters.RecyclerCharacterAdapter
import com.example.myanimection.adapters.RecyclerEpisodeAdapter
import com.example.myanimection.controllers.AnimeMediaController
import com.example.myanimection.controllers.UserController
import com.example.myanimection.models.AnimeMediaDetailed
import com.example.myanimection.models.ListedAnimeMedia
import com.example.myanimection.repositories.AnimeMediaRepository
import com.example.myanimection.utils.GridSpacingItemDecorator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AnimeDetailFragment : Fragment() {

    private val userController = UserController()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val animeMediaController = AnimeMediaController(AnimeMediaRepository())
    private val rvCharacterAdapter: RecyclerCharacterAdapter = RecyclerCharacterAdapter(arrayListOf())
    private val rvEpisodesAdapter: RecyclerEpisodeAdapter = RecyclerEpisodeAdapter(arrayListOf())
    private lateinit var queriedAnime: AnimeMediaDetailed

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
        (activity as MainActivity).supportActionBar?.show()
        setupMenu()
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

    private fun setupMenu() {
        (requireActivity() as MainActivity).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {

            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.menu_action_detail, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
               when(menuItem.itemId) {
                   R.id.itemAddWatching -> {
                       if (currentUser != null) {
                           userController.addAnimeToList(currentUser.uid, arrayListOf(
                               ListedAnimeMedia(queriedAnime.id, queriedAnime.romajiTitle!!, queriedAnime.bannerImageURl!!, 0, queriedAnime.episodes )
                           ), UserController.ANIMELIST.WATCHING)
                       }
                   }
                   R.id.itemAddComplete -> {
                       if (currentUser != null) {
                           userController.addAnimeToList(currentUser.uid, arrayListOf(
                               ListedAnimeMedia(queriedAnime.id, queriedAnime.romajiTitle!!, queriedAnime.bannerImageURl!!, 0, queriedAnime.episodes )
                           ), UserController.ANIMELIST.COMPLETED)
                       }
                   }
                   R.id.itemAddPending -> {
                       if (currentUser != null) {
                           userController.addAnimeToList(currentUser.uid, arrayListOf(
                               ListedAnimeMedia(queriedAnime.id, queriedAnime.romajiTitle!!, queriedAnime.bannerImageURl!!, 0, queriedAnime.episodes )
                           ), UserController.ANIMELIST.PENDING)
                       }
                   }
                   R.id.itemAddDropped -> {
                       if (currentUser != null) {
                           userController.addAnimeToList(currentUser.uid, arrayListOf(
                               ListedAnimeMedia(queriedAnime.id, queriedAnime.romajiTitle!!, queriedAnime.bannerImageURl!!, 0, queriedAnime.episodes )
                           ), UserController.ANIMELIST.DROPPED)
                       }
                   }
               }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun launchSingleAnimeQuery() = lifecycleScope.launch (Dispatchers.IO) {
        val animeMediaId = arguments?.getInt("animeId", 1) ?: 1     //  Ternaria para que en caso de no encontrar ningún parámetro por defecto sea 1.
        val response = animeMediaController.getSingleAnime(Optional.present(animeMediaId))
        if (response != null) {
            queriedAnime = response
            rvCharacterAdapter.data.addAll(queriedAnime.characters)
            rvEpisodesAdapter.data.addAll(queriedAnime.streamingEpisode?.filterNotNull()!!.toList())
            lifecycleScope.launch(Dispatchers.Main) {
                val request = ImageRequest.Builder(requireContext())
                    .data(queriedAnime.bannerImageURl)
                    .transformations(RoundedCornersTransformation(10f))
                    .target {
                        imgPortrait.setImageDrawable(it)
                    }
                    .build()
                Coil.imageLoader(requireContext()).enqueue(request)
                txtRomajiTitle.text = queriedAnime.romajiTitle
                txtNativeTitle.text = queriedAnime.nativeTitle
                txtDescription.text = queriedAnime.description
                var genres = ""
                for (i in queriedAnime.genres!!.indices) {
                    if (i != queriedAnime.genres!!.size-1) {
                        genres += queriedAnime.genres!![i] + ", "
                    } else{
                        genres += queriedAnime.genres!![i]
                    }
                }
                txtStudio.text = queriedAnime.animationStudio
                txtGenres.text = genres
                txtStartDate.text = queriedAnime.startDate
                txtEndDate.text = queriedAnime.endDate
                txtStatus.text = queriedAnime.status!!.name
                txtEpisodes.text = queriedAnime.episodes.toString()
                rvCharacterAdapter.notifyItemRangeInserted(0, rvCharacterAdapter.itemCount-1)
                rvEpisodesAdapter.notifyItemRangeInserted(0, rvEpisodesAdapter.itemCount-1)
        }
        }
    }

}