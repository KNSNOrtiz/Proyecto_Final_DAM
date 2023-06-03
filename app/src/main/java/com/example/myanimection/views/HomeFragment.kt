package com.example.myanimection.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo3.api.Optional
import com.example.myanimection.R
import com.example.myanimection.adapters.RecyclerAnimeMediaAdapter
import com.example.myanimection.controllers.AnimeMediaController
import com.example.myanimection.models.AnimeMedia
import com.example.myanimection.repositories.AnimeMediaRepository
import com.example.myanimection.utils.SpacingItemDecorator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Math.abs

/**
 * Fragment que representa la vista de inicio o Home, donde se muestra un catálogo de animes paginado.
 */
class HomeFragment : Fragment() {

    private val animeMediaController = AnimeMediaController(AnimeMediaRepository())
    private val rvAnimeHomeAnimeAdapter = RecyclerAnimeMediaAdapter(ArrayList<AnimeMedia?>())
    private val myAnimeList = ArrayList<AnimeMedia?>()
    private var currentPage = 1
    private var totalPages = 0
    private var nextPage = false

    private lateinit var cardViewRV: CardView
    private lateinit var rvAnimeHome: RecyclerView
    private lateinit var txtCurrentPage: TextView
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNext: ImageButton

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        (activity as MainActivity).supportActionBar?.show()
        rvAnimeHome = view.findViewById(R.id.rvAnimeHome)
        cardViewRV = view.findViewById(R.id.cardViewRecycler)
        txtCurrentPage = view.findViewById(R.id.txtCurrentPageHome)

        btnPrevious = view.findViewById(R.id.btnHomePrevious)
        btnNext = view.findViewById(R.id.btnHomeNext)

        btnPrevious.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                animateCardView(cardViewRV.width.toFloat())
            }
        }
        btnNext.setOnClickListener {
            if (currentPage < totalPages) {
                currentPage++
                animateCardView(-cardViewRV.width.toFloat())
            }
        }

        rvAnimeHome.addItemDecoration(SpacingItemDecorator(2, 30, false))
        rvAnimeHome.adapter = rvAnimeHomeAnimeAdapter
        rvAnimeHome.layoutManager = GridLayoutManager(context, 2)
        rvAnimeHome.itemAnimator = null

        rvAnimeHome.setOnTouchListener(object : View.OnTouchListener {
            private var downX = 0f
            private var downY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.x
                        downY = event.y
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val upX = event.x
                        val upY = event.y
                        val deltaX = downX - upX
                        val deltaY = downY - upY
                        if (abs(deltaX) > abs(deltaY) && deltaY < 50 && abs(deltaX) >= 300) {
                            if (deltaX < 0) {
                                // Desplazamiento hacia la derecha
                                if (currentPage > 1) {
                                    currentPage--
                                    animateCardView(cardViewRV.width.toFloat())
                                }
                            } else {
                                // Desplazamiento hacia la izquierda
                                if (currentPage < totalPages) {
                                    currentPage++
                                    animateCardView(-cardViewRV.width.toFloat())
                                }
                            }
                            return true
                        }
                        v.performClick()
                    }
                }
                return false
            }
        })

        return view
    }

    override fun onResume() {
        super.onResume()
        try{
            launchPageQuery()
        } catch (ex: RuntimeException){
            Log.d("NOT FOUND", "Animes no encontrados.")
        }
    }

    /**
     * Lanzamiento de corutina en el hilo de Entrada/Salida para obtener los detalles del anime.
     * A la vez, se asignan los valores del anime a la vista y se refrescan los RecyclerView de personajes y episodios.
     */
     private fun launchPageQuery() = lifecycleScope.launch(Dispatchers.IO) {
            myAnimeList.clear()
            val response =  animeMediaController.getPageAnimes(Optional.present(currentPage), Optional.present(30))
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
            refreshAnimeHome()
        }


    /**
     * Lanzamiento de corutina en el hilo principal para refrescar la vista con los datos nuevos que han llegado
     * de la consulta de GraphQL.*/
    private fun refreshAnimeHome() = lifecycleScope.launch(Dispatchers.Main) {
        rvAnimeHome.recycledViewPool.clear()
        val previousSize = rvAnimeHomeAnimeAdapter.itemCount
        rvAnimeHomeAnimeAdapter.data.clear()
        rvAnimeHomeAnimeAdapter.notifyItemRangeRemoved(0, previousSize)
        rvAnimeHomeAnimeAdapter.data.addAll(myAnimeList)
        rvAnimeHomeAnimeAdapter.notifyItemRangeInserted(0, rvAnimeHomeAnimeAdapter.itemCount)
        txtCurrentPage.text = "$currentPage / $totalPages"
        btnNext.isEnabled = nextPage
        btnPrevious.isEnabled = currentPage > 1
        Log.d("AnimeCount", "${rvAnimeHome.adapter?.itemCount}")
    }

    /** Método encargado de proporcionar una animación de desplazamiento horizontal al Cardview
     * que representa una página del catálogo de animes
     * @param offset El valor que tiene el desplazamiento a realizar en la animación.
     */
    fun animateCardView(offset: Float) {
        //  Animación que mueve el CardView en el eje horizontal.
        val animator = ValueAnimator.ofFloat(0f, offset)
        animator.addUpdateListener { valueAnimator ->
            val animatedValue = valueAnimator.animatedValue as Float
            cardViewRV.translationX = animatedValue
        }
        animator.duration = 300
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addListener(object : AnimatorListenerAdapter() {
            //  Cuando la animación termina, el CardView vuelve a su sitio original en el mismo tiempo y con la misma animación, pero inversa.
            override fun onAnimationEnd(animation: Animator) {
                val animatorReverse = ValueAnimator.ofFloat(offset, 0f)
                animatorReverse.addUpdateListener { valueAnimator ->
                    val animatedValue = valueAnimator.animatedValue as Float
                    cardViewRV.translationX = animatedValue
                }
                animatorReverse.duration = 300
                animatorReverse.interpolator = AccelerateDecelerateInterpolator()
                animatorReverse.start()
            }
        })

        animator.start()
        // DESPUÉS DE LANZAR LA ANIMACIÓN, REFRESCO LOS DATOS
        launchPageQuery()
    }
}