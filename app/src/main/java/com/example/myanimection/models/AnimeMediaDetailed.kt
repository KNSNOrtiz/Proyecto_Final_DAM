package com.example.myanimection.models

import com.example.myanimection.SingleAnimeQuery
import com.example.myanimection.type.MediaStatus

data class AnimeMediaDetailed(
    val id: Int,
    val romajiTitle: String?,
    val nativeTitle: String?,
    val bannerImageURl: String?,
    val genres: List<String?>?,
    val description: String?,
    val animationStudio: String?,
    val startDate: String?,
    val endDate: String?,
    val episodes: Int?,
    val status: MediaStatus?,
    val characters: List<AnimeCharacter>,
    val streamingEpisode: List<SingleAnimeQuery.StreamingEpisode?>?
)