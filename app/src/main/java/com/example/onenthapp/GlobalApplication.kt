package com.example.onenthapp


import android.app.Application
import com.kakao.vectormap.KakaoMapSdk

class GlobalApplication : Application() {
    companion object {
        lateinit var instance : GlobalApplication

    }
    override fun onCreate() {
        super.onCreate()
        instance = this

        // Kakao SDK 초기화
        val kakaoNativeAppKey = BuildConfig.APP_KEY
        android.util.Log.d("GlobalApplication", "Kakao Native App Key from BuildConfig: $kakaoNativeAppKey") // 로그 추가해서 실제 값 확인
        if (kakaoNativeAppKey.isBlank()) {

            android.util.Log.e("GlobalApplication", "Kakao Native App Key is not set or is a placeholder!")
        }

        KakaoMapSdk.init(this, kakaoNativeAppKey)
        android.util.Log.d("GlobalApplication", "KakaoMapSdk initialized with key: $kakaoNativeAppKey")
    }
}