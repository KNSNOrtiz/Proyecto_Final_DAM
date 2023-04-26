package com.example.myanimection.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.example.myanimection.R
import com.example.myanimection.models.AnimeMedia
import kotlinx.coroutines.Job

class RecyclerHomeAnimeAdapter(private val data: Array<AnimeMedia>): RecyclerView.Adapter<RecyclerHomeAnimeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_anime, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imgCover.load(data[position].bannerImageURl){
            crossfade(true)
            placeholder(R.mipmap.ic_launcher_round)
            transformations(CircleCropTransformation())
        }
        holder.txtEnglishTitle.text = data[position].englishTitle
        holder.txtJapaneseTitle.text = data[position].japaneseTitle
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imgCover: ImageView
        val txtEnglishTitle: TextView
        val txtJapaneseTitle: TextView

        init {
            imgCover = view.findViewById(R.id.imgCover)
            txtEnglishTitle = view.findViewById(R.id.txtTitleEnglish)
            txtJapaneseTitle = view.findViewById(R.id.txtTitleJapanese)
        }
    }
}