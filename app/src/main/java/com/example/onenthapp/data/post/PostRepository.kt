package com.example.onenthapp.data.post

import android.util.Log
import com.example.onenthapp.data.MemberApi

class PostRepository(private val api: MemberApi) {
    suspend fun getMyPosts(page: Int, size: Int): Result<MyPostsPage> {
        return try {
            val resp = api.getMyPosts(page, size)
            if (resp.isSuccessful) {
                val body = resp.body()
                if (body?.isSuccess == true && body.result != null) {
                    Result.success(body.result)
                } else {
                    Result.failure(IllegalStateException(body?.message ?: "요청 실패"))
                }
            } else {
                val err = resp.errorBody()?.string()
                Log.e("API", "getMyPosts ${resp.code()} ${resp.message()} body=$err")
                Result.failure(IllegalStateException("HTTP ${resp.code()} ${resp.message()} ${err ?: ""}".trim()))
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun getMyScrapPosts(page: Int, size: Int): Result<MyPostsPage> {
        return try {
            val resp = api.getMyScrapPosts(page, size)
            if (resp.isSuccessful) {
                val body = resp.body()
                if (body?.isSuccess == true && body.result != null) {
                    Result.success(body.result)
                } else {
                    Result.failure(IllegalStateException(body?.message ?: "요청 실패"))
                }
            } else {
                val err = resp.errorBody()?.string()
                Log.e("API", "getMyScrapPosts ${resp.code()} body=$err")
                Result.failure(IllegalStateException("HTTP ${resp.code()} ${resp.message()} ${err ?: ""}".trim()))
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun getMyLikedPosts(page: Int, size: Int): Result<MyPostsPage> {
        return try {
            val resp = api.getMyLikedPosts(page, size)
            if (resp.isSuccessful) {
                val body = resp.body()
                if (body?.isSuccess == true && body.result != null) {
                    Result.success(body.result)
                } else {
                    Result.failure(IllegalStateException(body?.message ?: "요청 실패"))
                }
            } else {
                val err = resp.errorBody()?.string()
                Log.e("API", "getMyLikedPosts ${resp.code()} body=$err")
                Result.failure(IllegalStateException("HTTP ${resp.code()} ${resp.message()} ${err ?: ""}".trim()))
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
