package com.example.videomeeting.firebase.database

import com.example.videomeeting.utilities.KEY_COLLECTION_USERS
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MyDatabase {

    private object Firestore{
        val instance by lazy { Firebase.firestore }
    }

    fun userReference(): CollectionReference {
        return Firestore.instance.collection(KEY_COLLECTION_USERS)
    }
}