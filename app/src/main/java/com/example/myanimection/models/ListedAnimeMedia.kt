package com.example.myanimection.models

data class ListedAnimeMedia(val id: Int, val title: String, val thumbnail: String, var watchedEpisodes: Int, val totalEpisodes: Int?, var category:  AnimeCategory)
{
    constructor():this(1, "", "", 0, 0, AnimeCategory.WATCHING)
}