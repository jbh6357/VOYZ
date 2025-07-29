package com.voyz.datas.network

import com.voyz.datas.model.dto.LoginRequest
import com.voyz.datas.model.dto.LoginResponse
import com.voyz.datas.model.dto.UserRegistrationRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApiService {
    
    @POST("users/register")
    suspend fun register(@Body request: UserRegistrationRequest): Response<ResponseBody>
    
    @POST("users/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}