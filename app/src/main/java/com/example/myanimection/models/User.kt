package com.example.myanimection.models

data class User(val uid: String, val userName: String, val email: String, val animeList: ArrayList<ListedAnimeMedia>){
    constructor(): this("", "", "", arrayListOf())
}
