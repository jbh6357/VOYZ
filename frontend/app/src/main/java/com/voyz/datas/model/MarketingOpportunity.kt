package com.voyz.datas.model

import java.time.LocalDate

/**
 * 마케팅 기회 데이터 모델
 */
data class MarketingOpportunity(
    val id: String,
    val date: LocalDate,
    val title: String,
    val category: MarketingCategory,
    val description: String,
    val targetCustomer: String,
    val suggestedAction: String,
    val expectedEffect: String,
    val confidence: Float, // 0.0 ~ 1.0, 성공 가능성
    val priority: Priority,
    val dataSource: DataSource
)

/**
 * 마케팅 기회 카테고리
 */
enum class MarketingCategory(val displayName: String, val emoji: String) {
    WEATHER("날씨", "🌤️"),
    UNIVERSITY("대학교", "🎓"),
    SPECIAL_DAY("특별한 날", "📅"),
    SEASON("계절", "🍂"),
    EVENT("이벤트", "🎉"),
    HOLIDAY("공휴일", "🏖️")
}

/**
 * 우선순위
 */
enum class Priority(val displayName: String, val color: String) {
    HIGH("높음", "#FF4444"),
    MEDIUM("보통", "#FF8800"), 
    LOW("낮음", "#44AA44")
}

/**
 * 데이터 소스
 */
enum class DataSource(val displayName: String) {
    WEATHER_API("날씨 API"),
    UNIVERSITY_SCHEDULE("대학교 일정"),
    SPECIAL_CALENDAR("특별 달력"),
    GOVERNMENT_DATA("공공 데이터"),
    SOCIAL_TREND("소셜 트렌드")
}

/**
 * 날짜별 마케팅 기회 그룹
 */
data class DailyMarketingOpportunities(
    val date: LocalDate,
    val opportunities: List<MarketingOpportunity>
) {
    val hasHighPriority: Boolean
        get() = opportunities.any { it.priority == Priority.HIGH }
    
    val totalCount: Int
        get() = opportunities.size
}