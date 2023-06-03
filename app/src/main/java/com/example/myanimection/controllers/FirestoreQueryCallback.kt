package com.example.myanimection.controllers

/**
 * Callback genérico para devolver el resultado de una operación (éxitosa o no exitosa) o una excepción cuando la operación termina en Firestore.
 */
interface FirestoreQueryCallback {
    /**
     * Invocado cuando la operacion de Firestore es completada.
     *
     * @param result  Booleano que determina si la operación ha sido exitosa o no
     */
    fun onQueryComplete(success: Boolean)
    /**
     * Invocado cuando ocurre un error durante la consulta de Firestore.
     *
     * @param exception  Excepción que representa el error ocurrido.
     */
    fun onQueryFailure(exception: Exception)
}