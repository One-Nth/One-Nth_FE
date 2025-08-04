package com.example.onenthapp


import android.app.Application
import com.example.onenthapp.util.TokenManager
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    companion object {
        lateinit var instance : GlobalApplication

    }
    override fun onCreate() {
        super.onCreate()
        instance = this

        //토큰 초기화
        TokenManager.init(this)

        // Kakao SDK 초기화
        val kakaoNativeAppKey = BuildConfig.APP_KEY
        android.util.Log.d("GlobalApplication", "Kakao Native App Key from BuildConfig: $kakaoNativeAppKey") // 로그 추가해서 실제 값 확인
        if (kakaoNativeAppKey.isBlank()) {

            android.util.Log.e("GlobalApplication", "Kakao Native App Key is not set or is a placeholder!")
        }

        // ✅ 카카오 로그인 SDK 초기화
        KakaoSdk.init(this, kakaoNativeAppKey)

        KakaoMapSdk.init(this, kakaoNativeAppKey)
        android.util.Log.d("GlobalApplication", "KakaoMapSdk initialized with key: $kakaoNativeAppKey")
    }
}
