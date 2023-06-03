package com.example.myanimection.models

import com.example.myanimection.SingleAnimeQuery
/**
 * Clase encargada de transformar objetos obtenidos en las consultas de anime.
 */

class AnimeMapper {
    /**
     * Mapea un  SingleAnimeQuery.Node1, donde Node es un objeto completo relacionado a otro en la API GraphQL, a AnimeCharacter.
     *
     * @param animeCharacter Objeto SingleAnimeQuery.Node1 a mapear.
     * @return [AnimeCharacter].
     */
    fun ToCharacter(animeCharacter: SingleAnimeQuery.Node1?): AnimeCharacter {
        return AnimeCharacter(animeCharacter?.name?.full.toString(), animeCharacter?.image?.medium.toString())
    }
}