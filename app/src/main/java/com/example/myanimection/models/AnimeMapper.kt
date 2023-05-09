package com.example.myanimection.models

import com.example.myanimection.SingleAnimeQuery

class AnimeMapper {
    fun ToCharacter(animeCharacter: SingleAnimeQuery.Node1?): AnimeCharacter {
        return AnimeCharacter(animeCharacter?.name.toString(), animeCharacter?.image.toString())
    }
}