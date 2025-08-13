package com.example.onenthapp.data

import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

