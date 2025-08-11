package com.voyz.datas.repository

import android.util.Log
import com.voyz.datas.model.dto.MenuSalesDto
import com.voyz.datas.model.dto.NationalityAnalyticsDto
import com.voyz.datas.model.dto.NationalitySummaryDto
import com.voyz.datas.model.dto.ReviewResponseDto
import com.voyz.datas.model.dto.ReviewSummaryDto
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

    suspend fun getReviewSummary(userId: String, start: String, end: String, positive: Int = 4, negative: Int = 2): ReviewSummaryDto {
        return api.getReviewSummary(userId, start, end, positive, negative)
    }

    suspend fun getNationalityByYear(userId: String, year: Int): List<NationalityAnalyticsDto> {
        return api.getNationalityStatsByYear(userId, year)
    }

    suspend fun getNationalityByMonth(userId: String, month: Int): List<NationalityAnalyticsDto> {
        return api.getNationalityStatsByMonth(userId, month)
    }

    suspend fun getNationalityByWeek(userId: String, week: Int): List<NationalityAnalyticsDto> {
        return api.getNationalityStatsByWeek(userId, week)
    }

    suspend fun getNationalitySummaryByMonth(userId: String, month: Int): NationalitySummaryDto {
        return api.getNationalitySummaryByMonth(userId, month)
    }

    suspend fun getReviews(
        userId: String,
        start: String,
        end: String,
        nationality: String? = null,
        minRating: Int? = null,
        maxRating: Int? = null,
        menuIds: List<Int>? = null,
    ): List<ReviewResponseDto> {
        return api.getReviews(userId, start, end, nationality, minRating, maxRating, menuIds)
    }

    suspend fun getReviewKeywords(
        userId: String,
        start: String,
        end: String,
        positive: Int = 4,
        negative: Int = 2,
        topK: Int = 5,
        mode: String = "openai",
    ): String {
        val resp = api.getReviewKeywords(userId, start, end, positive, negative, topK, mode)
        return if (resp.isSuccessful) resp.body()?.string().orEmpty() else "{}"
    }
}