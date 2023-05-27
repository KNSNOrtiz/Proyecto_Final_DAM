package com.example.myanimection.controllers

import com.apollographql.apollo3.api.Optional
import com.example.myanimection.PageAnimesQuery
import com.example.myanimection.SearchAnimesQuery
import com.example.myanimection.models.AnimeMapper
import com.example.myanimection.models.AnimeMediaDetailed
import com.example.myanimection.repositories.AnimeMediaRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class AnimeMediaController(private val repository: AnimeMediaRepository) {

    val animeMapper = AnimeMapper()

    //  LANZAMIENTO DE COROUTINE (HILOS EN KOTLIN) CON LA CONSULTA.
    suspend fun getSingleAnime(id: Optional<Int?>): AnimeMediaDetailed? {
        val response = repository.singleAnime(id).data?.Media
        var romaji = "Desconocido"
        var native = "Desconocido"
        var studio = "Desconocido"
        var endDate = "No terminado"
        if (response != null) {
            if (response.title?.romaji != null) {
                romaji = response.title.romaji
            }
            if (response.title?.native != null) {
                native = response.title.native
            }
            if (response.studios?.nodes != null) {
                studio = response.studios.nodes[0]?.name.toString()
            }
            if (response.endDate != null) {
                if (response.endDate.year != null && response.endDate.month != null|| response.endDate.day != null) {
                    endDate = "${response.endDate.day}/${response.endDate.month}/${response.endDate.year}"
                }
            }

        }
        val animeResult: AnimeMediaDetailed? = if (response != null) {
            AnimeMediaDetailed(
                response.id,
                romaji,
                native,
                response.coverImage?.large.toString(),
                response.genres,
                response.description,
                studio,
                "${response.startDate?.day}/${response.startDate?.month}/${response.startDate?.year}",
                endDate,
                response.episodes,
                response.status,
                response.characters?.nodes?.map(animeMapper::ToCharacter)!!.toList(),
                response.streamingEpisodes
                )
        } else {
            null
        }
        return animeResult
    }

    suspend fun getPageAnimes(page: Optional<Int?>, perPage: Optional<Int?>): PageAnimesQuery.Page? {
        var response: PageAnimesQuery.Page? = null
        coroutineScope {
            launch {
                response = repository.pageAnimes(page, perPage).data?.Page
            }.join()
        }
        return response
    }

    suspend fun getSearchAnimes(title: Optional<String>, genres: Optional<List<String>>): SearchAnimesQuery.Page? {
        var response: SearchAnimesQuery.Page? = null
        coroutineScope {
            launch {
                response = repository.searchAnime(title, genres).data?.Page
            }.join()
        }
        return response
    }

}