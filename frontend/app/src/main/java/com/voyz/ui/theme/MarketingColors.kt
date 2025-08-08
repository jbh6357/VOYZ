package com.voyz.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 마케팅 기회 서비스 전용 색상 시스템
 */
object MarketingColors {
    // 기본 브랜드 색상 (웹 팔레트와 정합)
    val Primary = Color(0xFFE53E3E)        // korean-red
    val PrimaryLight = Color(0xFFFDECEC)   // red light
    val Secondary = Color(0xFFD69E2E)      // korean-gold

    // 배경 색상
    val Surface = Color(0xFFFFFFFF)        // white
    val SurfaceVariant = Color(0xFFF7FAFC) // light gray

    // 마케팅 카테고리 색상 (웹 팔레트 정합)
    val CategoryPrimary = Color(0xFF1A202C)      // korean-black (텍스트/강조)
    val CategorySecondary = Color(0xFFF7FAFC)    // light gray

    val CategoryHighlight = Color(0xFFD69E2E)    // 강조용 gold
    val CategoryLight = Color(0xFFFFF7D6)        // 연한 gold 배경

    // 모든 카테고리를 같은 색상으로 통일 (차별화는 아이콘으로 유지)
    val WeatherPrimary = CategoryPrimary
    val WeatherSecondary = CategorySecondary
    
    val UniversityPrimary = CategoryPrimary
    val UniversitySecondary = CategorySecondary
    
    val SpecialDayPrimary = CategoryHighlight     // 특별한 날 강조
    val SpecialDaySecondary = CategoryLight
    
    val EventPrimary = CategoryPrimary
    val EventSecondary = CategorySecondary
    
    val SeasonPrimary = CategoryPrimary
    val SeasonSecondary = CategorySecondary
    
    val HolidayPrimary = CategoryHighlight        // 공휴일 강조
    val HolidaySecondary = CategoryLight
    
    // 우선순위 색상 (웹 팔레트 정합)
    val HighPriority = Color(0xFFE53E3E)        // red
    val MediumPriority = Color(0xFFD69E2E)      // gold
    val LowPriority = Color(0xFF38A169)         // green
    
    // 텍스트 색상
    val TextPrimary = Color(0xFF1A202C)         // korean-black
    val TextSecondary = Color(0xFF718096)       // korean-gray
    val TextTertiary = Color(0xFFBDBDBD)        // 연한 회색
    
    // 선택/활성 상태
    val Selected = Primary                      // red
    val SelectedBackground = PrimaryLight       // 연한 red 배경
    
    // 오버레이 배경
    val OverlayBackground = Color.Black.copy(alpha = 0.7f)  // 사이드바/모달 배경용
}

/**
 * 마케팅 카테고리별 색상 페어 반환
 */
fun getMarketingCategoryColors(category: com.voyz.datas.model.MarketingCategory): Pair<Color, Color> {
    return when (category) {
        com.voyz.datas.model.MarketingCategory.WEATHER ->
            MarketingColors.WeatherPrimary to MarketingColors.WeatherSecondary
        com.voyz.datas.model.MarketingCategory.UNIVERSITY ->
            MarketingColors.UniversityPrimary to MarketingColors.UniversitySecondary
        com.voyz.datas.model.MarketingCategory.SPECIAL_DAY ->
            MarketingColors.SpecialDayPrimary to MarketingColors.SpecialDaySecondary
        com.voyz.datas.model.MarketingCategory.EVENT ->
            MarketingColors.EventPrimary to MarketingColors.EventSecondary
        com.voyz.datas.model.MarketingCategory.SEASON ->
            MarketingColors.SeasonPrimary to MarketingColors.SeasonSecondary
        com.voyz.datas.model.MarketingCategory.HOLIDAY ->
            MarketingColors.HolidayPrimary to MarketingColors.HolidaySecondary
    }
}

/**
 * 우선순위별 색상 반환
 */
fun getPriorityColor(priority: com.voyz.datas.model.Priority): Color {
    return when (priority) {
        com.voyz.datas.model.Priority.HIGH -> MarketingColors.HighPriority
        com.voyz.datas.model.Priority.MEDIUM -> MarketingColors.MediumPriority
        com.voyz.datas.model.Priority.LOW -> MarketingColors.LowPriority
    }
}