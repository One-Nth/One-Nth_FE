package com.example.onenthapp.data.item

import com.example.onenthapp.RetrofitInstance
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class PlusRepository {
    private val api = RetrofitInstance.plusApi

    /** 같이 사요 */
    suspend fun createGroupPurchase(
        data: RequestBody,
        images: List<MultipartBody.Part>
    ): Response<BuyResponse> =
        api.createGroupPurchase(data, images)

    suspend fun fetchGroupPurchaseDetail(id: Long) =
        api.getGroupPurchaseDetail(id)
    /** 함께 나눠요 */
    suspend fun createSharingItem(
        data: RequestBody,
        images: List<MultipartBody.Part>
    ): Response<ShareResponse> =
        api.createSharingItem(data, images)

    suspend fun fetchSharingItemDetail(id: Long) =
        api.getSharingItemDetail(id)
}
