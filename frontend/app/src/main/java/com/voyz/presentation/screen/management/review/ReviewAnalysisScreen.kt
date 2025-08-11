package com.voyz.presentation.screen.management.review

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voyz.presentation.screen.management.review.component.CountryRatingChart
import com.voyz.presentation.screen.management.review.component.MenuSentimentChart
import com.voyz.datas.datastore.UserPreferencesManager
import com.voyz.datas.datastore.ReviewAnalysisCache
import com.voyz.datas.repository.AnalyticsRepository
import com.voyz.datas.model.dto.CountryRatingItem
import com.voyz.datas.model.dto.CountryRatingDto
import com.voyz.datas.model.dto.MenuSentimentDto
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.LocalDate

@Composable
fun ReviewAnalysisScreen() {
    val today = LocalDate.now()
    val defaultStart = today.minusMonths(1).withDayOfMonth(1)
    val defaultEnd = today

    // userId 로드
    val context = androidx.compose.ui.platform.LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    val scope = rememberCoroutineScope()
    var userId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        userPreferencesManager.userId.collect { fetched ->
            userId = fetched
        }
    }

    // 데이터 상태
    val analyticsRepository = remember { AnalyticsRepository() }
    val analysisCache = remember { ReviewAnalysisCache(context) }
    
    // 국가별 분석 상태
    var countryDateRange by remember { mutableStateOf(defaultStart to defaultEnd) }
    var countryRatings by remember { mutableStateOf<List<CountryRatingDto>>(emptyList()) }
    var isCountryLoading by remember { mutableStateOf(false) }
    
    // 메뉴별 분석 상태
    var menuDateRange by remember { mutableStateOf(defaultStart to defaultEnd) }
    var isMenuLoading by remember { mutableStateOf(false) }
    var selectedNationality by remember { mutableStateOf<String?>(null) }
    var availableNationalities by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // 메뉴별 분석 데이터 (국가별로 개별 로드)
    var menuSentiments by remember { mutableStateOf<List<MenuSentimentDto>>(emptyList()) }

    // 국가별 분석 데이터 로드 함수 (캐시 우선)
    fun loadCountryData() {
        val id = userId ?: return
        scope.launch {
            isCountryLoading = true
            
            try {
                // 1. 캐시된 데이터 먼저 확인
                analysisCache.getCachedAnalysis(id).collect { cached ->
                    if (cached != null && !cached.countryRatings.isNullOrEmpty()) {
                        println("✅ 캐시된 국가별 분석 사용")
                        countryRatings = cached.countryRatings.map { countryMap ->
                            CountryRatingDto(
                                nationality = countryMap["nationality"] as? String ?: "",
                                count = (countryMap["count"] as? Number)?.toLong() ?: 0L,
                                averageRating = (countryMap["averageRating"] as? Number)?.toDouble() ?: 0.0
                            )
                        }
                        isCountryLoading = false
                        return@collect
                    }
                    
                    // 2. 캐시가 없으면 API 호출
                    println("🔄 API에서 국가별 분석 로드")
                    val start = countryDateRange.first.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val end = countryDateRange.second.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    
                    countryRatings = analyticsRepository.getCountryRatings(id, start, end)
                    isCountryLoading = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                isCountryLoading = false
            }
        }
    }


    // 메뉴별 분석 데이터 로드 함수 (국가별 개별 캐시)
    fun loadMenuData() {
        val id = userId ?: return
        scope.launch {
            isMenuLoading = true
            
            try {
                // 1. 해당 국가의 캐시 확인
                analysisCache.getMenuAnalysisCache(id, selectedNationality).collect { cachedMenus ->
                    if (cachedMenus.isNotEmpty()) {
                        println("✅ 캐시된 메뉴별 분석 사용 (국가: ${selectedNationality ?: "전체"})")
                        menuSentiments = cachedMenus.map { menuMap ->
                            MenuSentimentDto(
                                menuId = (menuMap["menuId"] as? Number)?.toInt() ?: 0,
                                menuName = menuMap["menuName"] as? String ?: "",
                                positiveCount = (menuMap["positiveCount"] as? Number)?.toLong() ?: 0L,
                                negativeCount = (menuMap["negativeCount"] as? Number)?.toLong() ?: 0L,
                                neutralCount = (menuMap["neutralCount"] as? Number)?.toLong() ?: 0L,
                                averageRating = (menuMap["averageRating"] as? Number)?.toDouble() ?: 0.0,
                                reviewSummary = menuMap["reviewSummary"] as? String,
                                nationality = menuMap["nationality"] as? String
                            )
                        }
                        isMenuLoading = false
                        return@collect
                    }
                    
                    // 2. 캐시가 없으면 해당 국가 API 호출
                    println("🔄 API에서 메뉴별 분석 로드 (국가: ${selectedNationality ?: "전체"})")
                    val start = menuDateRange.first.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val end = menuDateRange.second.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    
                    val apiResult = analyticsRepository.getMenuSentiment(id, start, end, 4, 2, selectedNationality, true)
                    println("📊 API 응답 데이터 수: ${apiResult.size}")
                    apiResult.forEach { dto ->
                        println("🍽️ 메뉴: ${dto.menuName}, 국가: ${dto.nationality}, 긍정: ${dto.positiveCount}")
                    }
                    menuSentiments = apiResult
                    
                    // 3. 결과를 캐시에 저장
                    val menuMaps = apiResult.map { dto ->
                        mapOf<String, Any>(
                            "menuId" to dto.menuId,
                            "menuName" to (dto.menuName ?: ""),
                            "positiveCount" to dto.positiveCount,
                            "negativeCount" to dto.negativeCount,
                            "neutralCount" to dto.neutralCount,
                            "averageRating" to dto.averageRating,
                            "reviewSummary" to (dto.reviewSummary ?: ""),
                            "nationality" to (dto.nationality ?: "")
                        )
                    }
                    analysisCache.saveMenuAnalysis(id, selectedNationality, menuMaps)
                    
                    isMenuLoading = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                isMenuLoading = false
            }
        }
    }

    // 국가 목록 로드 함수
    fun loadNationalities() {
        val id = userId ?: return
        scope.launch {
            try {
                val nationalities = analyticsRepository.getReviewNationalities(id)
                println("🌍 사용 가능한 국가 목록: $nationalities")
                availableNationalities = nationalities
            } catch (e: Exception) {
                println("❌ 국가 목록 로드 실패: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // 초기 데이터 로드
    LaunchedEffect(userId) {
        if (userId != null) {
            loadCountryData()
            loadMenuData()
            loadNationalities()
        }
    }

    // 국가별 평점 데이터를 CountryRatingItem으로 변환 (리뷰 많은 순, 같으면 평점 높은 순)
    val countryRatingItems = remember(countryRatings) {
        val totalCount = countryRatings.sumOf { it.count }
        countryRatings
            .sortedWith(compareByDescending<CountryRatingDto> { it.count }.thenByDescending { it.averageRating })
            .take(5)
            .map { rating ->
                CountryRatingItem(
                    nationality = rating.nationality,
                    flag = com.voyz.presentation.screen.management.review.util.NationalityFlagMapper.flagFor(rating.nationality),
                    count = rating.count,
                    averageRating = rating.averageRating,
                    percentage = if (totalCount > 0) rating.count.toFloat() / totalCount else 0f
                )
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 상단 여백
        Spacer(modifier = Modifier.height(24.dp))

        // 국가별 분석 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // 제목과 기간 세그먼트 컨트롤
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "리뷰 많은 국가",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1D1D1F)
                    )
                    
                    // 기간 세그먼트 컨트롤
                    val periodOptions = listOf(
                        "지난 7일" to { 
                            val end = LocalDate.now()
                            val start = end.minusDays(6)
                            start to end
                        },
                        "이번 달" to {
                            val today = LocalDate.now()
                            val start = today.withDayOfMonth(1)
                            val end = today
                            start to end
                        },
                        "올해" to {
                            val today = LocalDate.now()
                            val start = today.withDayOfYear(1)
                            val end = today
                            start to end
                        }
                    )
                    
                    var selectedCountryIndex by remember { mutableStateOf(1) } // 기본값: 이번 달
                    
                    Box(
                        modifier = Modifier
                            .width(161.dp)
                            .background(
                                color = Color(0xFFF2F2F7),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(2.dp)
                    ) {
                        Row {
                            periodOptions.forEachIndexed { index, (label, rangeFn) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (selectedCountryIndex == index) Color.White
                                            else Color.Transparent
                                        )
                                        .clickable { 
                                            selectedCountryIndex = index
                                            countryDateRange = rangeFn()
                                            loadCountryData()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (selectedCountryIndex == index) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (selectedCountryIndex == index) Color(0xFF1D1D1F) else Color(0xFF8E8E93)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isCountryLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFCD212A)
                        )
                    }
                } else {
                    CountryRatingChart(
                        data = countryRatingItems
                    )
                }
            }
        }

        // 메뉴별 분석 카드
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // 제목과 국가 드롭다운
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "메뉴별 분석",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1D1D1F)
                    )
                    
                    // 국가 좌우 버튼 네비게이션
                    val nationalityOptions = listOf(null) + availableNationalities // null = 전체
                    var currentNationalityIndex by remember { mutableStateOf(0) }
                    
                    // 선택된 국가 동기화 (API 호출)
                    LaunchedEffect(currentNationalityIndex, nationalityOptions) {
                        if (nationalityOptions.isNotEmpty()) {
                            selectedNationality = nationalityOptions.getOrNull(currentNationalityIndex)
                            loadMenuData() // 국가 변경시 API 호출
                        }
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // 왼쪽 버튼
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clickable(enabled = nationalityOptions.size > 1 && !isMenuLoading) {
                                    currentNationalityIndex = (currentNationalityIndex - 1 + nationalityOptions.size) % nationalityOptions.size
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "◀",
                                fontSize = 10.sp,
                                color = if (nationalityOptions.size > 1 && !isMenuLoading) Color(0xFF1D1D1F) else Color(0xFF8E8E93)
                            )
                        }
                        
                        // 현재 국가 표시
                        Box(
                            modifier = Modifier
                                .width(71.dp)
                                .background(
                                    color = Color(0xFFF2F2F7),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val displayText = if (selectedNationality == null) {
                                "전체"
                            } else {
                                val flag = com.voyz.presentation.screen.management.review.util.NationalityFlagMapper.flagFor(selectedNationality!!)
                                "$flag $selectedNationality"
                            }
                            
                            Text(
                                text = displayText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1D1D1F),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        
                        // 오른쪽 버튼
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clickable(enabled = nationalityOptions.size > 1 && !isMenuLoading) {
                                    currentNationalityIndex = (currentNationalityIndex + 1) % nationalityOptions.size
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "▶",
                                fontSize = 10.sp,
                                color = if (nationalityOptions.size > 1 && !isMenuLoading) Color(0xFF1D1D1F) else Color(0xFF8E8E93)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isMenuLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFCD212A)
                        )
                    }
                } else {
                    MenuSentimentChart(
                        data = menuSentiments,
                        onMenuClick = { menu ->
                            // TODO: 메뉴 상세 정보 표시
                        }
                    )
                }
            }
        }
        
        // 하단 패딩
        Spacer(modifier = Modifier.height(24.dp))
    }
}



