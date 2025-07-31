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
        
        Log.d("CalendarDataMapper", "=== mapSuggestionToOpportunity ===")
        Log.d("CalendarDataMapper", "SpecialDay: ${specialDay.name} (ID: ${specialDay.sdIdx})")
        Log.d("CalendarDataMapper", "HasSuggest: ${suggestion.hasSuggest}")
        Log.d("CalendarDataMapper", "SpecialSuggest: ${if (specialSuggest != null) "EXISTS (ID: ${specialSuggest.ssuIdx})" else "NULL"}")
        
        return if (suggestion.hasSuggest && specialSuggest != null) {
            // AI 제안
            Log.d("CalendarDataMapper", "→ Creating SUGGESTION with ID: suggestion_${specialSuggest.ssuIdx}")
            MarketingOpportunity(
                id = "suggestion_${specialSuggest.ssuIdx}",
                date = specialSuggest.startDate,
                title = specialSuggest.title,
                category = mapSpecialDayToCategory(specialDay.type, specialDay.category),
                description = specialSuggest.content,
                targetCustomer = specialSuggest.targetCustomer ?: mapSpecialDayToTargetCustomer(specialDay.type),
                suggestedAction = specialSuggest.suggestedAction ?: "• ${specialDay.name} 활용한 마케팅\n• 특별 메뉴 출시\n• 테마 이벤트 진행",
                expectedEffect = specialSuggest.expectedEffect ?: "${specialDay.name} 관련 매출 증대",
                confidence = calculateDynamicConfidence(specialDay, "일반", specialSuggest.confidence), // TODO: storeCategory 전달 필요
                priority = Priority.MEDIUM, // 제안이 있으면 보통 우선순위
                dataSource = DataSource.SPECIAL_CALENDAR
            )
        } else {
            // 순수 기회 (특일만)
            Log.d("CalendarDataMapper", "→ Creating OPPORTUNITY with ID: special_day_${specialDay.sdIdx}")
            MarketingOpportunity(
                id = "special_day_${specialDay.sdIdx}",
                date = specialDay.startDate,
                title = specialDay.name,
                category = mapSpecialDayToCategory(specialDay.type, specialDay.category),
                description = specialDay.content ?: "${specialDay.name}입니다. 이 날을 활용한 마케팅을 고려해보세요.",
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
            // 시작일부터 종료일까지 각 날짜에 추가 (동일한 ID로 모든 날짜에 표시)
            var currentDate = reminder.startDate
            while (!currentDate.isAfter(reminder.endDate)) {
                allOpportunities.add(opportunity.copy(
                    id = opportunity.id, // 날짜별로 다른 ID가 아닌 동일한 ID 사용
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
        allOpportunities.forEachIndexed { index, opp ->
            Log.d("CalendarDataMapper", "[$index] ID: ${opp.id}, Title: ${opp.title}, Date: ${opp.date}")
        }
        
        // 날짜별로 그룹화
        val dailyOpportunities = allOpportunities
            .groupBy { it.date }
            .map { (date, opportunities) ->
                Log.d("CalendarDataMapper", "Grouping for date $date: ${opportunities.size} opportunities")
                opportunities.forEachIndexed { idx, opp ->
                    Log.d("CalendarDataMapper", "  [$idx] ID: ${opp.id}, Title: ${opp.title}")
                }
                DailyMarketingOpportunities(date, opportunities)
            }
            .sortedBy { it.date }
            
        Log.d("CalendarDataMapper", "Daily opportunities groups: ${dailyOpportunities.size}")
        dailyOpportunities.forEach { daily ->
            Log.d("CalendarDataMapper", "Final - Date: ${daily.date}, Opportunities: ${daily.opportunities.size}")
        }
        
        return dailyOpportunities
    }
    
    /**
     * 특일과 업종의 연관성에 따른 동적 신뢰도 계산
     */
    private fun calculateDynamicConfidence(
        specialDay: com.voyz.datas.model.dto.SpecialDayDto, 
        storeCategory: String, 
        mlConfidence: Float?
    ): Float {
        val baseConfidence = (mlConfidence ?: 85f) / 100f
        
        // 1. 특일 유형별 기본 가중치
        val typeWeight = when {
            specialDay.type.contains("절기") || specialDay.type.contains("계절") -> 0.9f // 계절 음식과 연관성 높음
            specialDay.type.contains("명절") || specialDay.type.contains("공휴일") -> 0.95f // 명절 음식 소비 증가
            specialDay.type.contains("기념일") -> 0.8f // 기념일은 상황에 따라 다름
            specialDay.name.contains("데이") && isDirectFoodDay(specialDay.name) -> 1.0f // 치킨데이, 피자데이 등
            else -> 0.7f
        }
        
        // 2. 특일명과 업종 매칭도
        val categoryWeight = when (storeCategory) {
            "한식" -> when {
                specialDay.name.contains("한식") || specialDay.name.contains("김치") || 
                specialDay.name.contains("설날") || specialDay.name.contains("추석") -> 1.0f
                specialDay.type.contains("절기") -> 0.9f // 절기는 한식과 연관성 높음
                else -> 0.8f
            }
            "치킨" -> when {
                specialDay.name.contains("치킨") -> 1.0f
                specialDay.name.contains("맥주") -> 0.95f // 치킨과 맥주
                else -> 0.75f
            }
            "피자" -> when {
                specialDay.name.contains("피자") -> 1.0f
                specialDay.name.contains("치즈") -> 0.9f
                else -> 0.75f
            }
            "카페" -> when {
                specialDay.name.contains("커피") || specialDay.name.contains("디저트") -> 1.0f
                specialDay.name.contains("밸런타인") || specialDay.name.contains("화이트데이") -> 0.95f
                else -> 0.8f
            }
            else -> 0.8f // 기본값
        }
        
        // 3. 계절성 보정
        val seasonalWeight = when {
            specialDay.name.contains("여름") && (storeCategory == "카페" || storeCategory == "치킨") -> 1.1f
            specialDay.name.contains("겨울") && storeCategory == "한식" -> 1.1f
            specialDay.name.contains("크리스마스") && storeCategory == "카페" -> 1.15f
            else -> 1.0f
        }
        
        // 최종 신뢰도 계산 (0.3~0.95 범위로 제한)
        val finalConfidence = (baseConfidence * typeWeight * categoryWeight * seasonalWeight)
            .coerceIn(0.3f, 0.95f)
        
        Log.d("CalendarDataMapper", 
            "Dynamic confidence for ${specialDay.name} + $storeCategory: " +
            "base=$baseConfidence, type=$typeWeight, category=$categoryWeight, " +
            "seasonal=$seasonalWeight → final=$finalConfidence"
        )
        
        return finalConfidence
    }
    
    /**
     * 직접적인 음식 관련 데이인지 확인
     */
    private fun isDirectFoodDay(name: String): Boolean {
        val foodDays = listOf(
            "치킨데이", "피자데이", "커피데이", "맥주데이", "와인데이", 
            "디저트데이", "라면데이", "삼겹살데이", "떡데이", "김치데이"
        )
        return foodDays.any { name.contains(it) }
    }
} 