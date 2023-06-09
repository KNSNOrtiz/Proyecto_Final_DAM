package com.example.myanimection.adapters

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.Coil
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.example.myanimection.R
import com.example.myanimection.SingleAnimeQuery

/**
 * Adaptador para mostrar los episodios en CrunchyRoll de un anime, de haberlos.
 *
 * @property data Lista dinámica de los episodios de tipo [SingleAnimeQuery.StreamingEpisode] que se mostrarán en el RecyclerView.
 */
class RecyclerEpisodeAdapter (var data: ArrayList<SingleAnimeQuery.StreamingEpisode>): RecyclerView.Adapter<RecyclerEpisodeAdapter.ViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_episode, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val circularProgressDrawable = CircularProgressDrawable(holder.itemView.context).apply {
            strokeWidth = 5f
            centerRadius = 30f
            start()
        }
        val request = ImageRequest.Builder(holder.itemView.context)
            .data(data[position].thumbnail)
            .placeholder(circularProgressDrawable)
            .transformations(RoundedCornersTransformation(50f))
            .target {
                holder.imgThumbnail.setImageDrawable(it)
            }
            .build()
        Coil.imageLoader(holder.itemView.context).enqueue(request)
        holder.txtTitle.text = data[position].title
        holder.layoutEpisode.setOnClickListener {
            Log.d("CLICK", data[position].title!!)
            val httpIntent = Intent(Intent.ACTION_VIEW)
            httpIntent.data = Uri.parse(data[position].url)
            startActivity(holder.itemView.context, httpIntent, null)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val layoutEpisode: ConstraintLayout
        val imgThumbnail: ImageView
        val txtTitle: TextView

        init {
            layoutEpisode = view.findViewById(R.id.layoutEpisode)
            imgThumbnail = view.findViewById(R.id.imgEpisodeThumbnail)
            txtTitle = view.findViewById(R.id.txtEpisodeName)
        }

    }
}