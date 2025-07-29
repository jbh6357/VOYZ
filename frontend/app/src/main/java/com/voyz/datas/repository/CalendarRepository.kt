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
    
    /**
     * 특정 기회 ID로 기회 정보 조회
     * @param opportunityId 기회 ID (형식: "reminder_123" 또는 "suggestion_456")
     * @param userId 사용자 ID (필수)
     * @return MarketingOpportunity 객체 또는 null
     */
    suspend fun getOpportunityById(opportunityId: String, userId: String): com.voyz.datas.model.MarketingOpportunity? {
        Log.d("CalendarRepository", "getOpportunityById called - opportunityId: $opportunityId, userId: $userId")
        return try {
            // ID 형식에 따라 데이터 소스 결정
            when {
                opportunityId.startsWith("reminder_") -> {
                    // 리마인더 데이터에서 찾기
                    val reminderPart = opportunityId.removePrefix("reminder_")
                    // "_" 이후의 날짜 부분을 제거하고 marketingIdx만 추출
                    val marketingIdx = reminderPart.split("_").firstOrNull()?.toIntOrNull()
                    Log.d("CalendarRepository", "Looking for reminder with marketingIdx: $marketingIdx from opportunityId: $opportunityId")
                    if (marketingIdx != null) {
                        // 현재 월의 데이터를 가져와서 찾기
                        val currentDate = java.time.LocalDate.now()
                        val (reminders, _) = getCalendarData(userId, currentDate.year, currentDate.monthValue)
                        Log.d("CalendarRepository", "Found ${reminders.size} reminders")
                        
                        reminders.find { it.marketingIdx == marketingIdx }?.let { reminder ->
                            Log.d("CalendarRepository", "Found matching reminder: ${reminder.title}")
                            com.voyz.datas.mapper.CalendarDataMapper.mapReminderToOpportunity(reminder)
                        } ?: run {
                            Log.w("CalendarRepository", "No reminder found with marketingIdx: $marketingIdx")
                            null
                        }
                    } else {
                        Log.w("CalendarRepository", "Invalid marketingIdx in opportunityId: $opportunityId")
                        null
                    }
                }
                opportunityId.startsWith("suggestion_") -> {
                    // 특일 제안 데이터에서 찾기
                    val suggestionPart = opportunityId.removePrefix("suggestion_")
                    val ssuIdx = suggestionPart.split("_").firstOrNull()?.toIntOrNull()
                    Log.d("CalendarRepository", "Looking for suggestion with ssuIdx: $ssuIdx from opportunityId: $opportunityId")
                    if (ssuIdx != null) {
                        val currentDate = java.time.LocalDate.now()
                        val (_, suggestions) = getCalendarData(userId, currentDate.year, currentDate.monthValue)
                        Log.d("CalendarRepository", "Found ${suggestions.size} suggestions")
                        
                        suggestions.find { it.specialDaySuggest?.ssuIdx == ssuIdx }?.let { suggestion ->
                            Log.d("CalendarRepository", "Found matching suggestion: ${suggestion.specialDay.name}")
                            com.voyz.datas.mapper.CalendarDataMapper.mapSuggestionToOpportunity(suggestion)
                        } ?: run {
                            Log.w("CalendarRepository", "No suggestion found with ssuIdx: $ssuIdx")
                            null
                        }
                    } else {
                        Log.w("CalendarRepository", "Invalid ssuIdx in opportunityId: $opportunityId")
                        null
                    }
                }
                opportunityId.startsWith("special_day_") -> {
                    // 특일 데이터에서 찾기
                    val specialDayPart = opportunityId.removePrefix("special_day_")
                    // "_" 이후의 날짜 부분을 제거하고 sdIdx만 추출
                    val sdIdx = specialDayPart.split("_").firstOrNull()?.toIntOrNull()
                    Log.d("CalendarRepository", "Looking for special day with sdIdx: $sdIdx from opportunityId: $opportunityId")
                    if (sdIdx != null) {
                        val currentDate = java.time.LocalDate.now()
                        val (_, suggestions) = getCalendarData(userId, currentDate.year, currentDate.monthValue)
                        Log.d("CalendarRepository", "Found ${suggestions.size} suggestions")
                        
                        suggestions.find { it.specialDay.sdIdx == sdIdx }?.let { suggestion ->
                            Log.d("CalendarRepository", "Found matching special day: ${suggestion.specialDay.name}")
                            com.voyz.datas.mapper.CalendarDataMapper.mapSuggestionToOpportunity(suggestion)
                        } ?: run {
                            Log.w("CalendarRepository", "No special day found with sdIdx: $sdIdx")
                            null
                        }
                    } else {
                        Log.w("CalendarRepository", "Invalid sdIdx in opportunityId: $opportunityId")
                        null
                    }
                }
                else -> {
                    Log.w("CalendarRepository", "Unknown opportunityId format: $opportunityId")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("CalendarRepository", "Error getting opportunity by ID: $opportunityId", e)
            null
        }
    }
} 