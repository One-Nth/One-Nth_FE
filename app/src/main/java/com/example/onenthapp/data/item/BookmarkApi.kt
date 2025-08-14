package com.example.onenthapp.data.item

import com.example.onenthapp.data.map.DeleteResponse
import com.example.onenthapp.data.map.GenericResponse
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface BookmarkApi {
    // 스크랩 등록
    @POST("group-purchases/{id}/scrap")
    suspend fun addPurchase(@Path("id") id: Long): GenericResponse<Unit>

    @POST("sharing-items/{id}/scrap")
    suspend fun addSharing(@Path("id") id: Long): GenericResponse<Unit>

    // 스크랩 삭제
    @DELETE("group-purchases/{id}/scrap")
    suspend fun removePurchase(
        @Path("id") itemId: Long
    ): DeleteResponse

    @DELETE("sharing-items/{id}/scrap")
    suspend fun removeSharing(
        @Path("id") itemId: Long
    ): DeleteResponse

}

