package com.voyz.datas.repository

import com.voyz.datas.network.ApiClient
import okhttp3.ResponseBody
import retrofit2.Response

class QrRepository {
    
    private val apiService = ApiClient.qrApiService
    
    /**
     * QR 코드 생성
     */
    suspend fun generateQrCodes(
        userId: String,
        tableCount: Int
    ): Response<ResponseBody> {
        return apiService.generateQrCodes(userId, tableCount)
    }
}