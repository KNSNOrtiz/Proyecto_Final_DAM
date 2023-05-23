package com.example.myanimection.adapters

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.Coil
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.example.myanimection.R
import com.example.myanimection.models.ListedAnimeMedia
import com.example.myanimection.views.AnimeDetailFragment

class RecyclerListedAnimeAdapter (var data: ArrayList<ListedAnimeMedia>): RecyclerView.Adapter<RecyclerListedAnimeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_listed_anime, parent, false)
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
                holder.imgListedAnime.setImageDrawable(it)
            }
            .build()
        Coil.imageLoader(holder.itemView.context).enqueue(request)
        holder.txtRomajiTitle.text = data[position].title
        holder.imgListedAnime.setOnClickListener {
            loadFragment(holder.itemView.context, data[position].id)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imgListedAnime: ImageView
        val txtRomajiTitle: TextView
        val txtWatchedEpisodes: TextView
        val txtTotalEpisodes: TextView

        init {
            imgListedAnime = view.findViewById(R.id.imgListedAnimeImage)
            txtRomajiTitle = view.findViewById(R.id.txtListedAnimeRomaji)
            txtWatchedEpisodes = view.findViewById(R.id.txtListedAnimeWatched)
            txtTotalEpisodes = view.findViewById(R.id.txtListedAnimeTotal)
        }
    }

    private fun loadFragment(context: Context, animeMediaId: Int) {
        val fragment = AnimeDetailFragment()
        val bundle = Bundle()
        bundle.putInt("animeId", animeMediaId)
        fragment.arguments = bundle
        Log.d("ID LOADFRAGMENT", animeMediaId.toString())
        val navHostFragment = (context as AppCompatActivity)
            .supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.action_HomeFragment_to_animeDetailFragment, bundle)
    }
}