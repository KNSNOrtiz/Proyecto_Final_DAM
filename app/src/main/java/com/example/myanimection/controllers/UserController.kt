package com.example.myanimection.controllers

import android.net.Uri
import android.util.Log
import com.example.myanimection.models.AnimeCategory
import com.example.myanimection.models.ListedAnimeMedia
import com.example.myanimection.models.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

/** Clase controladora encargada de establecer los distintos métodos y funciones para gestionar usuarios en Firestore.*/
class UserController {

    /*  Interfaces para crear callbacks y obtener los resultados de las operaciones según sea necesario.
        Se deben usar principalmente debido a la naturaleza asíncrona de todas las peticiones.
     */

    /**
     * Callback para recibir una lista de animes o una excepción cuando la operación termina.
     */
    interface ListedAnimeQueryCallback {
        /**
         * Invocado cuando la consulta de Firestore es exitosa.
         *
         * @param result  Lista de animes obtenida de la consulta.
         */
        fun onQueryComplete(result: ArrayList<ListedAnimeMedia>)
        /**
         * Invocado cuando ocurre un error durante la consulta de Firestore.
         *
         * @param exception  Excepción que representa el error ocurrido.
         */
        fun onQueryFailure(exception: Exception)
    }

    /**
     * Callback para recibir texto o una excepción cuando la operación termina.
     */
    interface StringQueryCallback {
        /**
         * Invocado cuando la consulta de Firestore es exitosa.
         *
         * @param result  Cadena de texto obtenida de la consulta.
         */
        fun onQueryComplete(result: String)
        /**
         * Invocado cuando ocurre un error durante la consulta de Firestore.
         *
         * @param exception  Excepción que representa el error ocurrido.
         */
        fun onQueryFailure(exception: Exception)
    }

    /** Callback que devuelve toda la información sobre un usuario de Firestore o una excepción en cuanto la operación termina.
     **/
    interface UserQueryCallback {
        /**
         * Invocado cuando la consulta de Firestore es exitosa.
         *
         * @param result  Objeto que contiene la información del usuario obtenida de la consulta.
         */
        fun onQueryComplete(result: User)
        /**
         * Invocado cuando ocurre un error durante la consulta de Firestore.
         *
         * @param exception  Excepción que representa el error ocurrido.
         */
        fun onQueryFailure(exception: Exception)
    }

    /** Callback que devuelve una lista con los datos de los usuarios o una excepción cuando la operación termina.*/
    interface UserListQueryCallback {
        /**
         * Invocado cuando la consulta de Firestore es exitosa.
         *
         * @param result  Lista de usuarios obtenida de la consulta.
         */
        fun onQueryComplete(result: ArrayList<User>)
        /**
         * Invocado cuando ocurre un error durante la consulta de Firestore.
         *
         * @param exception  Excepción que representa el error ocurrido.
         */
        fun onQueryFailure(exception: Exception)
    }

    //  Instancia de la librería que permite gestionar Firestore.
    private val db = Firebase.firestore
    //   Instancia de la librería que permite gestionar Firebase Storage para subir y obtener las imágenes de perfil de los usuarios.
    private val storage = Firebase.storage
    //  Referencia al directorio raíz de los archivos.
    private val storageRef = storage.reference

    //  Colección usuarios
    private val usersRef = db.collection("users")

    /**
     * Obtiene la referencia de Storage de la imagen de perfil del usuario en base a su UID.
     *
     * @param uid   UID del usuario.
     * @return      Referencia a Storage con la imagen de perfil del usuario.
     */
    fun getUserProfilePic(uid: String): StorageReference {
        return storageRef.child("images/profilepics/$uid.jpg")
    }

    /**
     * Sube a Storage una imagen local para ser empleada como imagen de perfil.
     *
     * @param uid       UID del usuario con el que se identificará la imagen.
     * @param newImage  URI de la nueva imagen de perfil.
     * @param callback  Callback para manejar el resultado de la consulta Firestore.
     */
    fun setUserProfilePic(uid: String, newImage: Uri, callback: FirestoreQueryCallback) {
        val imagesRef = storageRef.child("images/profilepics/$uid.jpg")
        //  Subida de imagen y respuesta del servidor en la nube.
        imagesRef.putFile(newImage)
            .addOnCompleteListener { result ->
                callback.onQueryComplete(result.isSuccessful)
            }
            .addOnFailureListener { exception ->
                callback.onQueryFailure(exception)
            }

    }

    /**  Obtiene un usuario buscandolo por su UID.
     * @param uid       UID del usuario a buscar.
     * @param callback  Callback para manejar el resultado de la consulta Firestore.
     */
    fun getUser(uid: String, callback: UserQueryCallback ) {
        usersRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    //  El método ToObject permite trabajar la información recibida con POJOs, pero es necesario tener constructores vacíos
                    val userData = document.toObject<User>()
                    callback.onQueryComplete(userData)
                }
            }
            .addOnFailureListener { exception ->
                callback.onQueryFailure(exception)
            }
    }

    /**  Obtiene el nombre de usuario de un usuario buscandolo por su UID.
     * @param uid       UID del usuario a buscar.
     * @param callback  Callback para manejar el resultado de la consulta Firestore.
     */
    fun getUserName(uid: String, callback: StringQueryCallback) {
        usersRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userData = document.toObject<User>()
                    callback.onQueryComplete(userData.userName)
                }
            }
            .addOnFailureListener { exception ->
                callback.onQueryFailure(exception)
            }
    }

    /**  Obtiene una lista de usuarios por su nombre de usuario. Esto se emplea en el buscador de usuarios
     *   donde es necesario filtrarlos por cómo empieza el usuario.
     *
     *  Las consultas de Firestore son case sensitive, por tanto es necesario almacenar un nombre de usuario en minúscula
     *  para poder encontrarlo de manera óptima.
     *  @param userName Cadena que contiene el nombre del usuario a buscar.
     *  @param callback Callback que devuelve los usuarios encontrados en la consulta Firestore.
     */
    fun getUsersByUsername(userName: String, callback: UserListQueryCallback) {
        val result = arrayListOf<User>()
        //  "'\uf8ff' es un comodín para indicar que puede terminar de cualquier manera mientras empiece por la cadena especificada.
        usersRef.orderBy("userNameLower").startAt(userName.lowercase()).endAt(userName.lowercase() +'\uf8ff').get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userData = document.toObject<User>()
                    result.add(userData)
                }
                callback.onQueryComplete(result)
            }
            .addOnFailureListener { exception ->
                callback.onQueryFailure(exception)
            }
    }

    /**  Consulta que devuelve una categoría específica de animes de un usuario.
     * Se obtiene la colección de animes que almacena el usuario y se devuelve la lista filtrada.
     *
     * @param uid           UID del usuario del que se obtendrán los animes.
     * @param animelist     Categoría con la que se identifica el anime en la colección, reconocido como "lista".
     * @param callback      Callback que devuelve los animes de la colección del usuari encontrados en la consulta Firestore.
     **/
    fun getUserAnimes(uid: String, animelist: AnimeCategory,  callback: ListedAnimeQueryCallback) {
        val userAnimes = arrayListOf<ListedAnimeMedia>()
        usersRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userData = document.toObject<User>()
                    Log.d("USER DATA", userData.toString())
                    val filteredList = userData.animeList.filter { anime -> anime.category == animelist  }
                    userAnimes.addAll(filteredList)
                    callback.onQueryComplete(userAnimes)
                }
            }
            .addOnFailureListener { exception ->
                callback.onQueryFailure(exception)
            }
    }


    /**  Añade un usuario a Firestore.
     * @param user      Objeto usuario a añadir.
     * @param callback  Callback para manejar el resultado de la consulta Firestore.
     */
    fun addUser(user: User, callback: FirestoreQueryCallback) {
        usersRef.add(user)
            .addOnCompleteListener { result ->
                callback.onQueryComplete(result.isSuccessful)
            }
            .addOnFailureListener { exception ->
                callback.onQueryFailure(exception)
            }
    }

    /**
     * Método que añade un "anime listado" a la colección de animes del usuario identificado por el UID pasado como parámetro.
     *
     * @param uid               UID del usuario al que se añadirá el anime listado.
     * @param listedAnimeMedia  Objeto ListedAnimeMedia que representa el anime listado a añadir.
     * @param callback          Callback para manejar el resultado de la consulta Firestore.
     */
    fun addAnime(uid: String, listedAnimeMedia: ListedAnimeMedia, callback: FirestoreQueryCallback) {
        usersRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                        document.reference.update("animeList", FieldValue.arrayUnion(listedAnimeMedia))
                            .addOnCompleteListener {
                                callback.onQueryComplete(it.isSuccessful)
                            }
                            .addOnFailureListener { exception ->
                                callback.onQueryFailure(exception)
                            }
                    }
                }
            .addOnFailureListener {
                Log.e("ADDANIMETOLIST ERROR", "No se pudo encontrar ningún documento del usuario $uid")
            }
    }

    /**
     * Método que actualiza un anime listado en la colección de animes de un usuario.
     *
     * @param uid               UID del usuario al que pertenece el anime listado.
     * @param listedAnimeMedia  Objeto ListedAnimeMedia que representa el anime listado actualizado.
     * @param callback          Callback para manejar el resultado de la consulta Firestore.
     */
    fun updateAnime(uid: String, listedAnimeMedia: ListedAnimeMedia, callback: FirestoreQueryCallback) {
        usersRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userData = document.toObject<User>()
                    val existingAnime: ListedAnimeMedia? = userData.animeList.find { anime -> anime.id == listedAnimeMedia.id }
                    if (existingAnime != null) {
                        existingAnime.category = listedAnimeMedia.category
                        existingAnime.watchedEpisodes = listedAnimeMedia.watchedEpisodes
                        document.reference.update("animeList", userData.animeList)
                            .addOnCompleteListener {
                                callback.onQueryComplete(it.isSuccessful)
                            }
                            .addOnFailureListener { exception ->
                                callback.onQueryFailure(exception)
                            }
                    }
                }
            }
            .addOnFailureListener {
                Log.e("ADDANIMETOLIST ERROR", "No se pudo encontrar ningún documento del usuario $uid")
            }
    }

    /**
     * Función para eliminar un anime listado de la colección de animes de un usuario.
     *
     * @param uid                   UID del usuario al que pertenece el anime listado.
     * @param listedAnimeMedia      Lista de objetos ListedAnimeMedia que representan los animes listados a eliminar.
     * @param callback              Callback para manejar el resultado de la consulta Firestore.
     */
    fun removeAnime(uid: String, listedAnimeMedia: ArrayList<ListedAnimeMedia>, callback: FirestoreQueryCallback) {
        usersRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    for (animeMedia in listedAnimeMedia) {
                        val userData = document.toObject<User>()
                        val existingAnime: ListedAnimeMedia? = userData.animeList.find { anime -> anime.id == animeMedia.id }
                        if (existingAnime != null) {
                            document.reference.update("animeList", FieldValue.arrayRemove(animeMedia))
                                .addOnCompleteListener {
                                    callback.onQueryComplete(it.isSuccessful)
                                }
                                .addOnFailureListener { exception ->
                                    callback.onQueryFailure(exception)
                                }
                        }
                    }
                }
            }
            .addOnFailureListener {
                Log.e("ADDANIMETOLIST ERROR", "No se pudo encontrar ningún documento del usuario $uid")
            }
    }

    /**
     * Función para establecer el nombre de usuario de un usuario en Firestore.
     *
     * @param uid                   UID del usuario al que se establecerá el nombre de usuario.
     * @param userName              Nombre de usuario a establecer.
     * @param callback              Callback para manejar el resultado de la consulta Firestore.
     */
    fun setUsername(uid: String, userName: String, callback: FirestoreQueryCallback) {
        usersRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.update("userName", userName, "userNameLower", userName.lowercase())
                        .addOnCompleteListener {
                            callback.onQueryComplete(it.isSuccessful)
                        }
                        .addOnFailureListener { exception ->
                            callback.onQueryFailure(exception)
                        }
                    }
                }
            .addOnFailureListener {
                Log.e("USER ERROR", "No se pudo encontrar ningún documento del usuario $uid")
            }
        }

    /**
     * Función para comprobar si un usuario está almacenado en la colección Users en Firestore.
     *
     * @param uid                   UID del usuario a comprobar.
     * @param callback              Callback para manejar el resultado de la consulta Firestore.
     */
    fun isUserRegistered(uid: String, callback: FirestoreQueryCallback) {
        Log.d("uid", uid)
        usersRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty && documents != null) {
                    callback.onQueryComplete(true)
                } else {
                    callback.onQueryComplete(false)
                }
            }
            .addOnFailureListener { exception ->
                callback.onQueryFailure(exception)
            }
    }

    /**
     * Función para comprobar si un nombre de usuario está siendo utilizado por otro usuario en Firestore.
     *
     * @param userName              Nombre de usuario a comprobar.
     * @param callback              Callback para manejar el resultado de la consulta Firestore.
     */

    fun isUsernameTaken(userName: String, callback: FirestoreQueryCallback) {
        usersRef.whereEqualTo("userName", userName).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty && documents != null) {
                    callback.onQueryComplete(true)
                } else {
                    callback.onQueryComplete(false)
                }
            }
            .addOnFailureListener { exception ->
                callback.onQueryFailure(exception)
            }
    }

    /**
     * Función para comprobar si un anime está dentro de la colección de animes de un usuario en Firestore.
     *
     * @param uid                   UID del usuario al que pertenece el anime listado.
     * @param animeId               ID del anime a comprobar.
     * @param callback              Callback para manejar el resultado de la consulta Firestore.
     */
    fun isAnimeListed(uid: String, animeId: Int, callback: FirestoreQueryCallback) {
        usersRef.whereEqualTo("uid", uid).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userData = document.toObject<User>()
                    val existingAnime: ListedAnimeMedia? = userData.animeList.find { anime -> anime.id == animeId }
                    callback.onQueryComplete(existingAnime != null)
                }
            }
            .addOnFailureListener {
                Log.e("ADDANIMETOLIST ERROR", "No se pudo encontrar ningún documento del usuario $uid")
            }
    }
}