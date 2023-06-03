package com.example.myanimection.views

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myanimection.R
import com.example.myanimection.adapters.RecyclerUserAdapter
import com.example.myanimection.controllers.UserController
import com.example.myanimection.models.User
import com.example.myanimection.utils.Notifications
import com.example.myanimection.utils.SpacingItemDecorator

import java.util.*
import kotlin.collections.ArrayList


class SearchUserFragment : Fragment() {

    private lateinit var txtSearch: TextView
    private lateinit var rvUsers: RecyclerView
    private lateinit var rvUsersAdapter: RecyclerUserAdapter
    val users = arrayListOf<User>()
    val userController = UserController()
    var isSearchEnabled = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_user, container, false)
        txtSearch = view.findViewById(R.id.txtSearchUser)
        rvUsers = view.findViewById(R.id.rvSearchUser)

        txtSearch.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                isSearchEnabled = true
            }
        }

        txtSearch.addTextChangedListener (object: TextWatcher {
            private val DELAY_MILLIS = 500L
            private var timer: Timer? = null

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val userName = s.toString()
                timer?.cancel()
                timer = Timer()
                timer?.schedule(object : TimerTask() {
                    override fun run() {
                        if (s?.length!! > 0){
                            if (isSearchEnabled) {
                                userController.getUserByUsername(userName, object: UserController.UserListQueryCallback{
                                    override fun onQueryComplete(result: ArrayList<User>) {
                                        refreshRecyclerView(result)
                                    }
                                    override fun onQueryFailure(exception: Exception) {
                                        Notifications.shortToast(view.context, "No se han podido recuperar los usuarios.")
                                        Log.e("USER SEARCH", exception.message.toString())
                                    }
                                })
                            }
                        }
                    }
                }, DELAY_MILLIS)
            }

        })

        rvUsersAdapter = RecyclerUserAdapter(users)
        rvUsers.adapter = rvUsersAdapter
        rvUsers.layoutManager = LinearLayoutManager(view.context)
        rvUsers.addItemDecoration(SpacingItemDecorator(1, 25, false))

        return view
    }

    override fun onResume() {
        super.onResume()
        isSearchEnabled = false
    }

    private fun refreshRecyclerView(userSearch: ArrayList<User>) {
        rvUsers.recycledViewPool.clear()
        val previousSize = rvUsersAdapter.itemCount
        users.clear()
        rvUsersAdapter.notifyItemRangeRemoved(0, previousSize)
        users.addAll(userSearch)
        rvUsersAdapter.notifyItemRangeInserted(0, rvUsersAdapter.itemCount)
    }
}