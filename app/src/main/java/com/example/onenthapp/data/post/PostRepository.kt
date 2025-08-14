package com.example.onenthapp.data.post

import android.util.Log
import com.example.onenthapp.data.MemberApi
import com.example.onenthapp.data.MyPageItemsResult
import com.example.onenthapp.data.MyPostProductItem
import com.example.onenthapp.util.TokenManager

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

    private fun tokenOrThrow(): String =
        TokenManager.getAccessToken()?.let { "Bearer $it" }
            ?: throw IllegalStateException("로그인이 필요합니다.")

    suspend fun getMyItems(page: Int, size: Int): Result<MyPageItemsResult> = runCatching {
        val res = api.getMyItems(tokenOrThrow(), page, size)
        res.result ?: error(res.message ?: "결과 없음")
    }

    /** itemType에 따라 올바른 삭제 API 호출 */
// PostRepository.kt
    suspend fun deleteMyItem(item: MyPostProductItem): Result<Unit> = runCatching {
        val bearer = tokenOrThrow()
        val t = item.itemType.trim().uppercase()

        val isPurchase = t.contains("PURCHASE") || t.contains("같이")
        val isShare    = t.contains("SHARE")    || t.contains("나눠")

        val resp = when {
            isPurchase -> api.deleteGroupPurchase(bearer, item.itemId)
            isShare    -> api.deleteSharingItem(bearer, item.itemId)
            else       -> error("알 수 없는 아이템 유형: ${item.itemType}")
        }
        if (resp.isSuccess != true) error(resp.message ?: "삭제 실패")
        Unit
    }


    suspend fun cancelMyLike(postId: Long): Result<Unit> = runCatching {
        val bearer = tokenOrThrow()
        val resp = api.cancelMyLikedPost(bearer, postId)
        if (!resp.isSuccessful ||
            resp.body()?.isSuccess != true ||
            resp.body()?.result?.isSuccess != true
        ) error(resp.body()?.message ?: "공감 취소 실패")
        Unit
    }

    suspend fun cancelMyScrapPost(postId: Long): Result<Unit> = runCatching {
        val resp = api.cancelMyScrapPost(tokenOrThrow(), postId)
        if (!resp.isSuccessful ||
            resp.body()?.isSuccess != true ||
            resp.body()?.result?.isSuccess != true
        ) {
            error(resp.body()?.message ?: "스크랩 취소 실패")
        }
        Unit
    }
}
