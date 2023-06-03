package com.example.myanimection.views

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.apollographql.apollo3.api.Optional
import com.example.myanimection.R
import com.example.myanimection.adapters.RecyclerAnimeMediaAdapter
import com.example.myanimection.controllers.AnimeMediaController
import com.example.myanimection.models.AnimeMedia
import com.example.myanimection.repositories.AnimeMediaRepository
import com.example.myanimection.type.MediaSort
import com.example.myanimection.utils.SpacingItemDecorator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

/**
 * Fragment que permite buscar animes y filtrar los resultados.
 */
class SearchAnimeFragment : Fragment() {

    private val myAnimeList = ArrayList<AnimeMedia?>()
    private val animeMediaController = AnimeMediaController(AnimeMediaRepository())
    private val animeMediaAdapter = RecyclerAnimeMediaAdapter(ArrayList<AnimeMedia?>())

    private lateinit var txtSearch: TextView
    private lateinit var rvSearchAnime: RecyclerView
    private lateinit var btnFilter: Button

    //  Flag para controlar que no se realice la búsqueda cada vez que se entra a la vista.
    private var isSearchEnabled = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_anime, container, false)
        txtSearch = view.findViewById(R.id.txtSearchAnime)

        btnFilter = view.findViewById(R.id.btnSearchFilter)

        btnFilter.setOnClickListener {
            var romaji: MediaSort?
            var startDate: MediaSort?
            var genres: ArrayList<String>

            //  Cuadro de diálogo personalizado que permite filtrar los animes en base a unos criterios que permite la API.
            val dialog = MaterialDialog(view.context)
                .noAutoDismiss()
                .customView(R.layout.dialog_filter_anime)
                .title(text = "Filtrar Animes")
            val romajiFilter = dialog.findViewById<RadioGroup>(R.id.rbgAnimeFilterRomaji)
            val startDateFilter = dialog.findViewById<RadioGroup>(R.id.rbgAnimeFilterStartDate)
            val spinGenre1 = dialog.findViewById<Spinner>(R.id.spinAnimeFilterGenre)
            val spinGenre2 = dialog.findViewById<Spinner>(R.id.spinAnimeFilterGenre2)
            val spinGenre3 = dialog.findViewById<Spinner>(R.id.spinAnimeFilterGenre3)
            val positiveButton = dialog.findViewById<TextView>(R.id.txtAnimeFilterPositive)
            val negativeButton = dialog.findViewById<TextView>(R.id.txtAnimeFilterNegative)

            ArrayAdapter.createFromResource(view.context, R.array.genres, R.layout.ani_spin_item).also { adapter ->
                adapter.setDropDownViewResource(R.layout.ani_spin_dropdown_item)
                spinGenre1.adapter = adapter
                spinGenre2.adapter = adapter
                spinGenre3.adapter = adapter
            }

            //  MediaSort es un enum generado por Apollo a partir del esquema de GraphQL de la API que contiene criterios de ordenación.
            positiveButton.setOnClickListener {
                romaji = when (romajiFilter.checkedRadioButtonId) {
                    R.id.rbAnimeFilterRomajiASC -> MediaSort.TITLE_ROMAJI
                    R.id.rbAnimeFilterRomajiDESC -> MediaSort.TITLE_ROMAJI_DESC
                    else -> null
                }
                startDate = when (startDateFilter.checkedRadioButtonId) {
                    R.id.rbAnimeFilterStartDateASC -> MediaSort.START_DATE
                    R.id.rbAnimeFilterStartDateDESC -> MediaSort.START_DATE_DESC
                    else -> null
                }
                genres = arrayListOf(spinGenre1.selectedItem.toString(), spinGenre2.selectedItem.toString(), spinGenre3.selectedItem.toString())
                launchSearchQuery(listOfNotNull(romaji, startDate), genres)
                dialog.dismiss()
            }
            negativeButton.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()

        }

        rvSearchAnime = view.findViewById(R.id.rvSearchAnime)
        rvSearchAnime.adapter = animeMediaAdapter
        rvSearchAnime.addItemDecoration(SpacingItemDecorator(2, 30, false))
        rvSearchAnime.layoutManager = GridLayoutManager(context, 2)

        //  No se activará la búsqueda hasta que no se establezca el foco en la barra de búsqueda al menos una vez.
        txtSearch.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                isSearchEnabled = true
            }
        }

        //  Listener que escucha cuando el usuario deja de escribir para realizar automáticamente una búsqueda sin filtros.
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
                            if (isSearchEnabled) {
                                //  Búsqueda por texto sin ningún filtro.
                                launchSearchQuery(listOf(), arrayListOf())
                            }
                        }
                    }
                }, DELAY_MILLIS)
            }

        })
        return view
    }

    //  En el onResume se desactiva la búsqueda por el motivo explicado más arriba.
    override fun onResume() {
        super.onResume()
        isSearchEnabled = false
    }

    /**
     * Lanzamiento de corutina en el hilo de Entrada/Salida para obtener los animes en base a los criterios establecidos, si se han establecido.
     * @param sort          Lista con los criterios de ordenación [MediaSort]
     * @param genresFilter  Lista con los géneros por los que se quiere filtrar en formato de texto.
     */
    private fun launchSearchQuery(sort: List<MediaSort>, genresFilter: ArrayList<String>) = lifecycleScope.launch(Dispatchers.IO) {
        myAnimeList.clear()
        val search = txtSearch.text.trim().toString()
        val genres = genresFilter.filter { genre ->  !genre.equals("Sin género")}.toMutableList()
        val response = animeMediaController.getSearchAnimes(if(search.isNotEmpty()) Optional.present(search) else Optional.absent(),
            if(genres.isNotEmpty()) Optional.present(genres) else Optional.absent(),
            if(sort.isNotEmpty()) Optional.present(sort) else Optional.absent())

        if (response != null) {
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
        }
        refreshRecyclerView()
    }

    /**
     * Actualiza el RecyclerView con los animes encontrados.
     */
    private fun refreshRecyclerView() = lifecycleScope.launch(Dispatchers.Main) {
        rvSearchAnime.recycledViewPool.clear()
        val previousSize = animeMediaAdapter.itemCount
        animeMediaAdapter.data.clear()
        animeMediaAdapter.notifyItemRangeRemoved(0, previousSize)
        animeMediaAdapter.data.addAll(myAnimeList)
        animeMediaAdapter.notifyItemRangeInserted(0, animeMediaAdapter.itemCount)
    }
}