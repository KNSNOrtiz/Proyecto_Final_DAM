package com.example.myanimection.controllers

import android.provider.MediaStore.Audio.Genres
import android.util.Log
import com.apollographql.apollo3.api.Optional
import com.example.myanimection.PageAnimesQuery
import com.example.myanimection.SearchAnimesQuery
import com.example.myanimection.models.AnimeCharacter
import com.example.myanimection.models.AnimeMapper
import com.example.myanimection.models.AnimeMedia
import com.example.myanimection.models.AnimeMediaDetailed
import com.example.myanimection.repositories.AnimeMediaRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.streams.toList

class AnimeMediaController(private val repository: AnimeMediaRepository) {

    val animeMapper = AnimeMapper()

    //  LANZAMIENTO DE COROUTINE (HILOS EN KOTLIN) CON LA CONSULTA.
    suspend fun getSingleAnime(id: Optional<Int?>): AnimeMediaDetailed? {
        val response = repository.singleAnime(id).data?.Media
        val animeResult: AnimeMediaDetailed? = if (response != null) {
            AnimeMediaDetailed(
                response.id,
                response.title?.romaji.toString(),
                response.title?.native.toString(),
                response.coverImage?.large.toString(),
                response.genres,
                response.description,
                response.studios?.nodes
                    ?.filterNotNull()
                    ?.filter { it.isAnimationStudio }
                    ?.map { it.name }.toString(),
                "${response.startDate!!.day}/${response.startDate.month}/${response.startDate.year}",
                "${response.endDate!!.day}/${response.endDate.month}/${response.endDate.year}",
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