package com.voyz.datas.repository

import android.util.Log
import com.voyz.datas.model.dto.DaySuggestionDto
import com.voyz.datas.model.dto.MarketingDto
import com.voyz.datas.network.ApiClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import retrofit2.Response

class CalendarRepository {
    
    private val apiService = ApiClient.calendarApiService
    
    /**
     * 리마인더 일정 조회
     * @param userId 사용자 ID
     * @param year 년도
     * @param month 월
     * @return 리마인더 마케팅 일정 목록
     */
    suspend fun getReminders(
        userId: String,
        year: Int,
        month: Int
    ): Response<List<MarketingDto>> {
        return apiService.getReminders(userId, year, month)
    }
    
    /**
     * 특일 및 특일 제안 조회
     * @param userId 사용자 ID
     * @param year 년도
     * @param month 월
     * @return 특일 및 특일 제안 목록
     */
    suspend fun getDaySuggestions(
        userId: String,
        year: Int,
        month: Int
    ): Response<List<DaySuggestionDto>> {
        return apiService.getDaySuggestions(userId, year, month)
    }
    
    /**
     * 두 API를 동시에 호출하여 캘린더 데이터를 통합 조회
     * @param userId 사용자 ID
     * @param year 년도
     * @param month 월
     * @return Pair<리마인더목록, 특일제안목록>
     */
    suspend fun getCalendarData(
        userId: String,
        year: Int,
        month: Int
    ): Pair<List<MarketingDto>, List<DaySuggestionDto>> {
        Log.d("CalendarRepository", "getCalendarData called - userId: $userId, year: $year, month: $month")
        return try {
            coroutineScope {
                // 두 API를 병렬로 호출
                Log.d("CalendarRepository", "Starting parallel API calls")
                val remindersDeferred = async { getReminders(userId, year, month) }
                val daySuggestionsDeferred = async { getDaySuggestions(userId, year, month) }
                
                val remindersResponse = remindersDeferred.await()
                val daySuggestionsResponse = daySuggestionsDeferred.await()
                
                Log.d("CalendarRepository", "Reminders API - Success: ${remindersResponse.isSuccessful}, Code: ${remindersResponse.code()}")
                Log.d("CalendarRepository", "DaySuggestions API - Success: ${daySuggestionsResponse.isSuccessful}, Code: ${daySuggestionsResponse.code()}")
                
                val reminders = if (remindersResponse.isSuccessful) {
                    val data = remindersResponse.body() ?: emptyList()
                    Log.d("CalendarRepository", "Reminders data size: ${data.size}")
                    data
                } else {
                    Log.w("CalendarRepository", "Reminders API failed: ${remindersResponse.errorBody()?.string()}")
                    emptyList()
                }
                
                val daySuggestions = if (daySuggestionsResponse.isSuccessful) {
                    val data = daySuggestionsResponse.body() ?: emptyList()
                    Log.d("CalendarRepository", "DaySuggestions data size: ${data.size}")
                    data
                } else {
                    Log.w("CalendarRepository", "DaySuggestions API failed: ${daySuggestionsResponse.errorBody()?.string()}")
                    emptyList()
                }
                
                Pair(reminders, daySuggestions)
            }
        } catch (e: Exception) {
            // 에러 발생 시 빈 리스트 반환
            Log.e("CalendarRepository", "Error in getCalendarData", e)
            Pair(emptyList(), emptyList())
        }
    }
} 