package com.example.myanimection.repositories

import android.util.Log
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.example.myanimection.PageAnimesQuery
import com.example.myanimection.SearchAnimesQuery
import com.example.myanimection.SingleAnimeQuery
import com.example.myanimection.utils.ApolloClient

//  Clase encargada de lanzar las peticiones sobre anime al cliente de Apollo.
class AnimeMediaRepository {

    suspend fun singleAnime(id: Optional<Int?>): ApolloResponse<SingleAnimeQuery.Data> {
        val response = ApolloClient.instance.query(SingleAnimeQuery(id)).execute()
        Log.d("AnimeQuery", "Success ${response.data?.Media}")
        return response
    }

    suspend fun searchAnime(title: Optional<String>, genres: Optional<List<String>>): ApolloResponse<SearchAnimesQuery.Data> {
        val response = ApolloClient.instance.query(SearchAnimesQuery(title, genres)).execute()
        return response
    }

    suspend fun pageAnimes(page: Optional<Int?>, perPage: Optional<Int?>): ApolloResponse<PageAnimesQuery.Data> {
        val response = ApolloClient.instance.query(PageAnimesQuery(page, perPage)).execute()
        Log.d("AnimeQuery", "Success ${response.data}")
        return response
    }
}