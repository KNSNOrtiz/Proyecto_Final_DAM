package com.example.myanimection.views

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo3.api.Optional
import com.example.myanimection.R
import com.example.myanimection.adapters.OnSwipeTouchListener
import com.example.myanimection.adapters.RecyclerHomeAnimeAdapter
import com.example.myanimection.controllers.AnimeMediaController
import com.example.myanimection.models.AnimeMedia
import com.example.myanimection.repositories.AnimeMediaRepository
import com.example.myanimection.utils.GridSpacingItemDecorator
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {

    private val animeMediaController = AnimeMediaController(AnimeMediaRepository())
    private val rvAnimeHomeAnimeAdapter = RecyclerHomeAnimeAdapter(ArrayList<AnimeMedia?>())
    private val myAnimeList = ArrayList<AnimeMedia?>()
    private var currentPage = 1
    private var totalPages = 0
    private var nextPage = false

    private lateinit var rvAnimeHome: RecyclerView
    private lateinit var txtCurrentPage: TextView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        rvAnimeHome = view.findViewById(R.id.rvAnimeHome)
        rvAnimeHome.addItemDecoration(GridSpacingItemDecorator(2, 30, false))
        rvAnimeHome.adapter = rvAnimeHomeAnimeAdapter
        rvAnimeHome.layoutManager = GridLayoutManager(context, 2)
        rvAnimeHome.itemAnimator = null
        txtCurrentPage = view.findViewById(R.id.txtCurrentPageHome)
        rvAnimeHome.setOnTouchListener(object: OnSwipeTouchListener(view.context, rvAnimeHome){
            override fun onSwipeRight() {
                if (currentPage > 1) {
                    currentPage--
                    launchPageQuery()
                }
            }

            override fun onSwipeLeft() {
                if (currentPage < totalPages) {
                    currentPage++
                    launchPageQuery()
                }
            }
        })

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  FIREBASE AUTHENTICATION
        val user = FirebaseAuth.getInstance().currentUser
        Log.d("Test","¡Bienvenido/a, ${user?.email}!" )

        try{
            launchPageQuery()
        } catch (ex: RuntimeException){
            Log.d("NOT FOUND", "Animes no encontrados.")
        }



    }
    //  Lanzamiento de corutina en un hilo de lectura/escritura.
     private fun launchPageQuery() = lifecycleScope.launch(Dispatchers.IO) {
            myAnimeList.clear()
            val response =  animeMediaController.getPageAnimes(Optional.present(currentPage), Optional.present(30))
            totalPages = response?.pageInfo?.total!!
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
            refreshAnimeHome()
        }


    //  Lanzamiento de una nueva corutina en cuanto se ha obtenido el resultado de esta primera llamada, esta vez en el hilo de la interfaz.
    private fun refreshAnimeHome() = lifecycleScope.launch(Dispatchers.Main) {
        rvAnimeHome.recycledViewPool.clear()
        val previousSize = rvAnimeHomeAnimeAdapter.itemCount
        rvAnimeHomeAnimeAdapter.data.clear()
        rvAnimeHomeAnimeAdapter.notifyItemRangeRemoved(0, previousSize)
        rvAnimeHomeAnimeAdapter.data.addAll(myAnimeList)
        rvAnimeHomeAnimeAdapter.notifyItemRangeInserted(0, rvAnimeHomeAnimeAdapter.itemCount)
        txtCurrentPage.text = "$currentPage / $totalPages"
        Log.d("AnimeCount", "${rvAnimeHome.adapter?.itemCount}")
    }


}