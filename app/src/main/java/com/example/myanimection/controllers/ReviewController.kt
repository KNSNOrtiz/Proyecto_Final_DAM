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

    fun addReview(review: AnimeReview) {
        reviewsRef.add(review)
    }

    fun addReviewTest() {
        val review = AnimeReview(1, Firebase.auth.currentUser!!.uid, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean nec semper lorem.", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean nec semper lorem. Nulla quis lorem lorem. Praesent sit amet congue quam. Etiam et lorem ante. Donec sapien lacus, venenatis ac tellus ac, porta placerat neque. Maecenas scelerisque ipsum vel mauris lacinia, non eleifend sem ullamcorper. Sed vel augue pretium, lobortis diam egestas, lobortis ex. Nunc interdum dignissim sem ut bibendum. In laoreet risus id faucibus consequat. Aliquam scelerisque augue at sem finibus, tristique facilisis augue venenatis.\n" +
                "\n" +
                "Sed non urna odio. Donec congue sem a interdum scelerisque. Nam sem eros, dignissim nec mi vel, aliquam ornare odio. Etiam blandit quam tortor, eu eleifend felis pretium id. Aliquam hendrerit ut ex nec auctor. Etiam lacinia sit amet erat eu molestie. Morbi ut vehicula neque, vitae vehicula diam. Nullam rutrum justo ut imperdiet lobortis. Cras eget lectus convallis nisl sollicitudin consequat in ac dolor. Phasellus venenatis lectus lorem. Nunc quis eros et dui fringilla condimentum. Integer tortor erat, sollicitudin ac feugiat in, venenatis sit amet dui. Maecenas eros nisl, tempor eget posuere non, lobortis ac ante. Aenean sed tempor diam.\n" +
                "\n" +
                "Mauris imperdiet eu urna et ultrices. Etiam varius odio eget sapien mollis, vestibulum scelerisque neque mattis. Donec vehicula viverra tempor. Sed nec aliquet eros. Duis congue sodales ultricies. Nunc in leo in magna pretium congue. Nullam nibh purus, rhoncus a vestibulum quis, molestie non enim. Nam facilisis mauris sed mauris facilisis blandit. Duis vel sapien congue, sodales nunc ut, venenatis mauris. Duis blandit nisi tellus. Sed rhoncus eu arcu ac vulputate. Proin tempor odio ac nisi ultrices scelerisque. Donec blandit dolor mauris, a viverra libero molestie id. Vivamus ante odio, varius laoreet dolor auctor, hendrerit commodo libero.\n" +
                "\n" +
                "Aliquam et leo vitae odio lobortis ornare condimentum ac magna. In dignissim lorem nec odio finibus eleifend. Quisque at magna vel nisl scelerisque dictum. Proin rutrum mauris eu diam faucibus, quis laoreet mauris rhoncus. Nunc tincidunt tempor fringilla. Aenean elit justo, gravida ac eros feugiat, laoreet semper mauris. Nulla vitae nibh urna. Maecenas quis malesuada ipsum, quis tincidunt elit. Curabitur in metus efficitur, feugiat velit ut, malesuada magna. Cras ullamcorper eu odio congue varius. Nulla semper pulvinar aliquet. Pellentesque eleifend tristique felis, quis ultrices est vehicula sit amet. Morbi volutpat elit lectus, eu tempus urna feugiat in.\n" +
                "\n" +
                "Curabitur lacinia ac tortor eget rutrum. Aliquam viverra molestie eleifend. Donec porttitor ex a felis consequat interdum. Ut vel lectus accumsan libero imperdiet eleifend. Nunc ut viverra leo. Donec id libero dui. Donec posuere, elit quis consequat bibendum, libero urna elementum lorem, ut blandit diam metus nec dui. Ut gravida lorem est, viverra porta eros scelerisque vitae. Nam ut nibh id sem mattis faucibus. Aenean feugiat, augue a finibus scelerisque, tellus neque fermentum justo, eu facilisis justo augue ut arcu. Ut vel lobortis neque.",
            10, arrayListOf())
        addReview(review)
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
}