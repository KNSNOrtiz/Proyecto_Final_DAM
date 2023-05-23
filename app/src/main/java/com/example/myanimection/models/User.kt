package com.example.myanimection.models

data class User(val uid: String, val userName: String, val email: String, val animeLists: UserLists){
    constructor(): this("", "", "", UserLists())
}
