package com.example.nativemicrosite.storage

import android.content.Context

object TokenStorage {

    private const val PREF_NAME = "microsite_prefs"
    private const val KEY_TOKEN = "user_token"

    fun saveToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_TOKEN, "") ?: ""
    }
}
