package com.voyz.datas.network

import com.voyz.datas.model.dto.MenuSalesDto
import com.voyz.datas.model.dto.SalesAnalyticsDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AnalyticsApiService {

    @GET("analytics/sales/{userId}")
    suspend fun getSalesAnalytics(
        @Path("userId") userId: String,
        @Query("startDate") startDate: String,  // "yyyy-MM-dd"
        @Query("endDate") endDate: String
    ): List<SalesAnalyticsDto>

    @GET("analytics/menus/{userId}/popular")
    suspend fun getTopMenuSales(
        @Path("userId") userId: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("category") category: String? = null,
        @Query("topCount") topCount: Int = 5
    ): List<MenuSalesDto>
}