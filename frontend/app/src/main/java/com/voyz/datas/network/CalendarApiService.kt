package com.voyz.datas.network

import com.voyz.datas.model.dto.DaySuggestionDto
import com.voyz.datas.model.dto.MarketingDto
import com.voyz.datas.model.dto.ReminderDto
import com.voyz.datas.model.dto.SpecialDaySuggestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
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
    
    /**
     * 리마인더 생성
     */
    @POST("calendars/reminder")
    suspend fun createReminder(
        @Body reminderDto: ReminderDto,
        @Query("userId") userId: String
    ): Response<Void>
    
    /**
     * 리마인더 상세 조회
     */
    @GET("calendars/reminder/{marketing_idx}")
    suspend fun getReminderDetail(
        @Path("marketing_idx") marketingIdx: Int
    ): Response<MarketingDto>
    
    /**
     * 제안 상세 조회
     */
    @GET("calendars/day-sug/{ssu_idx}")
    suspend fun getSuggestionDetail(
        @Path("ssu_idx") ssuIdx: Int
    ): Response<SpecialDaySuggestDto>
} 