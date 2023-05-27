package com.example.myanimection.models

data class AnimeMedia(
    val id: Int,
    val romajiTitle: String?,
    val nativeTitle: String?,
    val bannerImageURl: String?,
    val genres: List<String?>?
)
