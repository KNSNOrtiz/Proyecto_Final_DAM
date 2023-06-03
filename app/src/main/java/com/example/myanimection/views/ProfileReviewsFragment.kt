package com.example.myanimection.views

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myanimection.R
import com.example.myanimection.adapters.RecyclerReviewAdapter
import com.example.myanimection.controllers.ReviewController
import com.example.myanimection.models.AnimeReview
import com.example.myanimection.utils.SpacingItemDecorator

class ProfileReviewsFragment(private val uidSearch: String) : Fragment() {

    private lateinit var rvReviews: RecyclerView
    private lateinit var rvReviewsAdapter: RecyclerReviewAdapter
    private val reviewController = ReviewController()
    private val userReviews = arrayListOf<AnimeReview>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_reviews, container, false)
        rvReviews = view.findViewById(R.id.rvProfileReviews)
        rvReviewsAdapter = RecyclerReviewAdapter(userReviews, true)
        rvReviewsAdapter.reviewChangedListener = object : RecyclerReviewAdapter.ReviewChangedListener {
            override fun notifyRecyclerView() {
                refreshList()
            }
        }
        rvReviews.adapter = rvReviewsAdapter
        rvReviews.layoutManager = LinearLayoutManager(view.context)
        rvReviews.addItemDecoration(SpacingItemDecorator(1, 25, false))
        return view
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    fun refreshList() {
        reviewController.getReviewsFromUser(uidSearch, object: ReviewController.ReviewsQueryCallback {
            override fun onQueryComplete(result: ArrayList<AnimeReview>) {
                rvReviews.recycledViewPool.clear()
                val previousSize = rvReviewsAdapter.itemCount
                userReviews.clear()
                rvReviewsAdapter.notifyItemRangeRemoved(0, previousSize)
                userReviews.addAll(result)
                rvReviewsAdapter.notifyItemRangeInserted(0, userReviews.size)
            }
            override fun onQueryFailure(exception: Exception) {
                Log.e("PROFILE REVIEWS", exception.message.toString())
            }
        })
    }

}