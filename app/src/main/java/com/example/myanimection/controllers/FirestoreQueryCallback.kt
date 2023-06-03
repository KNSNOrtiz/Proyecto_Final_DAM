package com.example.myanimection.controllers

interface FirestoreQueryCallback {
    fun onQueryComplete(success: Boolean)
    fun onQueryFailure(exception: Exception)
}