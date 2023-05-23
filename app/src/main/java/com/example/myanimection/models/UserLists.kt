package com.example.myanimection.models

data class UserLists (val watching: ArrayList<ListedAnimeMedia>, val pending: ArrayList<ListedAnimeMedia>, val complete: ArrayList<ListedAnimeMedia>, val dropped: ArrayList<ListedAnimeMedia>)
{
    constructor(): this(arrayListOf(),arrayListOf(),arrayListOf(),arrayListOf())
}