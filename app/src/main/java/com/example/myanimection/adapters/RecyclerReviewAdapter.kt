package com.example.myanimection.adapters

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.request.CachePolicy
import coil.transform.CircleCropTransformation
import com.apollographql.apollo3.api.Optional
import com.example.myanimection.R
import com.example.myanimection.controllers.AnimeMediaController
import com.example.myanimection.controllers.FirestoreQueryCallback
import com.example.myanimection.controllers.ReviewController
import com.example.myanimection.controllers.UserController
import com.example.myanimection.models.AnimeReview
import com.example.myanimection.repositories.AnimeMediaRepository
import com.example.myanimection.utils.Notifications
import com.example.myanimection.views.AddReviewFragment
import com.example.myanimection.views.AnimeDetailFragment
import com.example.myanimection.views.ProfileFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.webtoonscorp.android.readmore.ReadMoreTextView
import io.github.rosariopfernandes.firecoil.load
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecyclerReviewAdapter(var data: ArrayList<AnimeReview>, private var isUserProfile: Boolean): RecyclerView.Adapter<RecyclerReviewAdapter.ViewHolder>() {

    interface ReviewChangedListener {
        fun notifyRecyclerView()
    }

    var reviewChangedListener: ReviewChangedListener? = null

    private val userController = UserController()
    private val reviewController = ReviewController()
    private val animeMediaController = AnimeMediaController(AnimeMediaRepository())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = data[position]

        val circularProgressDrawable = CircularProgressDrawable(holder.itemView.context).apply {
            strokeWidth = 5f
            centerRadius = 30f
            start()
        }
        holder.imgUserPic.load(userController.getUserProfilePic(review.uid)) {
            placeholder(circularProgressDrawable)
            error(R.drawable.ic_profile)
            transformations(CircleCropTransformation())
            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.DISABLED)
        }
        holder.txtTitle.text = review.title
        holder.txtBody.text = review.body
        holder.txtLikes.text = "${review.likes.size}"
        holder.txtScore.text = "${review.score}"
        //  Petición a la API para obtener el nombre del anime, para añadir mayor contexto a las reseñas desde la pestaña de perfil.
        if (isUserProfile) {
            GlobalScope.launch(Dispatchers.IO) {
                val animeTitle = animeMediaController.getAnimeTitle(Optional.present(review.animeId))
                withContext(Dispatchers.Main) {
                    holder.txtAnimeTitle.text = animeTitle
                }
            }
            holder.txtAnimeTitle.setOnClickListener {
                loadAnimeFragment(holder.itemView.context, review.animeId)
            }
        }


        userController.getUserName(review.uid, object: UserController.StringQueryCallback {
            override fun onQueryComplete(result: String) {
                holder.txtUserName.text = result
            }

            override fun onQueryFailure(exception: Exception) {
                holder.txtUserName.text = "Anonymous"
            }
        })

        holder.cvReview.setOnLongClickListener { v ->
            //  Menú contextual para el contenedor de las reseñas.
            val popupMenu = PopupMenu(v.context, v)
            popupMenu.inflate(R.menu.menu_popup_review)
            val menu = popupMenu.menu
            val itemProfile = menu.findItem(R.id.itemReviewProfile)
            val itemEdit = menu.findItem(R.id.itemReviewEdit)
            val itemRemove = menu.findItem(R.id.itemReviewRemove)

            //  Seguridad de las reseñas de los usuarios. No se pueden borrar o editar si no eres dueño.
            if (Firebase.auth.currentUser?.uid != review.uid) {
                itemEdit.isVisible = false
                itemRemove.isVisible = false
            }
            //  Con tal de evitar un ciclo infinito de ventanas, limito la opción si estás desde el perfil.
            if (isUserProfile) {
                itemProfile.isVisible = false
            }

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when(menuItem.itemId) {
                    R.id.itemReviewProfile -> {
                        loadUserFragment(v.context, review.uid)
                        true
                    }
                    R.id.itemReviewEdit -> {
                        loadReviewFragment(v.context, review)
                        true
                    }
                    R.id.itemReviewRemove -> {
                        Notifications.alertDialogOK(v.context, "Eliminar reseña", "¿Estás seguro de querer borrarla?",
                        positiveButtonClickListener = { dialog ->
                            reviewController.removeReview(review, object: FirestoreQueryCallback {
                                override fun onQueryComplete(success: Boolean) {
                                    if (success) {
                                        Notifications.shortToast(v.context, "Reseña eliminada.")
                                        if (reviewChangedListener != null) {
                                            reviewChangedListener!!.notifyRecyclerView()
                                        }
                                    } else {
                                        Notifications.shortToast(v.context, "No se encontró la reseña.")
                                    }

                                }
                                override fun onQueryFailure(exception: Exception) {
                                    Notifications.shortToast(v.context, "Error al borrar la reseña.")
                                    Log.e("REVIEW", exception.message.toString())
                                }
                            })
                            dialog.dismiss()
                        },
                        negativeButtonClickListener = { dialog ->
                            dialog.dismiss()
                        })

                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
            true
        }


        /*
           En casos donde se necesite saber en todo momento la cantidad de Likes de los usuarios, sería ideal devolver la lista nueva en un callback con un get()
           tras el Update, pero de haber muchos usuarios y ser un plan gratuito, aumentaría exponecialmente la cantidad de peticiones y se alcanzaría el límite
           mucho más rápido, así que solamente se "mockea" ya que de todas formas cuando se vuelva a la vista, la lista siempre será la más nueva.
        */
        holder.btnLike.setOnClickListener {
            if (Firebase.auth.currentUser != null) {
                reviewController.likeReview(Firebase.auth.currentUser!!.uid, review)
                if (reviewController.isLiked(Firebase.auth.currentUser!!.uid, review.likes)) {
                    review.likes.removeIf { userUID -> userUID == Firebase.auth.currentUser!!.uid }
                    holder.btnLike.setImageResource(R.drawable.ic_like_neutral)
                } else {
                    review.likes.add(Firebase.auth.currentUser!!.uid)
                    holder.btnLike.setImageResource(R.drawable.ic_like_activated)
                }
                holder.txtLikes.text = "${review.likes.size}"
            }
        }

        if (reviewController.isLiked(Firebase.auth.currentUser!!.uid, review.likes)) {
            holder.btnLike.setImageResource(R.drawable.ic_like_activated)
        } else {
            holder.btnLike.setImageResource(R.drawable.ic_like_neutral)
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val cvReview: CardView
        val imgUserPic: ImageView
        val txtUserName: TextView
        val txtAnimeTitle: TextView
        val txtTitle: TextView
        val txtBody: ReadMoreTextView
        val txtLikes: TextView
        val txtScore: TextView
        val btnLike: ImageButton

        init {
            cvReview = view.findViewById(R.id.cvReview)
            imgUserPic = view.findViewById(R.id.imgReviewUserPic)
            txtUserName = view.findViewById(R.id.txtReviewUserName)
            txtAnimeTitle = view.findViewById(R.id.txtReviewAnime)
            txtTitle = view.findViewById(R.id.txtReviewTitle)
            txtBody = view.findViewById(R.id.txtReviewBody)
            txtLikes = view.findViewById(R.id.txtLikes)
            txtScore = view.findViewById(R.id.txtReviewScore)
            btnLike = view.findViewById(R.id.btnLike)
        }

    }

    private fun loadAnimeFragment(context: Context, animeMediaId: Int) {
        val fragment = AnimeDetailFragment()
        val bundle = Bundle()
        bundle.putInt("animeId", animeMediaId)
        fragment.arguments = bundle
        val navHostFragment = (context as AppCompatActivity)
            .supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.action_HomeFragment_to_animeDetailFragment, bundle)
    }
    private fun loadUserFragment(context: Context, uid: String) {
        val fragment = ProfileFragment()
        val bundle = Bundle()
        bundle.putString("uid", uid)
        fragment.arguments = bundle
        val navHostFragment = (context as AppCompatActivity)
            .supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.action_HomeFragment_to_ProfileFragment, bundle)
    }

    private fun loadReviewFragment(context: Context, review: AnimeReview) {
        val fragment = AddReviewFragment()
        val bundle = Bundle()
        val gson = Gson()
        val reviewJson = gson.toJson(review)
        bundle.putString("reviewData", reviewJson)
        fragment.arguments = bundle
        val navHostFragment = (context as AppCompatActivity)
            .supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.action_ReviewsFragment_to_AddReviewFragment, bundle)
    }
}