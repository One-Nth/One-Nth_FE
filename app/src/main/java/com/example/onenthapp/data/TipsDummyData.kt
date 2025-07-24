package com.example.onenthapp.data

import com.example.onenthapp.model.TipItem

object TipsDummyData {

    fun getLifeTipDummy(): List<TipItem> = listOf(
        TipItem("coffee", "식초와 페트병으로 만드는 트랩", "3분 전", 2, 1, 24),
        TipItem("Americano", "00모기약 어떤가요?", "5분 전", 1, 0, 15),
        TipItem("latte", "사용해보신 분 있나요?", "10분 전", 3, 2, 56),
        TipItem("water", "어디꺼가 제일 좋나요?", "15분 전", 1, 5, 88),
        TipItem("효과 좋은 모기약", "효과가 좋나요?", "20분 전", 0, 3, 42)
    )

    fun getCafeDummy(): List<TipItem> = listOf(
        TipItem("우리동네 카페", "분위기 좋은 카페 추천 받아요!", "2분 전", 3, 0, 18),
        TipItem("가성비 카페", "가격대비 괜찮은 곳 추천", "8분 전", 1, 2, 47),
        TipItem("디저트 맛집", "디저트가 맛있는 카페?", "12분 전", 2, 1, 33)
    )

    fun getDiscountDummy(): List<TipItem> = listOf(
        TipItem("쿠팡 할인", "에어프라이어 30% 할인", "1분 전", 5, 2, 61),
        TipItem("이마트 세일", "1+1 행사하는 품목 정리", "6분 전", 0, 1, 29),
        TipItem("GS25 꿀딜", "편의점 음료 할인 정보", "11분 전", 2, 2, 41)
    )
}
