package com.example.myanimection.utils

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.afollestad.materialdialogs.DialogCallback
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.example.myanimection.R
import com.example.myanimection.controllers.FirestoreQueryCallback
import com.example.myanimection.controllers.UserController
import com.example.myanimection.utils.Utilities.USERNAME_REGEX

object Notifications : Application() {


    fun shortToast(context: Context, message : String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


    fun alertDialogOK(context: Context, title: String, message: String){
        MaterialDialog(context).show {
            title(text = title)
            message (text = message)
            positiveButton(text = "OK")
        }
    }

    fun alertDialogOK(context: Context, title: String, message: String, positiveButtonClickListener: DialogCallback, negativeButtonClickListener: DialogCallback?){
        MaterialDialog(context).show {
            title(text = title)
            message (text = message)
            positiveButton(text = "OK", click = positiveButtonClickListener)
            negativeButton(text = "NO", click = negativeButtonClickListener)
        }
    }


}