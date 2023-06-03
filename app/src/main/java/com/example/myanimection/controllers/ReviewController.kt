package com.example.myanimection.controllers

import android.util.Log
import com.example.myanimection.models.AnimeReview
import com.example.myanimection.models.ListedAnimeMedia
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

/**
 * Clase encargada de proporcionar métodos para poder gestionar la colección Reviews en Firestore.
 */
class ReviewController {

    /**
     * Callback que devuelve una serie de reseñas tras una consulta.
     */
    interface ReviewsQueryCallback {
        /**
         * Se llama cuando la consulta de reseñas se completa.
         *
         * @param result Lista de objetos [AnimeReview] obtenidas de la consulta.
         */
        fun onQueryComplete(result: ArrayList<AnimeReview>)

        /**
         * Se llama cuando se da un error en la consulta.
         *
         * @param exception Excepción que representa el error ocurrido.
         */
        fun onQueryFailure(exception: Exception)
    }

    //  Instancia de la librería que permite gestionar Firestore.
    private val db = Firebase.firestore

    //  Referencia a la colección Reviews, donde están las reseñas sobre animes escritas por los usuarios.
    private val reviewsRef = db.collection("reviews")


    /**
     * Agrega una reseña a la colección.
     *
     * @param review La reseña a añadir.
     * @param callback  Callback para manejar el resultado de la consulta Firestore.
     */
    fun addReview(review: AnimeReview, callback: FirestoreQueryCallback) {
        reviewsRef.add(review)
            .addOnCompleteListener { result ->
                callback.onQueryComplete(result.isSuccessful)
            }
            .addOnFailureListener {exception -> callback.onQueryFailure(exception)}
    }

    /**
     * Obtiene las reseñas según el ID del anime.
     *
     * @param animeId El ID del anime del que se quieren obtener las reseñas.
     * @param callback  Callback para manejar el resultado de la consulta Firestore.
     */
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

    /**
     * Obtiene las reseñas de anime realizadas por un usuario según su UID.
     *
     * @param uid El UID del usuario del que se quieren obtener las reseñas.
     * @param callback  Callback para manejar el resultado de la consulta Firestore.
     */
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

    /**
     * Permite dar "Me gusta" o quitarlo en una reseña.
     *
     * @param currentUserUID El UID del usuario que está actualmente en la sesión.
     * @param review La reseña a la que se quiere dar o quitar un "Me gusta".
     */
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
    /**
     * Verifica si un usuario ha dado "Me gusta" a una reseña.
     *
     * @param currentUserUID El UID del usuario que está actualmente en la sesión.
     * @param likedUsers Lista de usuarios que han dado un "Me gusta" a la reseña.
     * @return True si el usuario ha dado un "Me gusta", False en caso de que no.
     */
    fun isLiked(currentUserUID: String, likedUsers: ArrayList<String>): Boolean {
        return (likedUsers.contains(currentUserUID))
    }

    /**
     * Borra una reseña.
     *
     * @param review La reseña que se quiere eliminar.
     * @param callback  Callback para manejar el resultado de la consulta Firestore.
     */
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

    /**
     *  Actualiza una reseña de la colección.
     *
     * @param review Objeto AnimeReview que representa la reseña actualizada.
     * @param callback  Callback para manejar el resultado de la consulta Firestore.
     */
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

    /**
     * Comprueba si una reseña existe o no.
     *
     * @param review La reseña de anime a buscar.
     * @param callback  Callback para manejar el resultado de la consulta Firestore.
     */
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