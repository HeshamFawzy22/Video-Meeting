package com.example.videomeeting.utilities

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences(KEY_PREFERENCE_NAME, Context.MODE_PRIVATE)

    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun putString(key:String , value: String){
        sharedPreferences.edit().putString(key,value).apply()
    }

    fun getString(key: String): String{
        return sharedPreferences.getString(key,null).toString()
    }

    fun clearPreferences(){
        sharedPreferences.edit().clear().apply()
    }
}