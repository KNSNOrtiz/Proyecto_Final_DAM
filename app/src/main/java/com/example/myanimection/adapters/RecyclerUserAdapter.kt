package com.example.myanimection.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.request.CachePolicy
import coil.transform.CircleCropTransformation
import com.example.myanimection.R
import com.example.myanimection.controllers.UserController
import com.example.myanimection.models.User
import com.example.myanimection.views.AnimeDetailFragment
import io.github.rosariopfernandes.firecoil.load

class RecyclerUserAdapter(var data: ArrayList<User>): RecyclerView.Adapter<RecyclerUserAdapter.ViewHolder>() {
    val userController = UserController()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = data[position]

        val circularProgressDrawable = CircularProgressDrawable(holder.itemView.context).apply {
            strokeWidth = 5f
            centerRadius = 30f
            start()
        }
        holder.imgUserPic.load(userController.getUserProfilePic(user.uid)) {
            placeholder(circularProgressDrawable)
            error(R.drawable.ic_profile)
            transformations(CircleCropTransformation())
            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.DISABLED)
        }
        holder.txtUserName.text = user.userName
        holder.txtUserEmail.text = user.email

        holder.cvUser.setOnClickListener {
            loadFragment(holder.itemView.context, user.uid)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val cvUser: CardView
        val imgUserPic: ImageView
        val txtUserName: TextView
        val txtUserEmail: TextView

        init {
            cvUser = view.findViewById(R.id.cvHolderUser)
            imgUserPic = view.findViewById(R.id.imgHolderUserPic)
            txtUserName = view.findViewById(R.id.txtHolderUsername)
            txtUserEmail = view.findViewById(R.id.txtHolderEmail)
        }
    }

    private fun loadFragment(context: Context, uid: String) {
        val fragment = AnimeDetailFragment()
        val bundle = Bundle()
        bundle.putString("uid", uid)
        fragment.arguments = bundle
        val navHostFragment = (context as AppCompatActivity)
            .supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.action_HomeFragment_to_ProfileFragment, bundle)
    }
}