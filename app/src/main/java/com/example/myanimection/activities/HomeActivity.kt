package com.example.myanimection.activities

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo3.api.Optional
import com.example.myanimection.R
import com.example.myanimection.adapters.RecyclerHomeAnimeAdapter
import com.example.myanimection.controllers.AnimeMediaController
import com.example.myanimection.models.AnimeMedia
import com.example.myanimection.repositories.AnimeMediaRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private val animeMediaController = AnimeMediaController(AnimeMediaRepository())
    private var animeList = ArrayList<AnimeMedia?>()
    private var currentPage = 1
    private var totalPages = 0
    private var nextPage = false

    private lateinit var rvAnimeHome: RecyclerView
    private lateinit var txtCurrentPage: TextView
    private lateinit var btnNextPage: Button
    private lateinit var btnPreviousPage: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        rvAnimeHome = findViewById(R.id.rvAnimeHome)
        txtCurrentPage = findViewById(R.id.txtCurrentPageHome)
        btnNextPage = findViewById(R.id.btnNextPageHome)
        btnPreviousPage = findViewById(R.id.btnPreviousPageHome)
        //  FIREBASE AUTHENTICATION
        val user = FirebaseAuth.getInstance().currentUser
        Log.d("Test","¡Bienvenido/a, ${user?.email}!" )

        try{
            launchPageQuery()
        } catch (ex: RuntimeException){
            Log.d("NOT FOUND", "Animes no encontrados.")
        }

        btnNextPage.setOnClickListener {
            currentPage++
            launchPageQuery()
        }
        btnPreviousPage.setOnClickListener {
            currentPage--
            launchPageQuery()
        }


    }
    //  Lanzamiento de corutina en un hilo de lectura/escritura.
     private fun launchPageQuery() = lifecycleScope.launch(Dispatchers.IO) {
            animeList.clear()
            val response =  animeMediaController.getPageAnimes(Optional.present(currentPage), Optional.present(30))
            totalPages = response?.pageInfo?.total!!
            nextPage = response.pageInfo.hasNextPage!!
            response.media?.forEach {
                if (it != null) {
                    animeList.add(
                        AnimeMedia(
                            it.id,
                            it.title?.romaji.toString(),
                            it.title?.native.toString(),
                            it.coverImage?.large.toString(),
                            "${it.startDate?.day}-${it.startDate?.month}-${it.startDate?.year}",
                            "${it.endDate?.day}-${it.endDate?.month}-${it.endDate?.year}",
                            it.genres,
                            it.episodes,
                            it.status
                        )
                    )
                }
            }
            refreshAnimeHome()
        }

    //  Lanzamiento de una nueva corutina en cuanto se ha obtenido el resultado de esta primera llamada, esta vez en el hilo de la interfaz.
    private fun refreshAnimeHome() = lifecycleScope.launch(Dispatchers.Main) {
            val rvAnimeHomeAnimeAdapter = RecyclerHomeAnimeAdapter(animeList)
            rvAnimeHome.layoutManager = GridLayoutManager(applicationContext, 2)
            rvAnimeHome.adapter = rvAnimeHomeAnimeAdapter
            btnNextPage.isEnabled = nextPage
            btnPreviousPage.isEnabled = currentPage > 1
            txtCurrentPage.text = currentPage.toString()
            Log.d("AnimeCount", "${rvAnimeHome.adapter?.itemCount}")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_action_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.itemLogOut -> {
                FirebaseAuth.getInstance().signOut()
                onBackPressedDispatcher.onBackPressed()
                true
            } else -> super.onOptionsItemSelected(item)

        }
    }
}