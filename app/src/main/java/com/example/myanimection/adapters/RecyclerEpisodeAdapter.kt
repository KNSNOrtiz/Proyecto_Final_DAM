package com.example.myanimection.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.Coil
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.example.myanimection.R
import com.example.myanimection.SingleAnimeQuery


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
        holder.itemView.setOnClickListener {
            val httpIntent = Intent(Intent.ACTION_VIEW)
            httpIntent.data = Uri.parse(data[position].url)
            startActivity(holder.itemView.context, httpIntent, null)
        }
    }


    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imgThumbnail: ImageView
        val txtTitle: TextView

        init {
            imgThumbnail = view.findViewById(R.id.imgEpisodeThumbnail)
            txtTitle = view.findViewById(R.id.txtEpisodeName)
        }

    }
}