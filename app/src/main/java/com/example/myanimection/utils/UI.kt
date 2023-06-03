package com.example.myanimection.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

/** Singleton que contiene métodos para facilitar el control sobre la interfaz de Android.*/
object UI {
    /**Método que permtie esconder el teclado manualmente desde un Fragment.*/
    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    /**Método que permtie esconder el teclado manualmente desde un Activity.*/
    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }
    /**Método que permtie esconder el teclado manualmente dada una vista y un contexto.
     * @param view  Vista desde la que quiere ocultar el teclado.
     */
    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /** Método que permite esconder el teclado manualmente en una vista.
     *
     */
    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}