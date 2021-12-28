package com.example.videomeeting.model

import java.io.Serializable

data class User(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)

//data class UserWithToken(
//    val firstName: String,
//    val lastName: String,
//    val email: String,
//    val token: String
//)

data class UserWithToken(
    val firstName: String,
    val lastName: String,
    val email: String,
    val token: String
) : Serializable