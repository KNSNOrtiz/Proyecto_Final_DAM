package com.example.myanimection.controllers

import android.util.Log
import com.example.myanimection.models.AnimeCategory
import com.example.myanimection.models.ListedAnimeMedia
import com.example.myanimection.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class UserController {
    interface FirestoreListedAnimesQueryCallback {
        fun onQueryComplete(result: ArrayList<ListedAnimeMedia>)
        fun onQueryFailure(exception: Exception)
    }

    interface StringQueryCallback {
        fun onQueryComplete(result: String)
        fun onQueryFailure(exception: Exception)
    }

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val storageRef = storage.reference

    private val usersRef = db.collection("users")

    fun getLoggedUserProfilePic(): StorageReference {
        return storageRef.child("images/profilepics/${Firebase.auth.currentUser?.uid}.jpg")
    }

    fun getUserProfilePic(uid: String): StorageReference {
        return storageRef.child("images/profilepics/$uid.jpg")
    }

    fun getUserName(uid: String, callback: StringQueryCallback) {
        usersRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userData = document.toObject<User>()
                    callback.onQueryComplete(userData.userName)
                }
            }
            .addOnFailureListener { exception ->
                callback.onQueryFailure(exception)
            }
    }

    //  Consulta que devuelve una categoría específica de animes de un usuario y devuelve un resultado con un callback.
    fun getUserAnimes(uid: String, animelist: AnimeCategory,  callback: FirestoreListedAnimesQueryCallback) {
        val userAnimes = arrayListOf<ListedAnimeMedia>()
        usersRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userData = document.toObject<User>()
                    Log.d("USER DATA", userData.toString())
                    val filteredList = userData.animeList.filter { anime -> anime.category == animelist  }
                    userAnimes.addAll(filteredList)
                    callback.onQueryComplete(userAnimes)
                }
            }
            .addOnFailureListener { exception ->
                callback.onQueryFailure(exception)
            }
    }


    fun addUser(user: User) {
        usersRef.add(user)
    }

    fun addAnimeToList(uid: String, listedAnimeMedia: ArrayList<ListedAnimeMedia>, callback: FirestoreQueryCallback) {
        usersRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    for (animeMedia in listedAnimeMedia) {
                        val userData = document.toObject<User>()
                        val existingAnime: ListedAnimeMedia? = userData.animeList.find { anime -> anime.id == animeMedia.id }
                        if (existingAnime == null) {
                            document.reference.update("animeList", FieldValue.arrayUnion(animeMedia))
                                .addOnCompleteListener {
                                    callback.onQueryComplete(it.isSuccessful)
                                }
                                .addOnFailureListener { exception ->
                                    callback.onQueryFailure(exception)
                                }
                        } else {
                            existingAnime.category = animeMedia.category
                            existingAnime.watchedEpisodes = animeMedia.watchedEpisodes
                            document.reference.update("animeList", userData.animeList)
                                .addOnCompleteListener {
                                    callback.onQueryComplete(it.isSuccessful)
                                }
                                .addOnFailureListener { exception ->
                                    callback.onQueryFailure(exception)
                                }
                        }

                    }
                }
            }
            .addOnFailureListener {
                Log.e("ADDANIMETOLIST ERROR", "No se pudo encontrar ningún documento del usuario $uid")
            }
    }

    fun removeAnime(uid: String, listedAnimeMedia: ArrayList<ListedAnimeMedia>, callback: FirestoreQueryCallback) {
        usersRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    for (animeMedia in listedAnimeMedia) {
                        val userData = document.toObject<User>()
                        val existingAnime: ListedAnimeMedia? = userData.animeList.find { anime -> anime.id == animeMedia.id }
                        if (existingAnime != null) {
                            document.reference.update("animeList", FieldValue.arrayRemove(animeMedia))
                                .addOnCompleteListener {
                                    callback.onQueryComplete(it.isSuccessful)
                                }
                                .addOnFailureListener { exception ->
                                    callback.onQueryFailure(exception)
                                }
                        }
                    }
                }
            }
            .addOnFailureListener {
                Log.e("ADDANIMETOLIST ERROR", "No se pudo encontrar ningún documento del usuario $uid")
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