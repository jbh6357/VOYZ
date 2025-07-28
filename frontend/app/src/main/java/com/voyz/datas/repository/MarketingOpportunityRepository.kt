package com.voyz.datas.repository

import com.voyz.datas.model.*
import java.time.LocalDate

/**
 * 마케팅 기회 데이터 저장소
 */
object MarketingOpportunityRepository {
    
    private val sampleOpportunities = listOf(
        // === 6월 데이터 ===
        // 6월 1일 - 어린이날 특수
        MarketingOpportunity(
            id = "june_01",
            date = LocalDate.of(2025, 6, 1),
            title = "어린이날 연휴 마지막날",
            category = MarketingCategory.HOLIDAY,
            description = "어린이날 연휴 마지막날로 가족 단위 외식 수요가 높습니다.",
            targetCustomer = "가족 단위 고객",
            suggestedAction = "• 키즈 메뉴 무료 제공\n• 가족 세트 할인\n• 어린이 선물 증정 이벤트",
            expectedEffect = "가족 고객 유입 50% 증가",
            confidence = 0.88f,
            priority = Priority.HIGH,
            dataSource = DataSource.SPECIAL_CALENDAR
        ),
        
        // 6월 5일 - 환경의 날
        MarketingOpportunity(
            id = "june_05",
            date = LocalDate.of(2025, 6, 5),
            title = "세계 환경의 날",
            category = MarketingCategory.SPECIAL_DAY,
            description = "환경보호 관심 증대로 친환경 메뉴에 대한 관심이 높아집니다.",
            targetCustomer = "환경 의식 고객",
            suggestedAction = "• 비건 메뉴 특가\n• 일회용품 없는 포장\n• 친환경 캠페인 진행",
            expectedEffect = "브랜드 이미지 향상, 신규 고객 유입",
            confidence = 0.72f,
            priority = Priority.MEDIUM,
            dataSource = DataSource.SPECIAL_CALENDAR
        ),
        
        // 6월 10일 - 장마 시작
        MarketingOpportunity(
            id = "june_10",
            date = LocalDate.of(2025, 6, 10),
            title = "장마 시작 (비 예보)",
            category = MarketingCategory.WEATHER,
            description = "장마철 시작으로 배달 주문과 실내 음식 선호도가 증가합니다.",
            targetCustomer = "직장인, 학생",
            suggestedAction = "• 따뜻한 국물 요리 프로모션\n• 배달 무료 이벤트\n• 우천 시 할인 쿠폰",
            expectedEffect = "배달 주문 40% 증가",
            confidence = 0.85f,
            priority = Priority.HIGH,
            dataSource = DataSource.WEATHER_API
        ),
        
        // 6월 15일 - 대학교 기말고사
        MarketingOpportunity(
            id = "june_15",
            date = LocalDate.of(2025, 6, 15),
            title = "대학교 기말고사 시즌",
            category = MarketingCategory.UNIVERSITY,
            description = "전국 대학교 기말고사 시즌으로 학생들의 야식 및 간식 수요 급증",
            targetCustomer = "대학생",
            suggestedAction = "• 야식 메뉴 20% 할인\n• 24시간 배달 서비스\n• '시험 화이팅' 응원 쿠폰",
            expectedEffect = "야식 주문 60% 증가",
            confidence = 0.92f,
            priority = Priority.HIGH,
            dataSource = DataSource.UNIVERSITY_SCHEDULE
        ),
        
        // 6월 25일 - 월드컵 예선
        MarketingOpportunity(
            id = "june_25",
            date = LocalDate.of(2025, 6, 25),
            title = "한국 축구 경기일",
            category = MarketingCategory.EVENT,
            description = "한국 국가대표팀 경기로 치킨, 맥주 등 응원 음식 수요 폭증",
            targetCustomer = "축구팬, 젊은층",
            suggestedAction = "• 치킨+맥주 세트 할인\n• 경기 시청 공간 제공\n• 승리 시 추가 할인 이벤트",
            expectedEffect = "치킨 주문량 80% 증가",
            confidence = 0.95f,
            priority = Priority.HIGH,
            dataSource = DataSource.SOCIAL_TREND
        ),
        
        // === 7월 데이터 ===
        // 7월 7일 - 칠석
        MarketingOpportunity(
            id = "july_07",
            date = LocalDate.of(2025, 7, 7),
            title = "칠석 (견우직녀의 날)",
            category = MarketingCategory.SPECIAL_DAY,
            description = "칠석을 맞아 커플 고객들의 로맨틱한 식사 수요가 증가합니다.",
            targetCustomer = "커플, 연인",
            suggestedAction = "• 커플 메뉴 세트 출시\n• 로맨틱 데코레이션\n• 별자리 테마 디저트 증정",
            expectedEffect = "커플 고객 30% 증가",
            confidence = 0.68f,
            priority = Priority.MEDIUM,
            dataSource = DataSource.SPECIAL_CALENDAR
        ),
        
        // 7월 15일 - 초복
        MarketingOpportunity(
            id = "july_15",
            date = LocalDate.of(2025, 7, 15),
            title = "초복 (삼계탕의 날)",
            category = MarketingCategory.SPECIAL_DAY,
            description = "삼복 중 첫째 날로 보양식 수요가 폭증하는 날입니다.",
            targetCustomer = "전 연령층",
            suggestedAction = "• 삼계탕 특가 메뉴\n• 전통 보양식 코스\n• 건강 음식 프로모션",
            expectedEffect = "삼계탕 주문량 150% 증가",
            confidence = 0.98f,
            priority = Priority.HIGH,
            dataSource = DataSource.SPECIAL_CALENDAR
        ),
        
        // 7월 20일 - 여름휴가 시즌
        MarketingOpportunity(
            id = "july_20",
            date = LocalDate.of(2025, 7, 20),
            title = "여름휴가 성수기 시작",
            category = MarketingCategory.SEASON,
            description = "여름휴가철로 관광지 및 휴양지 음식점 매출 증가 예상",
            targetCustomer = "관광객, 휴가객",
            suggestedAction = "• 시원한 음료 무제한\n• 휴가객 대상 할인\n• 포장 메뉴 강화",
            expectedEffect = "관광지 매출 70% 증가",
            confidence = 0.87f,
            priority = Priority.HIGH,
            dataSource = DataSource.SOCIAL_TREND
        ),
        
        // 7월 25일 - 중복
        MarketingOpportunity(
            id = "july_25",
            date = LocalDate.of(2025, 7, 25),
            title = "중복 (두 번째 복날)",
            category = MarketingCategory.SPECIAL_DAY,
            description = "삼복 중 둘째 날로 여전히 높은 보양식 수요가 지속됩니다.",
            targetCustomer = "중장년층, 건강 관심층",
            suggestedAction = "• 보양식 메뉴 다양화\n• 건강 음료 세트\n• 원기회복 특별 코스",
            expectedEffect = "보양식 주문 120% 증가",
            confidence = 0.94f,
            priority = Priority.HIGH,
            dataSource = DataSource.SPECIAL_CALENDAR
        ),
        
        // 7월 30일 - 폭염경보
        MarketingOpportunity(
            id = "july_30",
            date = LocalDate.of(2025, 7, 30),
            title = "폭염경보 발령 (체감온도 38도)",
            category = MarketingCategory.WEATHER,
            description = "극심한 더위로 시원한 음식과 음료 수요 급증",
            targetCustomer = "전 연령층",
            suggestedAction = "• 아이스 메뉴 특가\n• 냉면, 빙수 프로모션\n• 시원한 실내 환경 어필",
            expectedEffect = "냉면류 주문 100% 증가",
            confidence = 0.91f,
            priority = Priority.HIGH,
            dataSource = DataSource.WEATHER_API
        ),
        
        // === 8월 데이터 ===
        // 8월 1일 - 물의 날
        MarketingOpportunity(
            id = "aug_01",
            date = LocalDate.of(2025, 8, 1),
            title = "물의 날 기념",
            category = MarketingCategory.SPECIAL_DAY,
            description = "물의 소중함을 알리는 날로 건강한 음료에 대한 관심 증가",
            targetCustomer = "건강 관심층",
            suggestedAction = "• 천연 음료 할인\n• 물 무료 제공 이벤트\n• 건강 음료 신메뉴 출시",
            expectedEffect = "음료 매출 25% 증가",
            confidence = 0.65f,
            priority = Priority.LOW,
            dataSource = DataSource.SPECIAL_CALENDAR
        ),
        
        // 8월 8일 - 입추
        MarketingOpportunity(
            id = "aug_08",
            date = LocalDate.of(2025, 8, 8),
            title = "입추 (가을의 시작)",
            category = MarketingCategory.SEASON,
            description = "절기상 가을의 시작으로 환절기 보양 음식 관심 증가",
            targetCustomer = "건강 의식 고객",
            suggestedAction = "• 환절기 보양 메뉴\n• 따뜻한 음식 프로모션\n• 계절 변화 테마 마케팅",
            expectedEffect = "보양식 주문 40% 증가",
            confidence = 0.73f,
            priority = Priority.MEDIUM,
            dataSource = DataSource.SPECIAL_CALENDAR
        ),
        
        // 8월 14일 - 말복
        MarketingOpportunity(
            id = "aug_14",
            date = LocalDate.of(2025, 8, 14),
            title = "말복 (마지막 복날)",
            category = MarketingCategory.SPECIAL_DAY,
            description = "삼복 중 마지막 복날로 여름 보양의 마무리 수요",
            targetCustomer = "전 연령층",
            suggestedAction = "• 삼복 완주 이벤트\n• 여름 보양 마무리 세트\n• 가을 대비 건강 메뉴",
            expectedEffect = "보양식 주문 80% 증가",
            confidence = 0.89f,
            priority = Priority.HIGH,
            dataSource = DataSource.SPECIAL_CALENDAR
        ),
        
        // 8월 15일 - 광복절
        MarketingOpportunity(
            id = "aug_15",
            date = LocalDate.of(2025, 8, 15),
            title = "광복절 (국경일)",
            category = MarketingCategory.HOLIDAY,
            description = "국경일 공휴일로 가족 모임 및 외식 수요 증가",
            targetCustomer = "가족 단위 고객",
            suggestedAction = "• 가족 모임 메뉴 세트\n• 전통 음식 특가\n• 애국심 테마 이벤트",
            expectedEffect = "가족 고객 50% 증가",
            confidence = 0.86f,
            priority = Priority.HIGH,
            dataSource = DataSource.SPECIAL_CALENDAR
        ),
        
        // 8월 20일 - 대학교 개강 준비
        MarketingOpportunity(
            id = "aug_20",
            date = LocalDate.of(2025, 8, 20),
            title = "대학교 2학기 개강 준비",
            category = MarketingCategory.UNIVERSITY,
            description = "대학생들의 개강 준비로 대학가 상권 활성화 시작",
            targetCustomer = "대학생",
            suggestedAction = "• 개강 맞이 할인 이벤트\n• 스터디 카페 메뉴 출시\n• 신학기 응원 이벤트",
            expectedEffect = "대학가 매출 35% 증가",
            confidence = 0.81f,
            priority = Priority.MEDIUM,
            dataSource = DataSource.UNIVERSITY_SCHEDULE
        ),
        
        // 8월 28일 - 처서 (더위 끝)
        MarketingOpportunity(
            id = "aug_28",
            date = LocalDate.of(2025, 8, 28),
            title = "처서 (더위가 그치는 날)",
            category = MarketingCategory.SEASON,
            description = "절기상 더위가 끝나는 시점으로 가을 메뉴 전환 시기",
            targetCustomer = "계절 음식 선호 고객",
            suggestedAction = "• 가을 시즌 메뉴 출시\n• 따뜻한 음식 복귀\n• 계절 변화 마케팅",
            expectedEffect = "신메뉴 주문 30% 증가",
            confidence = 0.77f,
            priority = Priority.MEDIUM,
            dataSource = DataSource.SPECIAL_CALENDAR
        ),
        
        // === 현재 날짜 기준 데이터 (테스트용) ===
        MarketingOpportunity(
            id = "today_1",
            date = LocalDate.now(),
            title = "오늘의 특별 기회 - 폭염 주의보",
            category = MarketingCategory.WEATHER,
            description = "극심한 더위로 인해 시원한 음식과 음료에 대한 수요가 급증할 것으로 예상됩니다.",
            targetCustomer = "전 연령층",
            suggestedAction = "• 시원한 면류 메뉴 프로모션\n• 아이스 음료 할인\n• 에어컨 가동 적극 홍보",
            expectedEffect = "냉면, 아이스커피 주문 60% 증가",
            confidence = 0.88f,
            priority = Priority.HIGH,
            dataSource = DataSource.WEATHER_API
        ),
        
        MarketingOpportunity(
            id = "today_2",
            date = LocalDate.now(),
            title = "홍익대학교 여름계절학기",
            category = MarketingCategory.UNIVERSITY,
            description = "여름계절학기 진행으로 대학가 유동인구 증가",
            targetCustomer = "대학생",
            suggestedAction = "• 여름 특강생 할인\n• 시원한 음료 무료 제공\n• 스터디 그룹 할인",
            expectedEffect = "대학가 매출 25% 증가",
            confidence = 0.75f,
            priority = Priority.MEDIUM,
            dataSource = DataSource.UNIVERSITY_SCHEDULE
        )
    )
    
    /**
     * 모든 마케팅 기회 조회
     */
    fun getAllOpportunities(): List<MarketingOpportunity> {
        return sampleOpportunities
    }
    
    /**
     * 특정 날짜의 마케팅 기회 조회
     */
    fun getOpportunitiesByDate(date: LocalDate): List<MarketingOpportunity> {
        return sampleOpportunities.filter { it.date == date }
    }
    
    /**
     * 날짜별로 그룹화된 마케팅 기회 조회
     */
    fun getDailyOpportunities(): List<DailyMarketingOpportunities> {
        return sampleOpportunities
            .groupBy { it.date }
            .map { (date, opportunities) ->
                DailyMarketingOpportunities(date, opportunities)
            }
            .sortedBy { it.date }
    }
    
    /**
     * 특정 ID의 마케팅 기회 조회
     */
    fun getOpportunityById(id: String): MarketingOpportunity? {
        return sampleOpportunities.find { it.id == id }
    }
}