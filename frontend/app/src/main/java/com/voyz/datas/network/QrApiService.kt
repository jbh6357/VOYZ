package com.voyz.datas.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

interface QrApiService {
    
    @POST("/api/qr/generate")
    suspend fun generateQrCodes(
        @Query("userId") userId: String,
        @Query("number") number: Int
    ): Response<ResponseBody>
}