package com.example.myanimection.views

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myanimection.R
import com.example.myanimection.adapters.RecyclerReviewAdapter
import com.example.myanimection.controllers.ReviewController
import com.example.myanimection.models.AnimeReview
import com.example.myanimection.utils.GridSpacingItemDecorator
import com.example.myanimection.utils.Notifications

class ReviewsFragment : Fragment() {

    private val reviews = arrayListOf<AnimeReview>()
    private val reviewController = ReviewController()
    private val reviewsAdapter = RecyclerReviewAdapter(reviews)
    private lateinit var rvReviews: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reviews, container, false)
        setupMenu()
        rvReviews = view.findViewById(R.id.rvReviews)
        rvReviews.adapter = reviewsAdapter
        rvReviews.layoutManager = LinearLayoutManager(view.context)
        rvReviews.addItemDecoration(GridSpacingItemDecorator(1, 25, false))
        refreshList(arguments?.getInt("animeId", 1) ?: 1)
        return view
    }

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

}