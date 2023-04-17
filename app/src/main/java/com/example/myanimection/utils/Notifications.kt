package com.example.myanimection.utils

import android.app.Application
import android.content.Context
import android.widget.Toast

object Notifications : Application() {
    fun  shortToast(context : Context, message : String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}