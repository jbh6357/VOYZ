package com.voyz.datas.network

import com.voyz.datas.model.dto.MenuItemDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface MenuApiService {
    
    @Multipart
    @POST("menus/ocr")
    suspend fun processOcrImage(
        @Part file: MultipartBody.Part
    ): Response<List<MenuItemDto>>
    
    @POST("menus/")
    @FormUrlEncoded
    suspend fun createMenu(
        @Field("userId") userId: String,
        @Field("menuName") menuName: String,
        @Field("menuPrice") menuPrice: Int,
        @Field("menuDescription") menuDescription: String,
        @Field("category") category: String
    ): Response<Void>
    
    @GET("menus/{userId}")
    suspend fun getMenusByUserId(
        @Path("userId") userId: String
    ): Response<List<MenuItemDto>>
    
    @DELETE("menus/{menuIdx}")
    suspend fun deleteMenu(
        @Path("menuIdx") menuIdx: Int
    ): Response<Void>
}