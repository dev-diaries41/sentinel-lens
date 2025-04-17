package com.fpf.sentinellens.api

import android.graphics.Bitmap
import com.fpf.sentinellens.lib.HttpMethod
import com.fpf.sentinellens.lib.RequestHandler
import com.fpf.sentinellens.lib.ResponseData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

suspend fun sendTelegramMessage(
    token: String,
    message: String,
    chatId: String,
    image: Bitmap
): Result<ResponseData> {
    val byteArray = withContext(Dispatchers.IO) {
        ByteArrayOutputStream().use { stream ->
            image.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            stream.toByteArray()
        }
    }

    val imageRequestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
    val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        .addFormDataPart("chat_id", chatId)
        .addFormDataPart("caption", message)
        .addFormDataPart("photo", "image.jpg", imageRequestBody)
        .build()

    val url = "https://api.telegram.org/bot$token/sendPhoto"
    val requestHandler = RequestHandler() // baseUrl not needed when passing the full url

    return requestHandler.makeRequest(
        url = url,
        method = HttpMethod.POST,
        headers = mapOf("Content-Type" to multipartBody.contentType().toString()),
        body = multipartBody
    )
}
