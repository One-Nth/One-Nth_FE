package com.example.onenthapp.data

import retrofit2.Response
import retrofit2.http.DELETE

interface MemberApi {
    @DELETE("members/withdraw")
    suspend fun withdraw(): Response<CommonResponse<String>>
}
