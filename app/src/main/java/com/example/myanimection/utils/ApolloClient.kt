package com.example.myanimection.utils

import com.apollographql.apollo3.ApolloClient
import com.example.myanimection.utils.Utilities.ANILIST_HOST

/** Instancia del cliente de Apollo que permite lanzar consultas de GraphQL a la API.*/
object ApolloClient {
    val instance = ApolloClient.Builder().serverUrl(ANILIST_HOST).build()
}