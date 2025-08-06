package com.voyz.datas.model.dto

import com.google.gson.annotations.SerializedName

/**
 * 회원가입 요청 DTO
 */
data class UserRegistrationRequest(
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("userPw")
    val userPw: String,
    
    @SerializedName("userName")
    val userName: String,
    
    @SerializedName("userPhone")
    val userPhone: String,
    
    @SerializedName("storeName")
    val storeName: String,
    
    @SerializedName("storeCategory")
    val storeCategory: String,
    
    @SerializedName("storeAddress")
    val storeAddress: String
)

/**
 * 로그인 요청 DTO
 */
data class LoginRequest(
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("userPw")
    val userPw: String
)

/**
 * 로그인 응답 DTO
 */
data class LoginResponse(
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("userName")
    val userName: String,
    
    @SerializedName("storeName")
    val storeName: String,
    
    @SerializedName("storeCategory")
    val storeCategory: String,
    
    @SerializedName("accessToken")
    val accessToken: String,
    
    @SerializedName("refreshToken")
    val refreshToken: String,
    
    @SerializedName("tokenType")
    val tokenType: String
)