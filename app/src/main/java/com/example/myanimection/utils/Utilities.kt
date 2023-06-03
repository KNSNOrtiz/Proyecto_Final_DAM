package com.example.myanimection.utils

/** Singleton que contiene constantes que serán usadas en la aplicación.
 * @property ANILIST_HOST       Cadena de texto con la dirección del Host de GraphQL de la API.
 * @property USERNAME_REGEX     Expresión regular empleada para validar nombres de usuario en la aplicación. De 5 a 15 caracteres alfanuméricos.
  */
object Utilities {
    const val ANILIST_HOST = "https://graphql.anilist.co"
    val USERNAME_REGEX = Regex("^[a-zA-Z0-9]{5,15}$")
}