package com.example.myanimection.utils

import com.apollographql.apollo3.ApolloClient
import com.example.myanimection.utils.Utilities.ANILIST_HOST

//  SINGLETON QUE PERMITE ACCEDER A LA API DE GRAPHQL DESDE CUALQUIER PARTE.
object ApolloClient {
    val instance = ApolloClient.Builder().serverUrl(ANILIST_HOST).build()
}