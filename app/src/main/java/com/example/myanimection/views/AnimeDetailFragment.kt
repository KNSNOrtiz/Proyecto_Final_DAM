package com.example.myanimection.views

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.Coil
import coil.load
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.apollographql.apollo3.api.Optional
import com.example.myanimection.R
import com.example.myanimection.adapters.RecyclerCharacterAdapter
import com.example.myanimection.adapters.RecyclerEpisodeAdapter
import com.example.myanimection.controllers.AnimeMediaController
import com.example.myanimection.controllers.FirestoreQueryCallback
import com.example.myanimection.controllers.UserController
import com.example.myanimection.models.AnimeCategory
import com.example.myanimection.models.AnimeMediaDetailed
import com.example.myanimection.models.ListedAnimeMedia
import com.example.myanimection.repositories.AnimeMediaRepository
import com.example.myanimection.utils.SpacingItemDecorator
import com.example.myanimection.utils.Notifications
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AnimeDetailFragment : Fragment() {

    private val userController = UserController()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val animeMediaController = AnimeMediaController(AnimeMediaRepository())
    private val rvCharacterAdapter: RecyclerCharacterAdapter = RecyclerCharacterAdapter(arrayListOf())
    private val rvEpisodesAdapter: RecyclerEpisodeAdapter = RecyclerEpisodeAdapter(arrayListOf())
    private val queryCompleteCallback = object: FirestoreQueryCallback {
        override fun onQueryComplete(success: Boolean) {
            Notifications.shortToast(context!!, "Anime añadido a la lista.")
        }

        override fun onQueryFailure(exception: Exception) {
            Notifications.shortToast(context!!, "Ha habido un problema al añadir el anime.")
        }
    }
    private lateinit var queriedAnime: AnimeMediaDetailed
    private lateinit var dialog: MaterialDialog
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
    private lateinit var txtLabelCharacters: TextView
    private lateinit var txtLabelStreamingEpisodes: TextView
    private lateinit var cvCharacters: CardView
    private lateinit var cvEpisodes: CardView
    private lateinit var txtExpandCharacters: TextView
    private lateinit var txtExpandEpisodes: TextView
    private lateinit var rvCharacters: RecyclerView
    private lateinit var rvEpisodes: RecyclerView
    private lateinit var dividerCharacters: View
    private lateinit var dividerEpisodes: View

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

        txtLabelCharacters = view.findViewById(R.id.labelCharacters)
        txtLabelStreamingEpisodes = view.findViewById(R.id.labelStreamingEpisodes)
        cvCharacters = view.findViewById(R.id.cvCharacters)
        cvEpisodes = view.findViewById(R.id.cvEpisodes)
        txtExpandCharacters = view.findViewById(R.id.txtExpandCharacters)
        txtExpandEpisodes = view.findViewById(R.id.txtExpandEpisodes)
        dividerCharacters = view.findViewById(R.id.dividerCharacters)
        dividerEpisodes = view.findViewById(R.id.dividerEpisodes)
        rvCharacters = view.findViewById(R.id.rvAnimeDetailCharacters)
        rvCharacters.adapter = rvCharacterAdapter
        rvCharacters.addItemDecoration(SpacingItemDecorator(3, 20, false))
        rvCharacters.layoutManager = GridLayoutManager(context, 3)
        rvEpisodes = view.findViewById(R.id.rvAnimeDetailEpisodes)
        rvEpisodes.adapter = rvEpisodesAdapter
        rvEpisodes.addItemDecoration(SpacingItemDecorator(1, 10, false))
        rvEpisodes.layoutManager = LinearLayoutManager(context)


        txtExpandCharacters.setOnClickListener {
            if  (rvCharacters.isVisible) {
                txtExpandCharacters.text = "Expandir"
                dividerCharacters.visibility = View.GONE
                rvCharacters.visibility = View.GONE
            } else {
                txtExpandCharacters.text = "Contraer"
                dividerCharacters.visibility = View.VISIBLE
                rvCharacters.visibility = View.VISIBLE
            }
        }

        txtExpandEpisodes.setOnClickListener {
            if (rvEpisodes.isVisible) {
                txtExpandEpisodes.text = "Expandir"
                dividerEpisodes.visibility = View.GONE
                rvEpisodes.visibility = View.GONE
            } else {
                txtExpandEpisodes.text = "Contraer"
                dividerEpisodes.visibility = View.VISIBLE
                rvEpisodes.visibility = View.VISIBLE
            }
        }


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
                   android.R.id.home -> {
                       requireActivity().onBackPressedDispatcher.onBackPressed()
                       return true
                   }
                   R.id.itemAddAnime -> {
                       dialog = MaterialDialog(context!!)
                           .noAutoDismiss()
                           .customView(R.layout.dialog_add_anime)
                           .title(text = "Añadir anime")
                       dialog.findViewById<ImageView>(R.id.imgAddAnimeImage).load(imgPortrait.drawable) {transformations(CircleCropTransformation())}
                       dialog.findViewById<TextView>(R.id.txtAddAnimeTitle).text = txtRomajiTitle.text
                       val dialogWatchedEpisodes = dialog.findViewById<TextView>(R.id.txtAddAnimeWatched).apply { text = "0" }
                       dialog.findViewById<TextView>(R.id.txtAddAnimeTotal).text = txtEpisodes.text
                       val dialogSpinner = dialog.findViewById<Spinner>(R.id.spinAddAnimeCategory)
                       ArrayAdapter.createFromResource(context!!, R.array.listed_categories, R.layout.ani_spin_item).also { adapter ->
                           adapter.setDropDownViewResource(R.layout.ani_spin_dropdown_item)
                           dialogSpinner.adapter = adapter
                       }
                       dialog.findViewById<TextView>(R.id.txtAddAnimePositive).setOnClickListener {
                           var watchedEpisodes = 0
                           if (currentUser != null) {
                               try {
                                   watchedEpisodes = dialogWatchedEpisodes.text.toString().toInt()
                               } catch (ex: NumberFormatException) {
                                   watchedEpisodes  = 0
                               }
                               if (queriedAnime.episodes != null) {
                                   if (watchedEpisodes  >= queriedAnime.episodes!!) {
                                       watchedEpisodes = queriedAnime.episodes!!
                                   }
                               }
                               dialogWatchedEpisodes.text = "$watchedEpisodes"
                               userController.isAnimeListed(Firebase.auth.currentUser!!.uid, queriedAnime.id, object: FirestoreQueryCallback {
                                   override fun onQueryComplete(success: Boolean) {
                                       if (!success) {
                                           userController.addAnime(
                                               Firebase.auth.currentUser!!.uid,
                                               ListedAnimeMedia(queriedAnime.id,
                                                   queriedAnime.romajiTitle!!,
                                                   queriedAnime.bannerImageURl!!,
                                                   watchedEpisodes, queriedAnime.episodes,
                                                   AnimeCategory.valueOf(dialogSpinner.selectedItem.toString())),
                                               queryCompleteCallback)
                                       } else {
                                           Notifications.alertDialogOK(context!!, "Editar anime", "El anime ya está en tu lista. ¿Quieres actualizarlo?",
                                           positiveButtonClickListener = { okDialog ->
                                               userController.updateAnime(
                                                   Firebase.auth.currentUser!!.uid,
                                                   ListedAnimeMedia(queriedAnime.id,
                                                       queriedAnime.romajiTitle!!,
                                                       queriedAnime.bannerImageURl!!,
                                                       watchedEpisodes, queriedAnime.episodes,
                                                       AnimeCategory.valueOf(dialogSpinner.selectedItem.toString())),
                                                   queryCompleteCallback)
                                             okDialog.dismiss()
                                           },
                                           negativeButtonClickListener = { okDialog ->
                                               okDialog.dismiss()
                                           })

                                       }
                                   }

                                   override fun onQueryFailure(exception: Exception) {
                                       Notifications.shortToast(view!!.context, "Hubo un error al recuperar el anime de la lista.")
                                   }
                               })
                           }
                           dialog.dismiss()
                       }

                       dialog.findViewById<TextView>(R.id.txtAddAnimeNegative).setOnClickListener {
                           dialog.dismiss()
                       }
                       dialog.show()
                   }
                   R.id.itemReviews -> {
                       loadFragment(context!!, queriedAnime.id)
                   }
               }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun launchSingleAnimeQuery() = lifecycleScope.launch (Dispatchers.IO) {
        val animeMediaId = arguments?.getInt("animeId", 1) ?: 1
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
                    if (i != queriedAnime.genres!!.size - 1) {
                        genres += queriedAnime.genres!![i] + ", "
                    } else {
                        genres += queriedAnime.genres!![i]
                    }
                }
                txtStudio.text = queriedAnime.animationStudio
                txtGenres.text = genres
                txtStartDate.text = queriedAnime.startDate
                txtEndDate.text = queriedAnime.endDate
                txtStatus.text = queriedAnime.status
                txtEpisodes.text = (queriedAnime.episodes ?: "Desconocido").toString()
                rvCharacterAdapter.notifyDataSetChanged()
                rvEpisodesAdapter.notifyDataSetChanged()

                if (queriedAnime.characters.isEmpty()) {
                    cvCharacters.visibility = View.GONE
                    txtLabelCharacters.visibility = View.GONE
                }

                if (queriedAnime.streamingEpisode!!.isEmpty()) {
                    cvEpisodes.visibility = View.GONE
                    txtLabelStreamingEpisodes.visibility = View.GONE
                }
            }
        }
    }

    private fun loadFragment(context: Context, animeMediaId: Int) {
        val fragment = AnimeDetailFragment()
        val bundle = Bundle()
        bundle.putInt("animeId", animeMediaId)
        fragment.arguments = bundle
        val navHostFragment = (context as AppCompatActivity)
            .supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.action_animeDetailFragment_to_ReviewsFragment, bundle)
    }

}