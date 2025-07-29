package com.voyz.datas.network

import com.voyz.datas.model.dto.DaySuggestionDto
import com.voyz.datas.model.dto.MarketingDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CalendarApiService {
    
    /**
     * 리마인더 일정 조회
     * GET /api/calendars/reminder
     */
    @GET("calendars/reminder")
    suspend fun getReminders(
        @Query("user_id") userId: String,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<List<MarketingDto>>
    
    /**
     * 특일, 특일 제안 조회
     * GET /api/calendars/day-sug
     */
    @GET("calendars/day-sug")
    suspend fun getDaySuggestions(
        @Query("user_id") userId: String,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<List<DaySuggestionDto>>
} 