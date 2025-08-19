package com.example.onenthapp.data.notificationboard

// app 내부 전용 UI 모델 (어댑터가 이걸 사용)
data class UiComment(
    val id: Int,
    val content: String,
    val nickname: String,
    val memberId: Int,
    val createdAt: String,          // 원본 문자열
    val profileImageUrl: String?    // 추가된 필드
)

