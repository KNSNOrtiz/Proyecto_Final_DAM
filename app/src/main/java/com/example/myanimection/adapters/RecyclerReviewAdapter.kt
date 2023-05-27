package com.example.myanimection.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.transform.CircleCropTransformation
import com.example.myanimection.R
import com.example.myanimection.controllers.ReviewController
import com.example.myanimection.controllers.UserController
import com.example.myanimection.models.AnimeReview
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import io.github.rosariopfernandes.firecoil.load

class RecyclerReviewAdapter(var data: ArrayList<AnimeReview>): RecyclerView.Adapter<RecyclerReviewAdapter.ViewHolder>() {

    val userController = UserController()
    val reviewController = ReviewController()


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
        }
        holder.txtTitle.text = review.title
        holder.txtBody.text = review.body
        holder.txtLikes.text = "${review.likes.size}"

        val usernameCallback = object: UserController.StringQueryCallback {
            override fun onQueryComplete(result: String) {
                holder.txtUserName.text = result
            }

            override fun onQueryFailure(exception: Exception) {
                holder.txtUserName.text = "Anonymous"
            }
        }
        userController.getUserName(review.uid, usernameCallback)


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
        val imgUserPic: ImageView
        val txtUserName: TextView
        val txtTitle: TextView
        val txtBody: TextView
        val txtLikes: TextView
        val btnLike: ImageButton

        init {
            imgUserPic = view.findViewById(R.id.imgReviewUserPic)
            txtUserName = view.findViewById(R.id.txtReviewUserName)
            txtTitle = view.findViewById(R.id.txtReviewTitle)
            txtBody = view.findViewById(R.id.txtReviewBody)
            txtLikes = view.findViewById(R.id.txtLikes)
            btnLike = view.findViewById(R.id.btnLike)
        }
    }
}