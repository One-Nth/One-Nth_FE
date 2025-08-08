package com.example.onenthapp.data.userset

import com.example.onenthapp.RetrofitInstance

class UserSetRepository {

    private val api = RetrofitInstance.usersetApi

    // 1. 알림 설정 전체 조회
    suspend fun getUserSettings() = api.getUserSettings()

    // 2. 스크랩 알림 on/off
    suspend fun updateScrapAlert(isEnabled: Boolean) =
        api.updateScrapAlert(EnabledRequest(isEnabled))

    // 3. 리뷰 알림 on/off
    suspend fun updateReviewAlert(isEnabled: Boolean) =
        api.updateReviewAlert(EnabledRequest(isEnabled))

    // 4. 채팅 알림 설정 on/off
    suspend fun updateChatAlert(isEnabled: Boolean) =
        api.updateChatAlert(EnabledRequest(isEnabled))

    // 5. 지역 키워드 등록
    suspend fun registerRegionKeyword(regionId: Int) =
        api.registerRegionKeyword(regionId)

    // 6. 지역 키워드 on/off
    suspend fun toggleRegionKeyword(regionKeywordAlertId: Int, isEnabled: Boolean) =
        api.toggleRegionKeyword(regionKeywordAlertId, EnabledRequest(isEnabled))

    // 7. 키워드 등록
    suspend fun registerKeyword(keyword: String) =
        api.registerKeyword(KeywordRequest(keyword))

    // 8. 키워드 on/off
    suspend fun toggleKeyword(keywordAlertId: Int, isEnabled: Boolean) =
        api.toggleKeyword(keywordAlertId, EnabledRequest(isEnabled))

    // 9. 키워드 삭제
    suspend fun deleteKeywords(productKeywordIds: List<Int>, regionKeywordIds: List<Int>) =
        api.deleteKeywords(DeleteKeywordsRequest(productKeywordIds, regionKeywordIds))

    // 10. 차단 목록 조회
    suspend fun getBlockedUsers() = api.getBlockedUsers()

    // 11. 차단 해제
    suspend fun unblockUser(blockedUserId: Int) =
        api.unblockUser(blockedUserId)
}
