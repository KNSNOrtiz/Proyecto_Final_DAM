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
import com.example.myanimection.models.AnimeMedia
import com.example.myanimection.views.AnimeDetailFragment

class RecyclerAnimeMediaAdapter(var data: ArrayList<AnimeMedia?>): RecyclerView.Adapter<RecyclerAnimeMediaAdapter.ViewHolder>() {


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
            .transformations(RoundedCornersTransformation(50f))
            .target {
                holder.imgCover.setImageDrawable(it)
            }
            .build()
        Coil.imageLoader(holder.itemView.context).enqueue(request)
        holder.txtNativeTitle.text = data[position]?.nativeTitle
        holder.txtRomajiTitle.text = data[position]?.romajiTitle
        holder.itemView.setOnClickListener {
            loadFragment(holder.itemView.context, data[position]?.id!!)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imgCover: ImageView
        val txtNativeTitle: TextView
        val txtRomajiTitle: TextView

        init {
            imgCover = view.findViewById(R.id.imgEpisodeThumbnail)
            txtNativeTitle = view.findViewById(R.id.txtTitleNativeAnimeMaster)
            txtRomajiTitle = view.findViewById(R.id.txtEpisodeName)
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