package com.example.myanimection.models

data class User(val uid: String, val userName: String, val email: String, val animeList: ArrayList<ListedAnimeMedia>, var userNameLower: String){
    constructor(): this("", "", "", arrayListOf(), "")
    init {
        userNameLower = userName.lowercase()
    }
}
