package com.example.myanimection.controllers

import android.util.Log
import com.apollographql.apollo3.api.Optional
import com.example.myanimection.PageAnimesQuery
import com.example.myanimection.models.AnimeMedia
import com.example.myanimection.repositories.AnimeMediaRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class AnimeMediaController(private val repository: AnimeMediaRepository) {


    //  LANZAMIENTO DE COROUTINE (HILOS EN KOTLIN) CON LA CONSULTA.
    suspend fun getSingleAnime(): AnimeMedia? {
        val response = repository.singleAnime().data?.Media
        val animeResult: AnimeMedia? = if (response != null) {
            AnimeMedia(
                response.id,
                response.title?.english.toString(),
                response.title?.native.toString(),
                response.coverImage?.large.toString(),
               "${response.startDate?.day}-${response.startDate?.month}-${response.startDate?.year}",
                "${response.endDate?.day}-${response.endDate?.month}-${response.endDate?.year}",
                response.genres,
                response.episodes,
                response.status
                )
        } else {
            null
        }
        Log.d("AnimePOJO", animeResult.toString())
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

}