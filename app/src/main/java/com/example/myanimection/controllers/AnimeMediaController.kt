package com.example.myanimection.controllers

import com.apollographql.apollo3.api.Optional
import com.example.myanimection.PageAnimesQuery
import com.example.myanimection.SearchAnimesQuery
import com.example.myanimection.models.AnimeMapper
import com.example.myanimection.models.AnimeMediaDetailed
import com.example.myanimection.repositories.AnimeMediaRepository
import com.example.myanimection.type.MediaSort
import com.example.myanimection.type.MediaStatus
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Clase controladora encargada de interactuar con [AnimeMediaRepository] y establecer funciones para obtener información detallada y transformada de los animes para poder ser
 * tratada. Las funciones son lanzadas mediante corutinas de Kotlin, es decir, de manera asíncrona.
 *
 * @param repository Repositorio que contiene las métodos para poder lanzar las consultas sobre anime de GraphQL mediante Apollo.
 */
class AnimeMediaController(private val repository: AnimeMediaRepository) {

    //  Clase que permite mapear algunos de los datos recibidos para poder ser tratados más apropiadamente.
    val animeMapper = AnimeMapper()


    /**
     * Obtiene información detallada de un anime a partir de su ID.
     *
     * @param id ID del anime a consultar.
     * @return Objeto [AnimeMediaDetailed] que contiene la información detallada del anime o null si no se encuentra.
     */
    suspend fun getSingleAnime(id: Optional<Int?>): AnimeMediaDetailed? {
        val response = repository.singleAnime(id).data?.Media

        //  Placeholders en caso de que ciertos campos del anime sean nulos.
        var romaji = "Desconocido"
        var native = "Desconocido"
        var studio = "Desconocido"
        var description = ""
        var startDate = "Desconocido"
        var endDate = "Desconocido"
        var status = ""
        if (response != null) {
            if (response.title?.romaji != null) {
                romaji = response.title.romaji
            }
            if (response.title?.native != null) {
                native = response.title.native
            }
            if (response.studios?.nodes != null && response.studios.nodes.isNotEmpty()) {
                studio = response.studios.nodes[0]?.name.toString()
            }
            if (response.startDate != null) {
                if (response.startDate.year != null && response.startDate.month != null && response.startDate.day != null) {
                    startDate = "${response.startDate.day}/${response.startDate.month}/${response.startDate.year}"
                }
            }
            if (response.endDate != null) {
                if (response.endDate.year != null && response.endDate.month != null && response.endDate.day != null) {
                    endDate = "${response.endDate.day}/${response.endDate.month}/${response.endDate.year}"
                }
            }
            if  (response.description != null) {
                //  El texto llega con formato HTML, así que lo intento sanear.
                description = response.description.replace(Regex("<br>"), "")
            }
            //  MediaStatus en un tipo de dato propio del esquema generado a partir de la API de GraphQL mediante Apollo.
            status = when(response.status) {
                MediaStatus.NOT_YET_RELEASED -> "Próximamente"
                MediaStatus.HIATUS -> "Pausado"
                MediaStatus.FINISHED -> "Finalizado"
                MediaStatus.CANCELLED -> "Cancelado"
                MediaStatus.RELEASING -> "En emisión"
                else -> "Desconocido"
            }

        }
        val animeResult: AnimeMediaDetailed? = if (response != null) {
            AnimeMediaDetailed(
                response.id,
                romaji,
                native,
                response.coverImage?.large.toString(),
                response.genres,
                description,
                studio,
                startDate,
                endDate,
                response.episodes,
                status,
                response.characters?.nodes?.map(animeMapper::ToCharacter)!!.toList(),
                response.streamingEpisodes
                )
        } else {
            null
        }
        return animeResult
    }
    /**
     * Obtiene una página de animes.
     *
     * @param page Página a obtener. Opcional.
     * @param perPage Cantidad de animes devueltos por página. Opcional.
     * @return Objeto [PageAnimesQuery.Page] que contiene la página de animes o null si no se encuentra.
     */
    suspend fun getPageAnimes(page: Optional<Int?>, perPage: Optional<Int?>): PageAnimesQuery.Page? {
        var response: PageAnimesQuery.Page? = null
        coroutineScope {
            launch {
                response = repository.pageAnimes(page, perPage).data?.Page
            }.join()
        }
        return response
    }

    /**
     * Realiza una búsqueda de animes dados un título, una lista de géneros y unos criterios de ordenación determinados.
     *
     * @param title Título del anime a buscar. Opcional.
     * @param genres Lista de géneros del anime a buscar. Opcional.
     * @param sort Lista de criterios bajo los que se ordenarán los animes obtenidos. Opcional.
     * @return Objeto [SearchAnimesQuery.Page] que contiene los resultados de la búsqueda o null si no se encuentra.
     */
    suspend fun getSearchAnimes(title: Optional<String>, genres: Optional<List<String>>, sort: Optional<List<MediaSort>>): SearchAnimesQuery.Page? {
        var response: SearchAnimesQuery.Page? = null
        coroutineScope {
            launch {
                response = repository.searchAnime(title, genres, sort).data?.Page
            }.join()
        }
        return response
    }
    /**
     * Obtiene el título de un anime dada una ID.
     *
     * @param id ID del anime del que se quiere obtener el título.
     * @return Título del anime en formato romaji (letras latinas).
     */
    suspend fun getAnimeTitle(id: Optional<Int?>): String? {
        var response: String? = null
        coroutineScope {
            launch {
                response = repository.animeTitle(id).data?.Media?.title?.romaji
            }.join()
        }
        return response
    }

}