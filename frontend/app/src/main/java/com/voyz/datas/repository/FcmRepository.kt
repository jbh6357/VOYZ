package com.voyz.datas.repository

import com.voyz.datas.model.dto.FcmTokenDto
import com.voyz.datas.network.ApiClient
import android.util.Base64
import org.json.JSONObject

class FcmRepository {
    private val apiService = ApiClient.fcmApiService

    suspend fun registerFcmToken(userId: String, token: String, uuid: String): Boolean {
        val request = FcmTokenDto(userId, token, uuid)
        val response = apiService.registerToken(request)
        return response.isSuccessful
    }

    suspend fun extractUuidFromJwt(jwt: String): String? {
        val parts = jwt.split(".")
        if (parts.size != 3) return null

        val payload = parts[1]
        val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
        val payloadJson = String(decodedBytes, Charsets.UTF_8)

        val jsonObject = JSONObject(payloadJson)
        return jsonObject.optString("uuid", null)
    }
}