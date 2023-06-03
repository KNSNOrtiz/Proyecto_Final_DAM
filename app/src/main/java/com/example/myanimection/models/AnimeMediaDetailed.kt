package com.example.myanimection.models

import com.example.myanimection.SingleAnimeQuery

/**
 * Clase que representa un anime con información más detallada.
 *
 * @property id ID del anime.
 * @property romajiTitle Título en romaji (romanizado).
 * @property nativeTitle Título nativo (japonés).
 * @property bannerImageURL URL con la portada del anime.
 * @property genres Géneros del anime.
 * @property description Descripción del anime.
 * @property animationStudio Estudio de animación del anime.
 * @property startDate Fecha en la que se comenzó a emitir el anime.
 * @property endDate Fecha en la que se terminó de emitir el anime.
 * @property episodes Cantidad de episodios.
 * @property status Estado del anime. Obtenido de [MediaStatus], enum que determina el estado del anime en la API.
 * @property characters Personajes del anime.
 * @property streamingEpisodes Colección de episodios subidos a CrunchyRoll. Es un tipo de dato generado por Apollo a partir del esquema de la API.
 */
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
    val status: String,
    val characters: List<AnimeCharacter>,
    val streamingEpisodes: List<SingleAnimeQuery.StreamingEpisode?>?
)