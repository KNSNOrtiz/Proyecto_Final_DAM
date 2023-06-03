package com.example.myanimection.models


/**
 * Clase que representa a un usuario en Firestore.
 *
 * @property uid ID del usuario.
 * @property userName Nombre de usuario.
 * @property email Correo electrónico del usuario.
 * @property animeList Lista de animes del usuario.
 * @property userNameLower Nombre de usuario en minúsculas para poder ser buscado en Firestore. Tiene un constructor vacío para poder trabajarse en las consultas del [UserController]
 */
data class User(val uid: String, val userName: String, val email: String, val animeList: ArrayList<ListedAnimeMedia>, var userNameLower: String){
    constructor(): this("", "", "", arrayListOf(), "")
    init {
        userNameLower = userName.lowercase()
    }
}
