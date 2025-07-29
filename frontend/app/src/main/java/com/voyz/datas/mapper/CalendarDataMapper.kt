package com.voyz.datas.mapper

import android.util.Log
import com.voyz.datas.model.*
import com.voyz.datas.model.dto.MarketingDto
import com.voyz.datas.model.dto.DaySuggestionDto
import java.time.LocalDate

/**
 * API 데이터를 기존 MarketingOpportunity 형태로 변환하는 매퍼
 */
object CalendarDataMapper {
    
    /**
     * MarketingDto(리마인더)를 MarketingOpportunity로 변환
     */
    fun mapReminderToOpportunity(reminder: MarketingDto): MarketingOpportunity {
        return MarketingOpportunity(
            id = "reminder_${reminder.marketingIdx}",
            date = reminder.startDate,
            title = "[리마인더] ${reminder.title}",
            category = mapReminderTypeToCategory(reminder.type),
            description = reminder.content,
            targetCustomer = "나의 일정",
            suggestedAction = "• 일정 확인\n• 필요한 준비 사항 체크\n• 관련 자료 준비",
            expectedEffect = "개인 일정 관리 향상",
            confidence = 1.0f, // 사용자가 직접 등록한 일정이므로 100%
            priority = mapReminderTypeToPriority(reminder.type), // 타입별 우선순위
            dataSource = DataSource.GOVERNMENT_DATA // 임시로 사용
        )
    }
    
    /**
     * DaySuggestionDto(제안)를 MarketingOpportunity로 변환
     */
    fun mapSuggestionToOpportunity(suggestion: DaySuggestionDto): MarketingOpportunity {
        val specialDay = suggestion.specialDay
        val specialSuggest = suggestion.specialDaySuggest
        
        return if (suggestion.hasSuggest && specialSuggest != null) {
            // 제안이 있는 특일
            MarketingOpportunity(
                id = "suggestion_${specialSuggest.ssuIdx}",
                date = specialSuggest.startDate,
                title = specialSuggest.title,
                category = mapSpecialDayToCategory(specialDay.type, specialDay.category),
                description = specialSuggest.content,
                targetCustomer = mapSpecialDayToTargetCustomer(specialDay.type),
                suggestedAction = "• ${specialDay.name} 활용한 마케팅\n• 특별 메뉴 출시\n• 테마 이벤트 진행",
                expectedEffect = "${specialDay.name} 관련 매출 증대",
                confidence = 0.85f,
                priority = Priority.MEDIUM, // 제안이 있으면 보통 우선순위
                dataSource = DataSource.SPECIAL_CALENDAR
            )
        } else {
            // 제안이 없는 단순 특일 정보
            MarketingOpportunity(
                id = "special_day_${specialDay.sdIdx}",
                date = specialDay.startDate,
                title = specialDay.name,
                category = mapSpecialDayToCategory(specialDay.type, specialDay.category),
                description = "${specialDay.name}입니다. 이 날을 활용한 마케팅을 고려해보세요.",
                targetCustomer = "일반 고객",
                suggestedAction = "• 특별한 날 홍보\n• 관련 테마 활용\n• 고객 관심 유도",
                expectedEffect = "브랜드 인지도 향상",
                confidence = 0.60f,
                priority = Priority.LOW, // 제안이 없으면 낮은 우선순위
                dataSource = DataSource.SPECIAL_CALENDAR
            )
        }
    }
    
    /**
     * 리마인더 타입을 우선순위로 매칭
     */
    private fun mapReminderTypeToPriority(type: String): Priority {
        return when (type.lowercase()) {
            "1", "marketing", "마케팅" -> Priority.HIGH // 마케팅 타입
            "2", "schedule", "일정" -> Priority.MEDIUM // 일정 타입  
            else -> Priority.MEDIUM // 기본값
        }
    }

    /**
     * 리마인더 타입을 MarketingCategory로 매칭
     */
    private fun mapReminderTypeToCategory(type: String): MarketingCategory {
        return when (type.lowercase()) {
            "1", "marketing", "마케팅" -> MarketingCategory.SPECIAL_DAY // 리마인더는 특별한 날로 분류
            else -> MarketingCategory.SPECIAL_DAY
        }
    }
    
    /**
     * 특일 타입을 MarketingCategory로 매칭
     */
    private fun mapSpecialDayToCategory(type: String, category: String?): MarketingCategory {
        return when {
            type.contains("공휴일") || type.contains("holiday") -> MarketingCategory.HOLIDAY
            type.contains("절기") || type.contains("season") || type.contains("계절") -> MarketingCategory.SEASON
            type.contains("기념일") || type.contains("특별") -> MarketingCategory.SPECIAL_DAY
            type.contains("이벤트") || type.contains("event") -> MarketingCategory.EVENT
            category?.contains("날씨") == true -> MarketingCategory.WEATHER
            category?.contains("대학") == true -> MarketingCategory.UNIVERSITY
            else -> MarketingCategory.SPECIAL_DAY
        }
    }
    
    /**
     * 특일 타입에 따른 타겟 고객 설정
     */
    private fun mapSpecialDayToTargetCustomer(type: String): String {
        return when {
            type.contains("공휴일") -> "가족 단위 고객"
            type.contains("절기") || type.contains("계절") -> "건강 관심층"
            type.contains("기념일") -> "커플, 가족"
            type.contains("이벤트") -> "젊은층, 이벤트 참여층"
            else -> "일반 고객"
        }
    }
    
    /**
     * 날짜별 모든 기회들을 DailyMarketingOpportunities로 변환
     */
    fun mapToDailyOpportunities(
        reminders: List<MarketingDto>,
        suggestions: List<DaySuggestionDto>
    ): List<DailyMarketingOpportunities> {
        Log.d("CalendarDataMapper", "mapToDailyOpportunities - Reminders: ${reminders.size}, Suggestions: ${suggestions.size}")
        
        val allOpportunities = mutableListOf<MarketingOpportunity>()
        
        // 리마인더 변환
        reminders.forEach { reminder ->
            Log.d("CalendarDataMapper", "Processing reminder: ${reminder.title} (${reminder.startDate} ~ ${reminder.endDate})")
            val opportunity = mapReminderToOpportunity(reminder)
            // 시작일부터 종료일까지 각 날짜에 추가
            var currentDate = reminder.startDate
            while (!currentDate.isAfter(reminder.endDate)) {
                allOpportunities.add(opportunity.copy(
                    id = "${opportunity.id}_${currentDate}",
                    date = currentDate
                ))
                currentDate = currentDate.plusDays(1)
            }
        }
        
        // 특일 제안 변환
        suggestions.forEach { suggestion ->
            Log.d("CalendarDataMapper", "Processing suggestion: ${suggestion.specialDay.name} (hasSuggest: ${suggestion.hasSuggest})")
            val opportunity = mapSuggestionToOpportunity(suggestion)
            // 시작일부터 종료일까지 각 날짜에 추가
            var currentDate = suggestion.specialDay.startDate
            while (!currentDate.isAfter(suggestion.specialDay.endDate)) {
                allOpportunities.add(opportunity.copy(
                    id = "${opportunity.id}_${currentDate}",
                    date = currentDate
                ))
                currentDate = currentDate.plusDays(1)
            }
        }
        
        Log.d("CalendarDataMapper", "Total opportunities created: ${allOpportunities.size}")
        
        // 날짜별로 그룹화
        val dailyOpportunities = allOpportunities
            .groupBy { it.date }
            .map { (date, opportunities) ->
                DailyMarketingOpportunities(date, opportunities)
            }
            .sortedBy { it.date }
            
        Log.d("CalendarDataMapper", "Daily opportunities groups: ${dailyOpportunities.size}")
        dailyOpportunities.forEach { daily ->
            Log.d("CalendarDataMapper", "Date: ${daily.date}, Opportunities: ${daily.opportunities.size}")
        }
        
        return dailyOpportunities
    }
} 