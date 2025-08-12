package com.example.onenthapp.data

data class UiProductCard(
    val tag: String,             // "같이 사요" / "함께 나눠요"
    val name: String,
    val info: String,            // "가격 10,000원 / 2개 / 원래 12,000원"
    val views: String,           // 조회수 스펙 없으니 임시 "-"
    val time: String,            // "3분 전"
    val badge: String? = null    // 겹치면 "내 게시글"
)

