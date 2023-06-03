package com.example.myanimection.utils

import android.app.Application
import android.content.Context
import android.widget.Toast
import com.afollestad.materialdialogs.DialogCallback
import com.afollestad.materialdialogs.MaterialDialog

/** Singleton que contiene distintos métodos para permitir la comunicación con el usuario.
 */
object Notifications : Application() {

    /** Método que muestra un mensaje durante un tiempo breve.
     * @param context       Contexto de la aplicación en donde se llama al Toast.
     * @param message       Cadena de texto que se muestra en el Toast.
     * */
    fun shortToast(context: Context, message : String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Muestra un diálogo de alerta de la librería MaterialDialog con un botón de "OK".
     *
     * @param context Contexto de la aplicación.
     * @param title Título del diálogo.
     * @param message Mensaje del diálogo.
     */
    fun alertDialogOK(context: Context, title: String, message: String){
        MaterialDialog(context).show {
            title(text = title)
            message (text = message)
            positiveButton(text = "OK")
        }
    }

    /**
     * Muestra un diálogo de alerta de la librería MaterialDialog con botones de "OK" y "NO".
     *
     * @param context Contexto de la aplicación.
     * @param title Título del diálogo.
     * @param message Mensaje del diálogo.
     * @param positiveButtonClickListener Acción a realizar cuando se presiona el botón "OK".
     * @param negativeButtonClickListener Acción a realizar cuando se presiona el botón "NO". Acepta valores nulos de no querer implementarse.
     */
    fun alertDialogOK(context: Context, title: String, message: String, positiveButtonClickListener: DialogCallback, negativeButtonClickListener: DialogCallback?){
        MaterialDialog(context).show {
            title(text = title)
            message (text = message)
            positiveButton(text = "OK", click = positiveButtonClickListener)
            negativeButton(text = "NO", click = negativeButtonClickListener)
        }
    }


}