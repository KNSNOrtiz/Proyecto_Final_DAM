package com.example.myanimection.adapters

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.Coil
import coil.load
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.example.myanimection.R
import com.example.myanimection.controllers.FirestoreQueryCallback
import com.example.myanimection.controllers.UserController
import com.example.myanimection.models.AnimeCategory
import com.example.myanimection.models.ListedAnimeMedia
import com.example.myanimection.utils.Notifications
import com.example.myanimection.utils.UI.hideKeyboard
import com.example.myanimection.utils.Utilities
import com.example.myanimection.views.AnimeDetailFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class RecyclerListedAnimeAdapter (var data: ArrayList<ListedAnimeMedia>): RecyclerView.Adapter<RecyclerListedAnimeAdapter.ViewHolder>() {

    interface CategoryChangedListener {
        fun notifyRecyclerView()
    }

    private val userController = UserController()

    var categoryChangedListener: CategoryChangedListener? = null

    //  Método que llama al método de la interfaz insertada desde ProfileAnimesFragment para refrescar la lista en cuanto la operación es exitosa.
    private val queryCallback = object: FirestoreQueryCallback {
        override fun onQueryComplete(success: Boolean) {
            if (categoryChangedListener != null && success) {
                Log.d("QUERY EXITO", success.toString())
                categoryChangedListener!!.notifyRecyclerView()
            } else{
                Log.d("QUERY EXITO", success.toString())
            }
        }

        override fun onQueryFailure(exception: Exception) {
                Log.d("QUERY EXCEPCION", exception.stackTraceToString())
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_listed_anime, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val listedAnime = data[position]

        val circularProgressDrawable = CircularProgressDrawable(holder.itemView.context).apply {
            strokeWidth = 5f
            centerRadius = 30f
            start()
        }
        val request = ImageRequest.Builder(holder.itemView.context)
            .data(listedAnime.thumbnail)
            .placeholder(circularProgressDrawable)
            .transformations(RoundedCornersTransformation(50f))
            .target {
                holder.imgListedAnime.setImageDrawable(it)
            }
            .build()
        Coil.imageLoader(holder.itemView.context).enqueue(request)


        holder.txtRomajiTitle.text = listedAnime.title
        if (listedAnime.totalEpisodes != null) {
            holder.txtTotalEpisodes.text = "${listedAnime.totalEpisodes}"
        } else {
            holder.txtTotalEpisodes.text = "?"
        }
        holder.txtWatchedEpisodes.text = "${listedAnime.watchedEpisodes}"
        if (listedAnime.category == AnimeCategory.COMPLETED) {
            holder.txtWatchedEpisodes.inputType = InputType.TYPE_NULL
        }
        holder.txtWatchedEpisodes.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                val userController = UserController()
                if (Firebase.auth.currentUser != null) {
                    listedAnime.watchedEpisodes = holder.txtWatchedEpisodes.text.toString().toInt()
                    if (listedAnime.totalEpisodes != null && listedAnime.watchedEpisodes >= listedAnime.totalEpisodes && listedAnime.category != AnimeCategory.COMPLETED) {
                        listedAnime.watchedEpisodes = listedAnime.totalEpisodes
                        Notifications.alertDialogOK(holder.itemView.context, "Has visto todos los episodios", "¿Quieres marcar el anime como completado?",
                            positiveButtonClickListener = {
                                listedAnime.category = AnimeCategory.COMPLETED
                                userController.addAnimeToList(Firebase.auth.currentUser!!.uid, arrayListOf(listedAnime), queryCallback)
                            },
                            negativeButtonClickListener = {
                                userController.addAnimeToList(Firebase.auth.currentUser!!.uid, arrayListOf(listedAnime), queryCallback)
                            })
                    } else {
                        userController.addAnimeToList(Firebase.auth.currentUser!!.uid, arrayListOf(listedAnime), queryCallback)
                        Log.d("ANIME UPDATED", listedAnime.toString())
                    }
                }
                v.clearFocus()
                v.hideKeyboard()
                return@OnKeyListener true
            }
            false
        })

        holder.cvListedAnime.setOnLongClickListener { v ->
            val popupMenu = PopupMenu(v.context, v)
            popupMenu.inflate(R.menu.menu_popup_listedanime)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when(menuItem.itemId) {
                    R.id.itemListedAnimeDetails -> {
                        loadFragment(v.context, listedAnime.id)
                        true
                    }
                    R.id.itemListedAnimeEdit -> {
                        val dialog = MaterialDialog(v.context)
                            .noAutoDismiss()
                            .customView(R.layout.dialog_add_anime)

                        dialog.findViewById<ImageView>(R.id.imgAddAnimeImage).load(holder.imgListedAnime.drawable) {transformations(
                            CircleCropTransformation()
                        )}
                        dialog.findViewById<TextView>(R.id.txtAddAnimeTitle).text = listedAnime.title
                        val dialogWatchedEpisodes = dialog.findViewById<TextView>(R.id.txtAddAnimeWatched).apply { text = "${listedAnime.watchedEpisodes}" }
                        dialog.findViewById<TextView>(R.id.txtAddAnimeTotal).text = holder.txtTotalEpisodes.text
                        val dialogSpinner = dialog.findViewById<Spinner>(R.id.spinAddAnimeCategory)
                        ArrayAdapter.createFromResource(v.context, R.array.listed_categories, R.layout.ani_spin_item).also { adapter ->
                            adapter.setDropDownViewResource(R.layout.ani_spin_dropdown_item)
                            dialogSpinner.adapter = adapter
                        }
                        dialog.findViewById<TextView>(R.id.txtAddAnimePositive).setOnClickListener {
                            var watchedEpisodes = 0
                            if (Firebase.auth.currentUser != null) {
                                try {
                                    watchedEpisodes = dialogWatchedEpisodes.text.toString().toInt()
                                } catch (ex: NumberFormatException) {
                                    watchedEpisodes  = 0
                                }
                                if (listedAnime.totalEpisodes != null) {
                                    if (watchedEpisodes  >= listedAnime.totalEpisodes) {
                                        watchedEpisodes = listedAnime.totalEpisodes
                                    }
                                }
                                dialogWatchedEpisodes.text = "$watchedEpisodes"
                                userController.addAnimeToList(Firebase.auth.currentUser!!.uid, arrayListOf(
                                    ListedAnimeMedia(listedAnime.id,
                                        listedAnime.title,
                                        listedAnime.thumbnail,
                                        watchedEpisodes, listedAnime.totalEpisodes,
                                        AnimeCategory.valueOf(dialogSpinner.selectedItem.toString()))),
                                    queryCallback)

                            }
                            dialog.dismiss()
                        }

                        dialog.findViewById<TextView>(R.id.txtAddAnimeNegative).setOnClickListener {
                            dialog.dismiss()
                        }
                        dialog.show()
                        true
                    }
                    R.id.itemListedAnimeRemove -> {
                        if (Firebase.auth.currentUser != null) {
                            userController.removeAnime(Firebase.auth.currentUser!!.uid, arrayListOf(listedAnime), queryCallback)
                        }
                        true
                    }
                    else -> true
                }
            }
            popupMenu.show()
            true
        }

        holder.imgListedAnime.setOnClickListener {
            loadFragment(holder.itemView.context, listedAnime.id)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val cvListedAnime: CardView
        val imgListedAnime: ImageView
        val txtRomajiTitle: TextView
        val txtWatchedEpisodes: TextView
        val txtTotalEpisodes: TextView

        init {
            cvListedAnime = view.findViewById(R.id.cvListedAnime)
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