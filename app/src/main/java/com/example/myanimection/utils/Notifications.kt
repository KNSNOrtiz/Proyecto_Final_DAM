package com.example.myanimection.utils

import android.app.Application
import android.content.Context
import android.widget.Toast
import com.afollestad.materialdialogs.DialogCallback
import com.afollestad.materialdialogs.MaterialDialog

object Notifications : Application() {

    val ANILIST_HOST = "https://graphql.anilist.co"

    fun shortToast(context : Context, message : String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    fun alertDialogOK(context: Context, title: String, message: String){
        MaterialDialog(context).show {
            title(text = title)
            message (text = message)
            positiveButton(text = "OK.")
        }
    }

    fun alertDialogOK(context: Context, title: String, message: String, positiveButtonClickListener: DialogCallback){
        MaterialDialog(context).show {
            title(text = title)
            message (text = message)
            positiveButton(text = "OK.", click = positiveButtonClickListener)
        }
    }

}