package com.example.myanimection.controllers

import android.util.Log
import com.example.myanimection.models.AnimeMedia
import com.example.myanimection.repositories.AnimeMediaRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AnimeMediaController {
    private val repository = AnimeMediaRepository()

    suspend fun getSingleAnime(): AnimeMedia {
        var animeResult: AnimeMedia
        //  LANZAMIENTO DE COROUTINE (HILOS EN KOTLIN) CON LA CONSULTA.
        /*runBlocking {
            launch {
                val response = repository.singleAnime().data?.Media
                animeResult = AnimeMedia(
                    response?.title?.english.toString(),
                    response?.title?.native.toString(),
                    response?.coverImage?.large.toString()
                )
                Log.d("AnimePOJO", animeResult.toString())
            }
        }.invokeOnCompletion {
            Log.d("COROUTINE", "FINISHED")
            return animeResult
        }*/
        val response = repository.singleAnime().data?.Media
        animeResult = AnimeMedia(
            response?.title?.english.toString(),
            response?.title?.native.toString(),
            response?.coverImage?.large.toString()
        )
        Log.d("AnimePOJO", animeResult.toString())
        return animeResult
    }
}