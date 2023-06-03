package com.example.myanimection.views

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myanimection.R
import com.example.myanimection.adapters.RecyclerListedAnimeAdapter
import com.example.myanimection.adapters.RecyclerReviewAdapter
import com.example.myanimection.controllers.ReviewController
import com.example.myanimection.models.AnimeReview
import com.example.myanimection.utils.SpacingItemDecorator
import com.example.myanimection.utils.Notifications
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Fragment que muestra las reseñas de un anime escritas por los usuarios y permite añadir o editar nuevas reseñas.
 */
class ReviewsFragment : Fragment() {

    private val reviews = arrayListOf<AnimeReview>()
    private val reviewController = ReviewController()
    private val reviewsAdapter = RecyclerReviewAdapter(reviews, false)
    private lateinit var rvReviews: RecyclerView
    private lateinit var btnAddReview: FloatingActionButton


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       val animeId = arguments?.getInt("animeId", 1) ?: 1

        val view = inflater.inflate(R.layout.fragment_reviews, container, false)
        setupMenu()
        rvReviews = view.findViewById(R.id.rvReviews)
        btnAddReview = view.findViewById(R.id.btnAddReview)
        btnAddReview.setOnClickListener {
            loadFragment(view.context, animeId)
        }
        reviewsAdapter.reviewChangedListener = object : RecyclerReviewAdapter.ReviewChangedListener {
            override fun notifyRecyclerView() {
                refreshList(animeId)
            }
        }
        rvReviews.adapter = reviewsAdapter
        rvReviews.layoutManager = LinearLayoutManager(view.context)
        rvReviews.addItemDecoration(SpacingItemDecorator(1, 25, false))
        refreshList(animeId)
        return view
    }

    /** Actualiza el RecyclerView de reseñas del anime.
     * @param animeId ID del anime del que se mostrarán las reseñas.
     */
    fun refreshList(animeId: Int) {
        reviewController.getReviewsFromAnime(animeId, object: ReviewController.ReviewsQueryCallback {
            override fun onQueryComplete(result: ArrayList<AnimeReview>) {
                rvReviews.recycledViewPool.clear()
                val previousSize = reviewsAdapter.itemCount
                reviews.clear()
                reviewsAdapter.notifyItemRangeRemoved(0, previousSize)
                reviews.addAll(result)
                reviewsAdapter.notifyItemRangeInserted(0, reviews.size)
            }
            override fun onQueryFailure(exception: Exception) {
                Notifications.shortToast(context!!, "No se han podido recuperar las reseñas.")
            }
        })

    }

    /**
     * Configuración del menú de acción en cuanto el estado de la vista es Resumed.
     */
    private fun setupMenu() {
        (requireActivity() as MainActivity).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when(menuItem.itemId) {
                    android.R.id.home -> {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    /**
     * Método que carga la pantalla de escritura de reseñas del anime.
     *
     * @param context Contexto de la aplicación.
     * @param animeMediaId ID del anime del que se escribirá la reseña.
     */
    private fun loadFragment(context: Context, animeMediaId: Int) {
        val bundle = Bundle()
        bundle.putInt("animeId", animeMediaId)
        val navHostFragment = (context as AppCompatActivity)
            .supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.action_ReviewsFragment_to_AddReviewFragment, bundle)
    }

}