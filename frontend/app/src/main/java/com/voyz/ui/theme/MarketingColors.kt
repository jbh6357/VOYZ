package com.voyz.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 마케팅 기회 서비스 전용 색상 시스템
 */
object MarketingColors {
    
    // 기본 브랜드 색상
    val Primary = Color(0xFF2196F3)      // 메인 파란색
    val PrimaryLight = Color(0xFFE3F2FD) // 연한 파란색
    val Secondary = Color(0xFF90A4AE)    // 회색 파란색
    
    // 배경 색상
    val Surface = Color(0xFFFAFAFA)      // 거의 흰색
    val SurfaceVariant = Color(0xFFF5F5F5) // 연한 회색
    
    // 마케팅 카테고리별 색상 (전문적이고 차분한 회색-파랑 계열)
    val CategoryPrimary = Color(0xFF546E7A)      // 진한 청회색
    val CategorySecondary = Color(0xFFECEFF1)    // 연한 청회색
    
    val CategoryHighlight = Color(0xFF1976D2)    // 강조용 파란색
    val CategoryLight = Color(0xFFE3F2FD)        // 연한 파란색
    
    // 모든 카테고리를 같은 색상으로 통일 (차별화는 아이콘으로)
    val WeatherPrimary = CategoryPrimary
    val WeatherSecondary = CategorySecondary
    
    val UniversityPrimary = CategoryPrimary
    val UniversitySecondary = CategorySecondary
    
    val SpecialDayPrimary = CategoryHighlight    // 특별한 날만 강조
    val SpecialDaySecondary = CategoryLight
    
    val EventPrimary = CategoryPrimary
    val EventSecondary = CategorySecondary
    
    val SeasonPrimary = CategoryPrimary
    val SeasonSecondary = CategorySecondary
    
    val HolidayPrimary = CategoryHighlight       // 공휴일도 강조
    val HolidaySecondary = CategoryLight
    
    // 우선순위 색상 (더 차분하게)
    val HighPriority = Color(0xFFE57373)        // 차분한 빨간색
    val MediumPriority = Color(0xFFFFB74D)      // 차분한 주황색
    val LowPriority = Color(0xFF81C784)         // 차분한 초록색
    
    // 텍스트 색상
    val TextPrimary = Color(0xFF212121)         // 진한 회색
    val TextSecondary = Color(0xFF757575)       // 중간 회색
    val TextTertiary = Color(0xFFBDBDBD)        // 연한 회색
    
    // 선택/활성 상태
    val Selected = Color(0xFF2196F3)            // 파란색
    val SelectedBackground = Color(0xFFE3F2FD)  // 연한 파란색
    
    // 오버레이 배경
    val OverlayBackground = Color.Gray.copy(alpha = 0.7f)  // 사이드바/모달 배경용
}

/**
 * 마케팅 카테고리별 색상 페어 반환
 */
fun getMarketingCategoryColors(category: com.voyz.data.model.MarketingCategory): Pair<Color, Color> {
    return when (category) {
        com.voyz.data.model.MarketingCategory.WEATHER -> 
            MarketingColors.WeatherPrimary to MarketingColors.WeatherSecondary
        com.voyz.data.model.MarketingCategory.UNIVERSITY -> 
            MarketingColors.UniversityPrimary to MarketingColors.UniversitySecondary
        com.voyz.data.model.MarketingCategory.SPECIAL_DAY -> 
            MarketingColors.SpecialDayPrimary to MarketingColors.SpecialDaySecondary
        com.voyz.data.model.MarketingCategory.EVENT -> 
            MarketingColors.EventPrimary to MarketingColors.EventSecondary
        com.voyz.data.model.MarketingCategory.SEASON -> 
            MarketingColors.SeasonPrimary to MarketingColors.SeasonSecondary
        com.voyz.data.model.MarketingCategory.HOLIDAY -> 
            MarketingColors.HolidayPrimary to MarketingColors.HolidaySecondary
    }
}

/**
 * 우선순위별 색상 반환
 */
fun getPriorityColor(priority: com.voyz.data.model.Priority): Color {
    return when (priority) {
        com.voyz.data.model.Priority.HIGH -> MarketingColors.HighPriority
        com.voyz.data.model.Priority.MEDIUM -> MarketingColors.MediumPriority
        com.voyz.data.model.Priority.LOW -> MarketingColors.LowPriority
    }
}