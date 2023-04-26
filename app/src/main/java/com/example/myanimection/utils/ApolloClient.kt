package com.example.myanimection.utils

import com.apollographql.apollo3.ApolloClient

//  SINGLETON QUE PERMITE ACCEDER A LA API DE GRAPHQL DESDE CUALQUIER PARTE.
object ApolloClient {
    val instance = ApolloClient.Builder().serverUrl(Notifications.ANILIST_HOST).build()
}