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
import coil.transform.RoundedCornersTransformation
import com.example.myanimection.R
import com.example.myanimection.models.AnimeCharacter

class RecyclerCharacterAdapter(var data: ArrayList<AnimeCharacter>): RecyclerView.Adapter<RecyclerCharacterAdapter.ViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_character, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val circularProgressDrawable = CircularProgressDrawable(holder.itemView.context).apply {
            strokeWidth = 5f
            centerRadius = 30f
            start()
        }
        val request = ImageRequest.Builder(holder.itemView.context)
            .data(data[position].image)
            .placeholder(circularProgressDrawable)
            .transformations(RoundedCornersTransformation(50f))
            .target {
                holder.imgCharacter.setImageDrawable(it)
            }
            .build()
        Coil.imageLoader(holder.itemView.context).enqueue(request)
        holder.txtCharacterName.text = data[position].name
    }


    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imgCharacter: ImageView
        val txtCharacterName: TextView

        init {
            imgCharacter = view.findViewById(R.id.imgEpisodeThumbnail)
            txtCharacterName = view.findViewById(R.id.txtEpisodeName)
        }

    }
}