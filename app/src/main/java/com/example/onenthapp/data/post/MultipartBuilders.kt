package com.example.onenthapp.data.post

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

// post JSON 파트 만들기
fun buildPostJsonPart(payload: PostPayload): RequestBody {
    val json = Gson().toJson(payload)
    return json.toRequestBody("application/json; charset=utf-8".toMediaType())
}

// 이미지 파트들 만들기 (최대 5장)
fun Context.buildImageParts(
    uris: List<Uri>,
    partName: String = "images"
): List<MultipartBody.Part> {
    return uris.take(5).mapIndexed { idx, uri ->
        contentResolver.openInputStream(uri).use { ins ->
            val bytes = ins?.readBytes() ?: ByteArray(0)
            val body = bytes.toRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, "image_$idx.jpg", body)
        }
    }
}
