package com.example.myanimection.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
/**
 * Clase para agregar espacios entre los items de un RecyclerView.
 *
 * @param spanCount Número de columnas.
 * @param spacing Espacio entre los elementos.
 * @param includeEdge Indica si se deben incluir los espacios en los bordes.
 */
class SpacingItemDecorator(spanCount: Int, spacing: Int, includeEdge: Boolean): RecyclerView.ItemDecoration() {

    private val spanCount = spanCount
    private val spacing = spacing
    private val includeEdge = includeEdge

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        //  La columna se calcula con el módulo de la posición del item entre el número de columnas.
        val column = position % spanCount

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount;
            outRect.right = (column + 1) * spacing / spanCount;

            if (position < spanCount) {
                outRect.top = spacing;
            }
            outRect.bottom = spacing;
        } else {
            outRect.left = column * spacing / spanCount;
            outRect.right = spacing - (column + 1) * spacing / spanCount;
            if (position >= spanCount) {
                outRect.top = spacing;
            }
        }
    }
}