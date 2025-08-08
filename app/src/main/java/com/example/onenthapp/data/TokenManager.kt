package com.example.onenthapp.util

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_TOKEN = "jwt_token"
    private lateinit var prefs: SharedPreferences

    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"
    private const val KEY_MEMBER = "member_id"
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String {
        return prefs.getString(KEY_TOKEN, "") ?: ""
    }

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    private const val KEY_MEMBER_ID = "member_id"

    fun saveMemberId(id: Long) {
        prefs.edit().putLong(KEY_MEMBER_ID, id).apply()
    }

    fun getMemberId(): Long? {
        val id = prefs.getLong(KEY_MEMBER_ID, -1)
        return if (id == -1L) null else id
    }


    private const val KEY_REFRESH_TOKEN = "refresh_token"

    fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun clearAll() { // ← 새로 추가
        prefs.edit()
            .remove(KEY_ACCESS)
            .remove(KEY_REFRESH)
            .remove(KEY_MEMBER)
            .apply()
    }
}
