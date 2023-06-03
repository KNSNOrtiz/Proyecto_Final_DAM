package com.example.myanimection.models

/**
 * Clase que representa un anime listado en una colección de un usuario. Cuenta con un constructor vacío para poder ser usada en consultas de Firestore.
 *
 * @property id ID del anime.
 * @property title Título romaji (romanizado).
 * @property thumbnail URL de la portada del anime.
 * @property watchedEpisodes Cantidad de episodios vistos del anime.
 * @property totalEpisodes Cantidad total de episodios del anime.
 * @property category [AnimeCategory] en la que se encuentra el anime para el usuario.
 */
data class ListedAnimeMedia(val id: Int, val title: String, val thumbnail: String, var watchedEpisodes: Int, val totalEpisodes: Int?, var category:  AnimeCategory)
{
    constructor():this(1, "", "", 0, 0, AnimeCategory.WATCHING)
}