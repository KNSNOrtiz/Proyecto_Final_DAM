package com.example.myanimection.controllers

import android.util.Log
import com.example.myanimection.models.ListedAnimeMedia
import com.example.myanimection.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson

class UserController {

    interface FirestoreQueryCallback {
        fun onQueryComplete(success: Boolean)
        fun onQueryFailure(exception: Exception)
    }

    interface FirestoreListedAnimesQueryCallback {
        fun onQueryComplete(result: ArrayList<ListedAnimeMedia>)
        fun onQueryFailure(exception: Exception)
    }

    enum class ANIMELIST {
        WATCHING,
        PENDING,
        DROPPED,
        COMPLETED
    }

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val storageRef = storage.reference

    val usersRef = db.collection("users")

    fun getUserProfilePic(): StorageReference {
        return storageRef.child("images/profilepics/${Firebase.auth.currentUser?.uid}.jpg")
    }

    fun getUserAnimes(uid: String, animelist: ANIMELIST,  callback: FirestoreListedAnimesQueryCallback) {
        val userAnimes = arrayListOf<ListedAnimeMedia>()
        usersRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userData = document.toObject<User>()
                    when(animelist) {
                        ANIMELIST.WATCHING -> userAnimes.addAll(userData.animeLists.watching)
                        ANIMELIST.PENDING -> userAnimes.addAll(userData.animeLists.pending)
                        ANIMELIST.DROPPED -> userAnimes.addAll(userData.animeLists.dropped)
                        ANIMELIST.COMPLETED -> userAnimes.addAll(userData.animeLists.complete)
                    }
                    callback.onQueryComplete(userAnimes)
                }
            }
            .addOnFailureListener {
                callback.onQueryFailure(it)
            }
    }

    fun addUser(user: User) {
        Log.d("USER", user.toString())
        usersRef.add(user)
    }

    fun addAnimeToList(uid: String, listedAnimeMedia: ArrayList<ListedAnimeMedia>, animelist: ANIMELIST) {
        val animelistRoute = "animeLists.${animelist.toString().lowercase()}"
        usersRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    for (animeMedia in listedAnimeMedia) {
                        document.reference.update(animelistRoute, FieldValue.arrayUnion(animeMedia))
                            .addOnSuccessListener {
                                Log.d("Animes añadidos a $animelistRoute", "")
                            }
                            .addOnFailureListener {
                                Log.d("Animes no se pudieron añadir a $animelistRoute", "")
                            }
                    }
                }
            }
            .addOnFailureListener {
                Log.d("ADDANIMETOLIST ERROR", "No se pudo encontrar ningún documento del usuario $uid")
            }
    }


    fun isUserRegistered(uid: String, callback: FirestoreQueryCallback) {
        Log.d("uid", uid)
        usersRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty && documents != null) {
                    callback.onQueryComplete(true)
                } else {
                    callback.onQueryComplete(false)
                }
            }
            .addOnFailureListener { exception ->
                callback.onQueryFailure(exception)
            }
    }
}