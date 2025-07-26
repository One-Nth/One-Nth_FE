package com.example.onenthapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** 구매(같이 사요) vs 나눔(함께 나눠요) 구분 */
enum class SearchType { BUY, SHARE }

@Parcelize
data class SearchResult(
    val id: String,
    val title: String,
    val price: Int,
    val unit: String = "개",
    val category: String,
    val imageUrls: List<String>,       // 최대 3장만 표시
    val isBookmarked: Boolean = false,
    val type: SearchType               // BUY 또는 SHARE
) : Parcelable