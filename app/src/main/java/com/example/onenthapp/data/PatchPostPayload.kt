package com.example.onenthapp.data
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

data class PatchPostPayload(
    val title: String = "",
    val content: String = "",
    val address: String = "",
    val placeName: String = "",
    val link: String = "",
    val tags: List<String> = emptyList()
)

fun PatchPostPayload.toPart(): RequestBody {
    val json = Gson().toJson(this)
    return json.toRequestBody("application/json; charset=utf-8".toMediaType())
}
