package com.example.myanimection.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.Coil
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.example.myanimection.R
import com.example.myanimection.models.AnimeMedia

class RecyclerHomeAnimeAdapter(private var data: ArrayList<AnimeMedia?>): RecyclerView.Adapter<RecyclerHomeAnimeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_anime, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val circularProgressDrawable = CircularProgressDrawable(holder.itemView.context).apply {
            strokeWidth = 5f
            centerRadius = 30f
            start()
        }
        val request = ImageRequest.Builder(holder.itemView.context)
            .data(data[position]?.bannerImageURl)
            .placeholder(circularProgressDrawable)
            .transformations(CircleCropTransformation())
            .target {
                holder.imgCover.setImageDrawable(it)
            }
            .build()
        Coil.imageLoader(holder.itemView.context).enqueue(request)

        holder.txtNativeTitle.text = data[position]?.nativeTitle
        holder.txtRomajiTitle.text = data[position]?.romajiTitle
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun updateData(newAnimeList: ArrayList<AnimeMedia?>) {
        this.data = newAnimeList
        notifyItemRangeChanged(0, data.size)
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imgCover: ImageView
        val txtNativeTitle: TextView
        val txtRomajiTitle: TextView

        init {
            imgCover = view.findViewById(R.id.imgCover)
            txtNativeTitle = view.findViewById(R.id.txtTitleNative)
            txtRomajiTitle = view.findViewById(R.id.txtTitleRomaji)
        }
    }
}