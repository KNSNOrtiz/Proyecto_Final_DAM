package com.example.myanimection.models

data class AnimeReview(val animeId: Int, val uid: String, val title: String, val body: String, val score: Int, val likes: ArrayList<String> )
{
    constructor(): this(0, "", "", "", 0, arrayListOf())
}