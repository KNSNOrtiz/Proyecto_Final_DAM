package com.example.myanimection.repositories

import android.util.Log
import com.apollographql.apollo3.api.ApolloResponse
import com.example.myanimection.SingleAnimeQuery
import com.example.myanimection.utils.ApolloClient

//  Clase encargada de lanzar las peticiones sobre anime al cliente de Apollo.
class AnimeMediaRepository {
    suspend fun singleAnime(): ApolloResponse<SingleAnimeQuery.Data>{
        val response = ApolloClient.instance.query(SingleAnimeQuery()).execute()
        Log.d("AnimeQuery", "Success ${response.data}" )
        return response
    }
}