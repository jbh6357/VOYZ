package com.voyz.datas.network

import com.voyz.datas.model.dto.FcmTokenDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FcmApiService {

    @POST("fcm/token")
    suspend fun registerToken(
        @Body tokenDto: FcmTokenDto
    ): Response<Void>

}