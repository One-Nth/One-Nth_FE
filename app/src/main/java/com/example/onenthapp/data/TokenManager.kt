package com.example.onenthapp.util

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREFS_NAME = "app_prefs"
    private lateinit var prefs: SharedPreferences

    // 표준 키
    private const val KEY_ACCESS = "access_token"
    private const val KEY_REFRESH = "refresh_token"
    private const val KEY_MEMBER = "member_id"
    private const val KEY_NICKNAME = "nickname"

    // 레거시 키(이관용)
    private const val LEGACY_KEY_TOKEN = "jwt_token"

    /** 반드시 Application.onCreate()에서 한 번 호출 */
    fun init(context: Context) {
        if (::prefs.isInitialized) return
        // TODO(보안): 추후 EncryptedSharedPreferences로 교체 고려
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        migrateIfNeeded()
    }

    /** 공용 editor helper */
    private inline fun edit(block: SharedPreferences.Editor.() -> Unit) {
        ensureInit()
        val e = prefs.edit()
        e.block()
        e.apply()
    }

    private fun ensureInit() {
        if (!::prefs.isInitialized) {
            throw IllegalStateException("TokenManager.init(context)가 먼저 호출되어야 합니다.")
        }
    }

    /** ----------------- Access Token ----------------- */
    fun saveAccessToken(token: String) {
        edit {
            putString(KEY_ACCESS, token)
            // 필요 시 레거시 키 동기화(완전 제거 가능)
            putString(LEGACY_KEY_TOKEN, token)
        }
    }

    fun getAccessToken(): String? {
        ensureInit()
        val current = prefs.getString(KEY_ACCESS, null)
        if (!current.isNullOrEmpty()) return current

        val legacy = prefs.getString(LEGACY_KEY_TOKEN, null)
        if (!legacy.isNullOrEmpty()) {
            // 표준 키로 이관
            saveAccessToken(legacy)
            return legacy
        }
        return null
    }

    /** Authorization 헤더 헬퍼 */
    fun authHeader(): String? = getAccessToken()?.let { "Bearer $it" }

    /** ----------------- Refresh Token ----------------- */
    fun saveRefreshToken(token: String) {
        edit { putString(KEY_REFRESH, token) }
    }

    fun getRefreshToken(): String? {
        ensureInit()
        return prefs.getString(KEY_REFRESH, null)
    }

    /** ----------------- Member ID ----------------- */
    fun saveMemberId(id: Long?) {
        edit { putLong(KEY_MEMBER, id ?: -1L) }
    }

    fun getMemberId(): Long? {
        ensureInit()
        val cur = prefs.getLong(KEY_MEMBER, -1L)
        return if (cur > 0) cur else null
    }

    /** ----------------- Nickname (캐시) -------------- */
    fun saveNickname(nickname: String) {
        edit { putString(KEY_NICKNAME, nickname) }
    }

    fun getNickname(): String? {
        ensureInit()
        return prefs.getString(KEY_NICKNAME, null)
    }

    /** ----------------- Utils ------------------------ */
    fun isLoggedIn(): Boolean = !getAccessToken().isNullOrEmpty() && getMemberId() != null

    fun clearToken() {
        edit {
            remove(KEY_ACCESS)
            remove(KEY_REFRESH)
            remove(KEY_MEMBER)
            remove(KEY_NICKNAME)
            remove(LEGACY_KEY_TOKEN)
        }
    }

    /** 레거시 -> 표준 키 이관 */
    private fun migrateIfNeeded() {
        // ensureInit() 불필요: init() 안에서만 호출됨
        val legacyAccess = prefs.getString(LEGACY_KEY_TOKEN, null)
        val hasCurrent = prefs.getString(KEY_ACCESS, null)
        if (!legacyAccess.isNullOrEmpty() && hasCurrent.isNullOrEmpty()) {
            edit { putString(KEY_ACCESS, legacyAccess) }
        }
    }
}
