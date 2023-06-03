package com.example.myanimection.controllers

import android.net.Uri
import android.util.Log
import com.example.myanimection.models.AnimeCategory
import com.example.myanimection.models.ListedAnimeMedia
import com.example.myanimection.models.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class UserController {
    interface ListedAnimeQueryCallback {
        fun onQueryComplete(result: ArrayList<ListedAnimeMedia>)
        fun onQueryFailure(exception: Exception)
    }

    interface StringQueryCallback {
        fun onQueryComplete(result: String)
        fun onQueryFailure(exception: Exception)
    }

    interface UserQueryCallback {
        fun onQueryComplete(result: User)
        fun onQueryFailure(exception: Exception)
    }

    interface UserListQueryCallback {
        fun onQueryComplete(result: ArrayList<User>)
        fun onQueryFailure(exception: Exception)
    }

    private val db = Firebase.firestore
    private val storage = Firebase.storage
    private val storageRef = storage.reference

    private val usersRef = db.collection("users")

    fun getUserProfilePic(uid: String): StorageReference {
        val timestamp = System.currentTimeMillis()
        return storageRef.child("images/profilepics/$uid.jpg")
    }

    fun setUserProfilePic(uid: String, newImage: Uri, callback: FirestoreQueryCallback) {
        newImage.lastPathSegment
        val imagesRef = storageRef.child("images/profilepics/$uid.jpg")
        imagesRef.putFile(newImage)
            .addOnCompleteListener { result ->
                callback.onQueryComplete(result.isSuccessful)
            }
            .addOnFailureListener { exception ->
                callback.onQueryFailure(exception)
            }

    }

    fun getUser(uid: String, callback: UserQueryCallback ) {
        usersRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userData = document.toObject<User>()
                    callback.onQueryComplete(userData)
                }
            }
            .addOnFailureListener { exception ->
                callback.onQueryFailure(exception)
            }
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

    fun getUserByUsername(userName: String, callback: UserListQueryCallback) {
        val result = arrayListOf<User>()
        usersRef.orderBy("userNameLower").startAt(userName.lowercase()).endAt(userName.lowercase() +'\uf8ff').get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userData = document.toObject<User>()
                    result.add(userData)
                }
                callback.onQueryComplete(result)
            }
            .addOnFailureListener { exception ->
                callback.onQueryFailure(exception)
            }
    }

    //  Consulta que devuelve una categoría específica de animes de un usuario y devuelve un resultado con un callback.
    fun getUserAnimes(uid: String, animelist: AnimeCategory,  callback: ListedAnimeQueryCallback) {
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


    fun addUser(user: User, callback: FirestoreQueryCallback) {
        usersRef.add(user)
            .addOnCompleteListener { result ->
                callback.onQueryComplete(result.isSuccessful)
            }
            .addOnFailureListener { exception ->
                callback.onQueryFailure(exception)
            }
    }

    fun addAnime(uid: String, listedAnimeMedia: ListedAnimeMedia, callback: FirestoreQueryCallback) {
        usersRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                        document.reference.update("animeList", FieldValue.arrayUnion(listedAnimeMedia))
                            .addOnCompleteListener {
                                callback.onQueryComplete(it.isSuccessful)
                            }
                            .addOnFailureListener { exception ->
                                callback.onQueryFailure(exception)
                            }
                    }
                }
            .addOnFailureListener {
                Log.e("ADDANIMETOLIST ERROR", "No se pudo encontrar ningún documento del usuario $uid")
            }
    }



    fun updateAnime(uid: String, listedAnimeMedia: ListedAnimeMedia, callback: FirestoreQueryCallback) {
        usersRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userData = document.toObject<User>()
                    val existingAnime: ListedAnimeMedia? = userData.animeList.find { anime -> anime.id == listedAnimeMedia.id }
                    if (existingAnime != null) {
                        existingAnime.category = listedAnimeMedia.category
                        existingAnime.watchedEpisodes = listedAnimeMedia.watchedEpisodes
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

    fun setUsername(uid: String, userName: String, callback: FirestoreQueryCallback) {
        usersRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.update("userName", userName, "userNameLower", userName.lowercase())
                        .addOnCompleteListener {
                            callback.onQueryComplete(it.isSuccessful)
                        }
                        .addOnFailureListener { exception ->
                            callback.onQueryFailure(exception)
                        }
                    }
                }
            .addOnFailureListener {
                Log.e("USER ERROR", "No se pudo encontrar ningún documento del usuario $uid")
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

    fun isUsernameTaken(userName: String, callback: FirestoreQueryCallback) {
        usersRef.whereEqualTo("userName", userName).get()
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

    fun isAnimeListed(uid: String, animeId: Int, callback: FirestoreQueryCallback) {
        usersRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userData = document.toObject<User>()
                    val existingAnime: ListedAnimeMedia? = userData.animeList.find { anime -> anime.id == animeId }
                    callback.onQueryComplete(existingAnime != null)
                }
            }
            .addOnFailureListener {
                Log.e("ADDANIMETOLIST ERROR", "No se pudo encontrar ningún documento del usuario $uid")
            }
    }
}