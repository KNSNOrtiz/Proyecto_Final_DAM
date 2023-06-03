package com.example.myanimection.repositories

import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Optional
import com.example.myanimection.GetAnimeTitleQuery
import com.example.myanimection.PageAnimesQuery
import com.example.myanimection.SearchAnimesQuery
import com.example.myanimection.SingleAnimeQuery
import com.example.myanimection.type.MediaSort
import com.example.myanimection.utils.ApolloClient

/**
 * Clase encargada de lanzar las peticiones relacionadas a animes al cliente de Apollo.
 */
class AnimeMediaRepository {
    /**
         * Realiza una query de GraphQL de un solo anime a través del cliente de Apollo.
     *
     * @param id El ID del anime
     * @return ApolloResponse que contiene el resultado de la query.
     */
    suspend fun singleAnime(id: Optional<Int?>): ApolloResponse<SingleAnimeQuery.Data> {
        return ApolloClient.instance.query(SingleAnimeQuery(id)).execute()
    }

    /**
     * Realiza una query de GraphQL para buscar animes en base a unos criterios a través del cliente de Apollo.
     *
     * @param title Título del anime. Puede ser opcional.
     * @param genres Géneros del anime. Puede ser opcional.
     * @param sort Lista de criterios de ordenación del anime. Puede ser opcional.
     * @return ApolloResponse que contiene el resultado de la query.
     */
    suspend fun searchAnime(
        title: Optional<String>,
        genres: Optional<List<String>>,
        sort: Optional<List<MediaSort>>
    ): ApolloResponse<SearchAnimesQuery.Data> {
        return ApolloClient.instance.query(SearchAnimesQuery(title, genres, sort)).execute()
    }
    /**
     * Realiza una query de GraphQL para obtener el título de un anime a través del cliente de Apollo.
     *
     * @param id El ID del anime.
     * @return ApolloResponse que contiene el resultado de la query.
     */
    suspend fun animeTitle(id: Optional<Int?>): ApolloResponse<GetAnimeTitleQuery.Data> {
        return ApolloClient.instance.query(GetAnimeTitleQuery(id)).execute()
    }
    /**
     * Realiza una query paginando los animes a través del cliente de Apollo.
     *
     * @param page Número de página. Puede ser opcional.
     * @param perPage Cantidad de animes por página. Puede ser opcional.
     * @return ApolloResponse que contiene el resultado de la query.
     */
    suspend fun pageAnimes(
        page: Optional<Int?>,
        perPage: Optional<Int?>
    ): ApolloResponse<PageAnimesQuery.Data> {
        return ApolloClient.instance.query(PageAnimesQuery(page, perPage)).execute()
    }
}