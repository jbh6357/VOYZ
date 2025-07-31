package com.voyz.presentation.component.calendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.voyz.datas.model.dto.DaySuggestionDto
import com.voyz.datas.model.dto.MarketingDto
import com.voyz.datas.model.DailyMarketingOpportunities
import com.voyz.datas.repository.CalendarRepository
import com.voyz.datas.mapper.CalendarDataMapper
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(
    private val context: android.content.Context
) : ViewModel() {
    
    private val calendarRepository = CalendarRepository()
    private val calendarDataStore = com.voyz.datas.datastore.CalendarDataStore(context)
    
    var selectedDate by mutableStateOf<LocalDate?>(null)
        private set
    
    var currentMonth by mutableStateOf(YearMonth.now())
        private set
        
    init {
        android.util.Log.d("CalendarViewModel", "=== CalendarViewModel Init ===")
        android.util.Log.d("CalendarViewModel", "System time: ${java.time.LocalDateTime.now()}")
        android.util.Log.d("CalendarViewModel", "YearMonth.now(): ${YearMonth.now()}")
        android.util.Log.d("CalendarViewModel", "LocalDate.now(): ${java.time.LocalDate.now()}")
        android.util.Log.d("CalendarViewModel", "currentMonth: $currentMonth")
        android.util.Log.d("CalendarViewModel", "=== End CalendarViewModel Init ===")
    }
        
    var dailyOpportunities by mutableStateOf<Map<LocalDate, DailyMarketingOpportunities>>(emptyMap())
        private set
        
    var isLoading by mutableStateOf(false)
        private set
    
    var reminders by mutableStateOf<List<MarketingDto>>(emptyList())
        private set
        
    var daySuggestions by mutableStateOf<List<DaySuggestionDto>>(emptyList())
        private set
    
    // 캐싱을 위한 변수들
    private var cachedUserId: String? = null
    private var cachedMonth: YearMonth? = null
    private var lastApiCallTime: Long = 0L

    /**
     * 캘린더 데이터 로딩 (리마인더 + 특일제안)
     */
    fun loadCalendarData(userId: String) {
        Log.d("CalendarViewModel", "loadCalendarData called with userId: $userId, year: ${currentMonth.year}, month: ${currentMonth.monthValue}")
        Log.d("CalendarViewModel", "Current real date: ${LocalDate.now()}, currentMonth in ViewModel: $currentMonth")
        
        // 캐싱 체크: 동일한 사용자와 월, 그리고 5분 이내인 경우 API 호출 스킵
        val currentTime = System.currentTimeMillis()
        val cacheValidTime = 5 * 60 * 1000L // 5분
        
        if (cachedUserId == userId && 
            cachedMonth == currentMonth && 
            (currentTime - lastApiCallTime) < cacheValidTime &&
            dailyOpportunities.isNotEmpty()) {
            Log.d("CalendarViewModel", "Using cached data - skipping API call")
            return
        }
        
        viewModelScope.launch {
            isLoading = true
            try {
                val (remindersData, daySuggestionsData) = calendarRepository.getCalendarData(
                    userId = userId,
                    year = currentMonth.year,
                    month = currentMonth.monthValue
                )
                
                Log.d("CalendarViewModel", "API Response - Reminders: ${remindersData.size}, DaySuggestions: ${daySuggestionsData.size}")
                
                reminders = remindersData
                daySuggestions = daySuggestionsData
                
                // 데이터를 기존 MarketingOpportunity 형태로 변환
                updateOpportunitiesMap()
                
                // 캐시 정보 업데이트
                cachedUserId = userId
                cachedMonth = currentMonth
                lastApiCallTime = currentTime
                
                Log.d("CalendarViewModel", "Final dailyOpportunities size: ${dailyOpportunities.size}")
                
            } catch (e: Exception) {
                // 에러 처리 (로그 출력 등)
                Log.e("CalendarViewModel", "Error loading calendar data", e)
                reminders = emptyList()
                daySuggestions = emptyList()
                dailyOpportunities = emptyMap()
            } finally {
                isLoading = false
            }
        }
    }
    
    /**
     * 리마인더와 특일제안 데이터를 MarketingOpportunity로 변환하여 맵 업데이트
     */
    private fun updateOpportunitiesMap() {
        val opportunitiesList = CalendarDataMapper.mapToDailyOpportunities(reminders, daySuggestions)
        dailyOpportunities = opportunitiesList.associateBy { it.date }
        
        // DataStore에 캐시 저장
        viewModelScope.launch {
            try {
                val allOpportunities = opportunitiesList.flatMap { it.opportunities }
                val monthKey = "${currentMonth.year}-${String.format("%02d", currentMonth.monthValue)}"
                
                cachedUserId?.let { userId ->
                    calendarDataStore.cacheOpportunities(userId, monthKey, allOpportunities)
                    Log.d("CalendarViewModel", "Cached ${allOpportunities.size} opportunities for $monthKey")
                }
            } catch (e: Exception) {
                Log.e("CalendarViewModel", "Failed to cache opportunities", e)
            }
        }
    }

    fun selectDate(date: LocalDate) {
        selectedDate = date
    }
    
    fun clearSelection() {
        selectedDate = null
    }
    
    /**
     * 캐시 무효화 (외부에서 호출 가능)
     */
    fun invalidateCache() {
        Log.d("CalendarViewModel", "Cache invalidated")
        cachedUserId = null
        cachedMonth = null
        lastApiCallTime = 0L
    }

    fun goToNextMonth(userId: String) {
        currentMonth = currentMonth.plusMonths(1)
        invalidateCache() // 월 변경 시 캐시 무효화
        loadCalendarData(userId)
    }
    
    fun goToPreviousMonth(userId: String) {
        currentMonth = currentMonth.minusMonths(1)
        invalidateCache() // 월 변경 시 캐시 무효화
        loadCalendarData(userId)
    }
    
    fun goToNextMonthWithDirection(userId: String): Pair<YearMonth, Boolean> {
        val newMonth = currentMonth.plusMonths(1)
        currentMonth = newMonth
        invalidateCache() // 월 변경 시 캐시 무효화
        loadCalendarData(userId)
        return newMonth to true // true = 다음 달로 이동
    }
    
    fun goToPreviousMonthWithDirection(userId: String): Pair<YearMonth, Boolean> {
        val newMonth = currentMonth.minusMonths(1)
        currentMonth = newMonth
        invalidateCache() // 월 변경 시 캐시 무효화
        loadCalendarData(userId)
        return newMonth to false // false = 이전 달로 이동
    }
    
    fun goToMonth(yearMonth: YearMonth, userId: String) {
        currentMonth = yearMonth
        invalidateCache() // 월 변경 시 캐시 무효화
        loadCalendarData(userId)
    }
    
    fun getOpportunitiesForDate(date: LocalDate): DailyMarketingOpportunities? {
        return dailyOpportunities[date]
    }
    fun goToToday() {
        val today = LocalDate.now()
        selectedDate = today
        currentMonth = YearMonth.from(today) // ✅ 현재 월도 변경해줘야 애니메이션/캘린더 이동
    }
}

