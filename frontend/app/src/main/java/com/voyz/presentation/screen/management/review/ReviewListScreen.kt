package com.voyz.presentation.screen.management.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.voyz.presentation.screen.management.review.component.ReviewItemCard
import com.voyz.presentation.screen.management.review.component.InsightCard
import com.voyz.presentation.screen.management.review.component.InsightCardsSection
import com.voyz.presentation.screen.management.review.component.InsightItem
import com.voyz.presentation.screen.management.review.component.FilterChips
import com.voyz.presentation.screen.management.review.component.ExpandableFilterChips
import com.voyz.presentation.screen.management.review.component.SelectedFilter
import com.voyz.presentation.screen.management.review.model.Review
import com.voyz.datas.datastore.UserPreferencesManager
import com.voyz.datas.repository.AnalyticsRepository
import com.voyz.datas.repository.TranslateRepository
import com.voyz.datas.datastore.ReviewAnalysisCache
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ReviewListScreen() {
    // 기간 기본값: 최근 1개월
    val today = LocalDate.now()
    val defaultStart = today.minusMonths(1).withDayOfMonth(1)
    val defaultEnd = today

    // userId
    val context = androidx.compose.ui.platform.LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    var userId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        userPreferencesManager.userId.collect { fetched -> userId = fetched }
    }

    // 데이터 로드
    val repo = remember { AnalyticsRepository() }
    val translateRepo = remember { TranslateRepository() }
    val analysisCache = remember { ReviewAnalysisCache(context) }
    val scope = rememberCoroutineScope()
    var apiReviews by remember { mutableStateOf<List<com.voyz.datas.model.dto.ReviewResponseDto>>(emptyList()) }
    var insights by remember { mutableStateOf<List<InsightItem>>(emptyList()) }
    var isInsightsLoading by remember { mutableStateOf(false) }
    
    // 새로운 필터 상태
    var selectedFilters by remember { mutableStateOf<List<SelectedFilter>>(emptyList()) }
    var availableNationalities by remember { mutableStateOf<List<String>>(emptyList()) }
    var availableMenus by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // 리뷰 데이터 로드 
    LaunchedEffect(userId) {
        val id = userId ?: return@LaunchedEffect
        val start = defaultStart.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val end = defaultEnd.format(DateTimeFormatter.ISO_LOCAL_DATE)
        runCatching { apiReviews = repo.getReviews(id, start, end) }
    }
    
    // 인사이트 데이터 로드 (캐시 우선)
    LaunchedEffect(userId) {
        val id = userId ?: return@LaunchedEffect
        isInsightsLoading = true
        
        scope.launch {
            // 1. 캐시된 데이터 먼저 확인
            analysisCache.getCachedAnalysis(id).collect { cached ->
                if (cached != null && !cached.insights.isNullOrEmpty()) {
                    println("✅ 캐시된 인사이트 사용")
                    insights = cached.insights.map { insightMap ->
                        // suggestedFilters도 캐시에서 복원
                        val suggestedFiltersMap = try {
                            val filters = (insightMap["suggestedFilters"] as? Map<String, String>) ?: emptyMap()
                            println("🔍 캐시된 인사이트 필터: ${insightMap["title"]} -> $filters")
                            filters
                        } catch (e: Exception) {
                            println("❌ 캐시된 인사이트 필터 복원 실패: ${e.message}")
                            emptyMap<String, String>()
                        }
                        InsightItem(
                            type = insightMap["type"] as? String ?: "trend",
                            title = insightMap["title"] as? String ?: "",
                            description = insightMap["description"] as? String ?: "",
                            priority = insightMap["priority"] as? String ?: "medium",
                            suggestedFilters = suggestedFiltersMap
                        )
                    }
                    isInsightsLoading = false
                    return@collect
                }
                
                // 2. 캐시가 없으면 API 호출
                println("🔄 API에서 인사이트 로드")
                try {
                    val response = repo.getComprehensiveInsights(id)
                    val insightsList = response["insights"] as? List<Map<String, Any>> ?: emptyList()
                    insights = insightsList.map { insightMap ->
                        val suggestedFiltersMap = insightMap["suggestedFilters"] as? Map<String, String> ?: emptyMap()
                        println("🔍 API 인사이트 필터: ${insightMap["title"]} -> $suggestedFiltersMap")
                        InsightItem(
                            type = insightMap["type"] as? String ?: "trend",
                            title = insightMap["title"] as? String ?: "",
                            description = insightMap["description"] as? String ?: "",
                            priority = insightMap["priority"] as? String ?: "medium",
                            suggestedFilters = suggestedFiltersMap
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // 기본 인사이트 설정
                    insights = listOf(
                        InsightItem("trend", "분석 중", "리뷰를 분석하고 있어요", "medium", mapOf("sort" to "latest")),
                        InsightItem("improvement", "데이터 수집", "더 많은 리뷰가 필요해요", "low", mapOf("sentiment" to "negative")), 
                        InsightItem("strength", "서비스 운영", "꾸준히 좋은 서비스 해주세요", "high", mapOf("sentiment" to "positive"))
                    )
                } finally {
                    isInsightsLoading = false
                }
            }
        }
    }

    // 번역된 리뷰 저장
    var translatedReviews by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // 리뷰 로드 시 번역 (캐시 우선)
    LaunchedEffect(apiReviews, userId) {
        if (apiReviews.isNotEmpty() && userId != null) {
            scope.launch {
                val reviewTexts = apiReviews.map { it.comment }
                val reviewsHash = analysisCache.generateReviewsHash(reviewTexts)
                
                // 1. 캐시된 번역 확인
                analysisCache.getCachedAnalysis(userId!!).collect { cached ->
                    if (cached != null && cached.reviewsHash == reviewsHash && !cached.translatedReviews.isNullOrEmpty()) {
                        println("✅ 캐시된 번역 사용")
                        translatedReviews = cached.translatedReviews
                        return@collect
                    }
                    
                    // 2. 캐시가 없으면 번역 API 호출
                    println("🔄 API에서 번역 실행")
                    try {
                        translatedReviews = translateRepo.translateReviews(reviewTexts, "ko")
                    } catch (e: Exception) {
                        println("❌ 번역 실패: ${e.message}")
                        translatedReviews = reviewTexts // 원본 사용
                    }
                }
            }
        }
    }

    // UI 모델 변환
    val allReviews: List<Review> = remember(apiReviews, translatedReviews) {
        apiReviews.mapIndexed { index, dto ->
            Review(
                content = dto.comment,
                translatedContent = translatedReviews.getOrNull(index) ?: dto.comment,
                rating = dto.rating.toFloat(),
                nationality = dto.nationality,
                timestamp = dto.createdAt,
                isPositive = dto.rating >= 4,
                recommendedMenu = dto.menuName ?: "알 수 없는 메뉴"
            )
        }
    }

    // 리뷰에서 사용 가능한 국가와 메뉴 추출
    LaunchedEffect(allReviews) {
        availableNationalities = allReviews.map { it.nationality }.distinct().take(8)
        availableMenus = allReviews.map { it.recommendedMenu }.distinct().take(10)
    }
    
    // 필터 적용된 리뷰 (OR 조건)
    val filteredReviews = remember(allReviews, selectedFilters) {
        var filtered = allReviews
        
        // 카테고리별로 필터 그룹화
        val sentimentFilters = selectedFilters.filter { it.categoryId == "sentiment" }
        val nationalityFilters = selectedFilters.filter { it.categoryId == "nationality" }
        val ratingFilters = selectedFilters.filter { it.categoryId == "rating" }
        val menuFilters = selectedFilters.filter { it.categoryId == "menu" }
        
        // 각 카테고리 내에서는 OR, 카테고리 간에는 AND
        if (sentimentFilters.isNotEmpty()) {
            filtered = filtered.filter { review ->
                sentimentFilters.any { filter ->
                    when (filter.optionId) {
                        "positive" -> review.isPositive
                        "negative" -> !review.isPositive
                        else -> false
                    }
                }
            }
        }
        
        if (nationalityFilters.isNotEmpty()) {
            val nationalities = nationalityFilters.map { it.optionId }
            filtered = filtered.filter { review ->
                nationalities.contains(review.nationality)
            }
        }
        
        if (ratingFilters.isNotEmpty()) {
            val ratings = ratingFilters.mapNotNull { it.optionId.toIntOrNull() }
            filtered = filtered.filter { review ->
                ratings.contains(review.rating.toInt())
            }
        }
        
        if (menuFilters.isNotEmpty()) {
            val menus = menuFilters.map { it.optionId }
            filtered = filtered.filter { review ->
                menus.contains(review.recommendedMenu)
            }
        }
        
        // 정렬 적용
        val sortFilter = selectedFilters.find { it.categoryId == "sort" }
        when (sortFilter?.optionId) {
            "latest" -> filtered.sortedByDescending { it.timestamp }
            "oldest" -> filtered.sortedBy { it.timestamp }
            else -> filtered.sortedByDescending { it.timestamp } // 기본값: 최신순
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 4.dp)
        ) {
        // AI 인사이트 카드 섹션
        InsightCardsSection(
            insights = if (isInsightsLoading) emptyList() else insights,
            onInsightClick = { insight ->
                println("🎯 인사이트 클릭: ${insight.title}")
                println("🔍 suggestedFilters: ${insight.suggestedFilters}")
                
                // 현재 선택된 인사이트와 동일한지 확인
                val currentInsightFilters = selectedFilters.filter { filter ->
                    insight.suggestedFilters.any { (filterType, filterValue) ->
                        filter.categoryId == filterType && filter.optionId == filterValue
                    }
                }
                
                if (currentInsightFilters.size == insight.suggestedFilters.size && 
                    currentInsightFilters.size == selectedFilters.size) {
                    // 동일한 인사이트를 다시 클릭했으면 필터 초기화
                    selectedFilters = emptyList()
                } else {
                    // 다른 인사이트를 클릭했으면 새 필터로 교체
                    val newFilters = mutableListOf<SelectedFilter>()
                    
                    insight.suggestedFilters?.forEach { (filterType, filterValue) ->
                        when (filterType) {
                            "nationality", "country" -> {
                                // 국가 코드 정규화 (USA -> US 등)
                                val normalizedValue = when (filterValue.uppercase()) {
                                    "USA" -> "US"
                                    "KOREA", "KOR" -> "KR"
                                    "JAPAN" -> "JP" 
                                    "CHINA" -> "CN"
                                    "ITALY" -> "IT"
                                    else -> filterValue
                                }
                                val label = when (normalizedValue) {
                                    "KR" -> "한국"
                                    "JP" -> "일본" 
                                    "CN" -> "중국"
                                    "US" -> "미국"
                                    "IT" -> "이탈리아"
                                    else -> normalizedValue
                                }
                                newFilters.add(SelectedFilter("nationality", normalizedValue, "국가", label))
                            }
                            "sentiment" -> {
                                val label = when (filterValue) {
                                    "positive" -> "긍정만"
                                    "negative" -> "부정만"
                                    else -> filterValue
                                }
                                newFilters.add(SelectedFilter("sentiment", filterValue, "감정", label))
                            }
                            "rating" -> {
                                newFilters.add(SelectedFilter("rating", filterValue, "평점", "${filterValue}점 이상"))
                            }
                            "sort" -> {
                                val label = when (filterValue) {
                                    "latest" -> "최신순"
                                    "oldest" -> "오래된순"
                                    else -> filterValue
                                }
                                newFilters.add(SelectedFilter("sort", filterValue, "정렬", label))
                            }
                            "menu" -> {
                                // 메뉴 필터는 메뉴 이름으로 처리
                                newFilters.add(SelectedFilter("menu", filterValue, "메뉴", filterValue))
                            }
                        }
                    }
                    
                    selectedFilters = newFilters
                }
            },
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 확장형 필터 칩들
        ExpandableFilterChips(
            availableNationalities = availableNationalities,
            availableMenus = availableMenus,
            selectedFilters = selectedFilters,
            onFilterAdd = { categoryId, optionId ->
                val categoryTitle = when (categoryId) {
                    "sentiment" -> "감정"
                    "nationality" -> "국가"
                    "rating" -> "평점"
                    "menu" -> "메뉴"
                    "sort" -> "정렬"
                    else -> categoryId
                }
                val optionLabel = when {
                    categoryId == "sentiment" && optionId == "positive" -> "긍정만"
                    categoryId == "sentiment" && optionId == "negative" -> "부정만"
                    categoryId == "rating" -> optionId
                    categoryId == "sort" && optionId == "latest" -> "최신순"
                    categoryId == "sort" && optionId == "oldest" -> "오래된순"
                    else -> optionId
                }
                
                // 정렬은 하나만 선택 가능
                val newFilters = if (categoryId == "sort") {
                    selectedFilters.filter { it.categoryId != "sort" } + SelectedFilter(categoryId, optionId, categoryTitle, optionLabel)
                } else {
                    selectedFilters + SelectedFilter(categoryId, optionId, categoryTitle, optionLabel)
                }
                selectedFilters = newFilters
            },
            onFilterRemove = { filter ->
                selectedFilters = selectedFilters.filter { it != filter }
            },
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
        ) {
            items(filteredReviews) { review ->
                ReviewItemCard(review = review)
            }
        }
        }
        
        // 플로팅 액션 버튼 (필터가 선택되어 있을 때만 표시)
        if (selectedFilters.isNotEmpty()) {
            FloatingActionButton(
                onClick = {
                    selectedFilters = emptyList()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.error
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "초기화",
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}


