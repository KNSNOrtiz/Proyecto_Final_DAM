package com.example.myanimection.models

data class ListedAnimeMedia(val id: Int, val title: String, val thumbnail: String, val watchedEpisodes: Int, val totalEpisodes: Int?)
{
    constructor():this(1, "", "", 0, 0)
}