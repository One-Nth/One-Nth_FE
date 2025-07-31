// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("androidx.navigation.safeargs.kotlin") version "2.9.2" apply false
}
val compileSdkVersion by extra(34)
subprojects {
    configurations.configureEach {
        resolutionStrategy {
            // lifecycle-runtime-compose-android 모듈을 전역 2.8.0으로 강제
            force("androidx.lifecycle:lifecycle-runtime-compose-android:2.8.0")
        }
    }
}