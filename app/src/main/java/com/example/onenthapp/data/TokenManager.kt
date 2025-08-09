package com.example.onenthapp.util

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREFS_NAME = "app_prefs"
    private lateinit var prefs: SharedPreferences

    // ✅ 최종적으로 사용할 표준 키
    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"
    private const val KEY_MEMBER = "member_id"

    // 🔁 과거(혼용) 키 – 호환/마이그레이션용
    private const val LEGACY_KEY_TOKEN = "jwt_token"
    private const val LEGACY_KEY_REFRESH = "refresh_token" // 동일명 보호
    private const val LEGACY_KEY_MEMBER = "member_id"      // 동일명 보호
    private const val LEGACY_KEY_MEMBER_ID = "member_id"
    private const val LEGACY_KEY_REFRESH_TOKEN = "refresh_token"

    private const val KEY_NICKNAME = "nickname"

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        migrateIfNeeded()
    }

    /** ----------------- Access Token ----------------- */
    fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS, token).apply()
        // 과거 키도 동기화(선택): 필요 없으면 삭제
        prefs.edit().putString(LEGACY_KEY_TOKEN, token).apply()
    }

    fun getAccessToken(): String? {
        // 표준 키 우선, 없으면 레거시에서 끌어와서 표준 키로 복구
        val current = prefs.getString(KEY_ACCESS, null)
        if (!current.isNullOrEmpty()) return current

        val legacy = prefs.getString(LEGACY_KEY_TOKEN, null)
        if (!legacy.isNullOrEmpty()) {
            saveAccessToken(legacy)
            return legacy
        }
        return null
    }

    /** ----------------- Refresh Token ----------------- */
    fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH, token).apply()
        // 레거시 호환
        prefs.edit().putString(LEGACY_KEY_REFRESH_TOKEN, token).apply()
    }

    fun getRefreshToken(): String? {
        val current = prefs.getString(KEY_REFRESH, null)
        if (!current.isNullOrEmpty()) return current

        val legacy = prefs.getString(LEGACY_KEY_REFRESH_TOKEN, null)
        if (!legacy.isNullOrEmpty()) {
            saveRefreshToken(legacy)
            return legacy
        }
        return null
    }

    /** ----------------- Member ID ----------------- */
    fun saveMemberId(id: Long?) {
        prefs.edit().putLong(KEY_MEMBER, id ?: -1L).apply()
        // 레거시 호환
        prefs.edit().putLong(LEGACY_KEY_MEMBER_ID, id ?: -1L).apply()
    }

    fun getMemberId(): Long? {
        val cur = prefs.getLong(KEY_MEMBER, -1L)
        if (cur > 0) return cur

        val legacy = prefs.getLong(LEGACY_KEY_MEMBER_ID, -1L)
        return if (legacy > 0) {
            saveMemberId(legacy)
            legacy
        } else null
    }

    /** ----------------- Helpers ----------------- */
    fun isLoggedIn(): Boolean = !getAccessToken().isNullOrEmpty() && getMemberId() != null

    fun clearToken() {
        prefs.edit()
            // 표준 키
            .remove(KEY_ACCESS)
            .remove(KEY_REFRESH)
            .remove(KEY_MEMBER)
            .remove(KEY_NICKNAME)
            // 레거시 키들도 함께 제거
            .remove(LEGACY_KEY_TOKEN)
            .remove(LEGACY_KEY_REFRESH_TOKEN)
            .remove(LEGACY_KEY_MEMBER_ID)
            .apply()
    }

    /** 레거시 -> 표준 키 이관 */
    private fun migrateIfNeeded() {
        val legacyAccess = prefs.getString(LEGACY_KEY_TOKEN, null)
        if (!legacyAccess.isNullOrEmpty() && prefs.getString(KEY_ACCESS, null).isNullOrEmpty()) {
            prefs.edit().putString(KEY_ACCESS, legacyAccess).apply()
        }
        val legacyRefresh = prefs.getString(LEGACY_KEY_REFRESH_TOKEN, null)
        if (!legacyRefresh.isNullOrEmpty() && prefs.getString(KEY_REFRESH, null).isNullOrEmpty()) {
            prefs.edit().putString(KEY_REFRESH, legacyRefresh).apply()
        }
        val legacyMember = prefs.getLong(LEGACY_KEY_MEMBER_ID, -1L)
        if (legacyMember > 0 && prefs.getLong(KEY_MEMBER, -1L) <= 0) {
            prefs.edit().putLong(KEY_MEMBER, legacyMember).apply()
        }
    }


    fun saveNickname(nickname: String) {
        prefs.edit().putString(KEY_NICKNAME, nickname).apply()
    }

    fun getNickname(): String? = prefs.getString(KEY_NICKNAME, null)
}
