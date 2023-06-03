package com.example.myanimection.controllers

import android.util.Log
import com.example.myanimection.models.AnimeReview
import com.example.myanimection.models.ListedAnimeMedia
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class ReviewController {

    interface ReviewsQueryCallback {
        fun onQueryComplete(result: ArrayList<AnimeReview>)
        fun onQueryFailure(exception: Exception)
    }

    private val db = Firebase.firestore
    private val reviewsRef = db.collection("reviews")

    fun addReview(review: AnimeReview, callback: FirestoreQueryCallback) {
        reviewsRef.add(review)
            .addOnCompleteListener { result ->
                callback.onQueryComplete(result.isSuccessful)
            }
            .addOnFailureListener {exception -> callback.onQueryFailure(exception)}
    }

    fun getReviewsFromAnime(animeId:Int, callback: ReviewsQueryCallback) {
        val reviewsResult = arrayListOf<AnimeReview>()
        reviewsRef.whereEqualTo("animeId", animeId).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val reviewData = document.toObject<AnimeReview>()
                    reviewsResult.add(reviewData)
                }
                callback.onQueryComplete(reviewsResult)
            }
            .addOnFailureListener { exception ->
                callback.onQueryFailure(exception)
            }
    }

    fun getReviewsFromUser(uid:String, callback: ReviewsQueryCallback) {
        val reviewsResult = arrayListOf<AnimeReview>()
        reviewsRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val reviewData = document.toObject<AnimeReview>()
                    reviewsResult.add(reviewData)
                }
                callback.onQueryComplete(reviewsResult)
            }
            .addOnFailureListener { exception ->
                callback.onQueryFailure(exception)
            }
    }

    fun likeReview(currentUserUID: String, review: AnimeReview) {
        reviewsRef.whereEqualTo("uid", review.uid).whereEqualTo("animeId", review.animeId).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val reviewData = document.toObject<AnimeReview>()
                    if (!isLiked(currentUserUID, reviewData.likes)) {
                        document.reference.update("likes", FieldValue.arrayUnion(currentUserUID))
                    } else {
                        document.reference.update("likes", FieldValue.arrayRemove(currentUserUID))
                    }
                }
            }
            .addOnFailureListener {
                Log.e("REVIEWS ERROR", "No se pudo encontrar ningún reseña.")
            }
    }

    fun isLiked(currentUserUID: String, likedUsers: ArrayList<String>): Boolean {
        return (likedUsers.contains(currentUserUID))
    }

    fun removeReview(review: AnimeReview, callback: FirestoreQueryCallback) {
        reviewsRef.whereEqualTo("uid", review.uid).whereEqualTo("animeId", review.animeId).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete().addOnCompleteListener { result ->
                        callback.onQueryComplete(result.isSuccessful)
                    }.addOnFailureListener { exception ->
                        callback.onQueryFailure(exception)
                    }
                }
            }
            .addOnFailureListener {  exception ->
                callback.onQueryFailure(exception)
            }
    }

    fun updateReview(review: AnimeReview, callback: FirestoreQueryCallback) {
        reviewsRef.whereEqualTo("uid", review.uid).whereEqualTo("animeId", review.animeId).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.update("title", review.title, "body", review.body, "score", review.score)
                        .addOnCompleteListener { result -> callback.onQueryComplete(result.isSuccessful) }
                        .addOnFailureListener { exception -> callback.onQueryFailure(exception) }
                }
            }
            .addOnFailureListener {  exception ->
                callback.onQueryFailure(exception)
            }
    }

    fun reviewExists(review: AnimeReview, callback: FirestoreQueryCallback) {
        reviewsRef.whereEqualTo("uid", review.uid).whereEqualTo("animeId", review.animeId).get()
            .addOnSuccessListener { documents ->
                callback.onQueryComplete(!documents.isEmpty)
            }
            .addOnFailureListener { exception ->
                callback.onQueryFailure(exception)
            }
    }
}