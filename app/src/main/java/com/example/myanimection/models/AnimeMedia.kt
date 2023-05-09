package com.example.myanimection.models

import com.example.myanimection.type.MediaStatus

data class AnimeMedia(
    val id: Int,
    val romajiTitle: String?,
    val nativeTitle: String?,
    val bannerImageURl: String?,
    val genres: List<String?>?
)
