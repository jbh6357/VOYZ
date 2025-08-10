package com.voyz.datas.repository

import android.util.Log
import com.voyz.datas.model.dto.MenuSalesDto
import com.voyz.datas.model.dto.SalesAnalyticsDto
import com.voyz.datas.network.ApiClient

class AnalyticsRepository {

    private val api = ApiClient.analyticsApiService

    suspend fun getSalesAnalytics(userId: String, start: String, end: String): List<SalesAnalyticsDto> {
        return api.getSalesAnalytics(userId, start, end)
    }

    suspend fun getTopMenus(userId: String, start: String, end: String, category: String? = null): List<MenuSalesDto> {
        Log.d("AnalyticsRepository", "userId ${userId}, start ${start}, end ${end}, category ${category}")
        return api.getTopMenuSales(userId, start, end, category)
    }
}