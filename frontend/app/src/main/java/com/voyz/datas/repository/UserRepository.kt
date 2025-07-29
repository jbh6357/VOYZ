package com.voyz.datas.repository

import com.voyz.datas.model.dto.LoginRequest
import com.voyz.datas.model.dto.LoginResponse
import com.voyz.datas.model.dto.UserRegistrationRequest
import com.voyz.datas.network.ApiClient
import okhttp3.ResponseBody
import retrofit2.Response

class UserRepository {
    
    private val apiService = ApiClient.userApiService
    
    /**
     * 회원가입
     */
    suspend fun register(
        userId: String,
        password: String,
        userName: String,
        userPhone: String,
        storeName: String,
        storeCategory: String,
        storeAddress: String
    ): Response<ResponseBody> {
        val request = UserRegistrationRequest(
            userId = userId,
            userPw = password,
            userName = userName,
            userPhone = userPhone,
            storeName = storeName,
            storeCategory = storeCategory,
            storeAddress = storeAddress
        )
        
        return apiService.register(request)
    }
    
    /**
     * 로그인
     */
    suspend fun login(userId: String, password: String): Response<LoginResponse> {
        val request = LoginRequest(
            userId = userId,
            userPw = password
        )
        
        return apiService.login(request)
    }
}