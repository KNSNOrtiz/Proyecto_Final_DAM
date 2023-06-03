package com.example.myanimection.models

/**
 * Clase que representa un anime.
 *
 * @property id ID del anime.
 * @property romajiTitle Título en romaji (romanizado).
 * @property nativeTitle Título nativo (japonés).
 * @property bannerImageURL URL con la portada del anime.
 * @property genres Géneros del anime.
 */
data class AnimeMedia(
    val id: Int,
    val romajiTitle: String?,
    val nativeTitle: String?,
    val bannerImageURl: String?,
    val genres: List<String?>?
)
