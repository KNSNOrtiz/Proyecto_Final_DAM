package com.example.myanimection.repositories

import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.example.myanimection.GetAnimeTitleQuery
import com.example.myanimection.PageAnimesQuery
import com.example.myanimection.SearchAnimesQuery
import com.example.myanimection.SingleAnimeQuery
import com.example.myanimection.type.MediaSort
import com.example.myanimection.utils.ApolloClient

//  Clase encargada de lanzar las peticiones sobre anime al cliente de Apollo.
class AnimeMediaRepository {

    suspend fun singleAnime(id: Optional<Int?>): ApolloResponse<SingleAnimeQuery.Data> {
        return ApolloClient.instance.query(SingleAnimeQuery(id)).execute()
    }

    suspend fun searchAnime(
        title: Optional<String>,
        genres: Optional<List<String>>,
        sort: Optional<List<MediaSort>>
    ): ApolloResponse<SearchAnimesQuery.Data> {
        return ApolloClient.instance.query(SearchAnimesQuery(title, genres, sort)).execute()
    }

    suspend fun animeTitle(id: Optional<Int?>): ApolloResponse<GetAnimeTitleQuery.Data> {
        return ApolloClient.instance.query(GetAnimeTitleQuery(id)).execute()
    }

    suspend fun pageAnimes(
        page: Optional<Int?>,
        perPage: Optional<Int?>
    ): ApolloResponse<PageAnimesQuery.Data> {
        return ApolloClient.instance.query(PageAnimesQuery(page, perPage)).execute()
    }
}