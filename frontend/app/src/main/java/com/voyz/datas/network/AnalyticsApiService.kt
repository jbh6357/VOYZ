package com.voyz.datas.network

import com.voyz.datas.model.dto.MenuSalesDto
import com.voyz.datas.model.dto.NationalityAnalyticsDto
import com.voyz.datas.model.dto.ReviewResponseDto
import com.voyz.datas.model.dto.ReviewSummaryDto
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

    // 리뷰 요약 통계
    @GET("analytics/reviews/{userId}/summary")
    suspend fun getReviewSummary(
        @Path("userId") userId: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("positiveThreshold") positiveThreshold: Int = 4,
        @Query("negativeThreshold") negativeThreshold: Int = 2,
    ): ReviewSummaryDto

    // 국적별 리뷰 통계 (연/월/주)
    @GET("analytics/customers/{userId}/nationality")
    suspend fun getNationalityStatsByYear(
        @Path("userId") userId: String,
        @Query("year") year: Int,
    ): List<NationalityAnalyticsDto>

    @GET("analytics/customers/{userId}/nationality")
    suspend fun getNationalityStatsByMonth(
        @Path("userId") userId: String,
        @Query("month") month: Int,
    ): List<NationalityAnalyticsDto>

    @GET("analytics/customers/{userId}/nationality")
    suspend fun getNationalityStatsByWeek(
        @Path("userId") userId: String,
        @Query("week") week: Int,
    ): List<NationalityAnalyticsDto>

    @GET("analytics/customers/{userId}/nationality/summary")
    suspend fun getNationalitySummaryByMonth(
        @Path("userId") userId: String,
        @Query("month") month: Int,
    ): com.voyz.datas.model.dto.NationalitySummaryDto

    // 리뷰 목록 (필터)
    @GET("analytics/reviews/{userId}")
    suspend fun getReviews(
        @Path("userId") userId: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("nationality") nationality: String? = null,
        @Query("minRating") minRating: Int? = null,
        @Query("maxRating") maxRating: Int? = null,
        @Query("menuIds") menuIds: List<Int>? = null,
    ): List<ReviewResponseDto>

    @GET("analytics/reviews/{userId}/keywords")
    suspend fun getReviewKeywords(
        @Path("userId") userId: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("positiveThreshold") positiveThreshold: Int = 4,
        @Query("negativeThreshold") negativeThreshold: Int = 2,
        @Query("topK") topK: Int = 5,
        @Query("mode") mode: String = "openai",
    ): retrofit2.Response<okhttp3.ResponseBody>
}