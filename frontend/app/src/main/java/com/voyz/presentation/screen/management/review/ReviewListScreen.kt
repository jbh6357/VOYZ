package com.voyz.presentation.screen.management.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.voyz.presentation.screen.management.review.component.ReviewItemCard
import com.voyz.presentation.screen.management.review.model.Review
import com.voyz.datas.datastore.UserPreferencesManager
import com.voyz.datas.repository.AnalyticsRepository
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
    var apiReviews by remember { mutableStateOf<List<com.voyz.datas.model.dto.ReviewResponseDto>>(emptyList()) }
    LaunchedEffect(userId) {
        val id = userId ?: return@LaunchedEffect
        val start = defaultStart.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val end = defaultEnd.format(DateTimeFormatter.ISO_LOCAL_DATE)
        runCatching { apiReviews = repo.getReviews(id, start, end) }
    }

    // UI 모델 변환
    val allReviews: List<Review> = remember(apiReviews) {
        apiReviews.map { dto ->
            Review(
                content = dto.comment,
                translatedContent = dto.comment, // 번역 미적용
                rating = dto.rating.toFloat(),
                nationality = dto.nationality,
                timestamp = dto.createdAt,
                isPositive = dto.rating >= 4,
                recommendedMenu = "메뉴 #${dto.menuIdx}"
            )
        }
    }

    // 메뉴 드롭다운
    var expanded by remember { mutableStateOf(false) }
    var selectedMenu by remember { mutableStateOf<String?>(null) }

    // 필터 상태
    var selectedReviewType by remember { mutableStateOf("전체") }
    var selectedNationalities by remember { mutableStateOf(setOf<String>()) }
    var selectedRatingRanges by remember { mutableStateOf(setOf<String>()) }
    var selectedMenus by remember { mutableStateOf(setOf<String>()) }
    var showFilterDialog by remember { mutableStateOf(false) }

    val allMenus = remember(allReviews) { allReviews.map { it.recommendedMenu }.distinct() }

    val filteredReviews = allReviews.filter { review:
        Review ->
        val typeMatch = when (selectedReviewType) {
            "긍정" -> review.isPositive
            "부정" -> !review.isPositive
            else -> true
        }
        val nationalityMatch = selectedNationalities.isEmpty() || selectedNationalities.contains(review.nationality)
        val ratingMatch = if (selectedRatingRanges.isEmpty()) true else selectedRatingRanges.any {
            val (min, max) = it.split("~").map(String::toFloat)
            review.rating in min..max
        }
        val menuMatch = (selectedMenu == null || review.recommendedMenu == selectedMenu) &&
            (selectedMenus.isEmpty() || selectedMenus.contains(review.recommendedMenu))
        typeMatch && nationalityMatch && ratingMatch && menuMatch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 메뉴 드롭다운
            Box {
                OutlinedButton(onClick = { expanded = true }) {
                    Text(selectedMenu ?: "메뉴 선택")
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("전체") }, onClick = {
                        selectedMenu = null
                        expanded = false
                    })
                    allMenus.forEach { menu ->
                        DropdownMenuItem(text = { Text(menu) }, onClick = {
                            selectedMenu = menu
                            expanded = false
                        })
                    }
                }
            }

            IconButton(onClick = { showFilterDialog = true }) {
                Icon(Icons.Default.FilterList, contentDescription = "리뷰 필터")
            }
        }

        // 선택된 메뉴의 키워드(필요 시 API 연결 확장 가능)
        selectedMenu?.let { menuName ->
            Spacer(Modifier.height(8.dp))
            Text("선택된 메뉴: $menuName", style = MaterialTheme.typography.titleMedium)
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
        ) {
            items(filteredReviews) { review ->
                ReviewItemCard(review = review)
            }
        }
    }

    if (showFilterDialog) {
        FilterDialog(
            selectedType = selectedReviewType,
            onTypeChange = { selectedReviewType = it },

            selectedNationalities = selectedNationalities,
            onNationalityToggle = { nation ->
                selectedNationalities = selectedNationalities.toMutableSet().apply {
                    if (contains(nation)) remove(nation) else add(nation)
                }
            },

            selectedRatingRanges = selectedRatingRanges,
            onRatingToggle = { range ->
                selectedRatingRanges = selectedRatingRanges.toMutableSet().apply {
                    if (contains(range)) remove(range) else add(range)
                }
            },

            allMenus = allMenus,
            selectedMenus = selectedMenus,
            onMenuToggle = { menu ->
                selectedMenus = selectedMenus.toMutableSet().apply {
                    if (contains(menu)) remove(menu) else add(menu)
                }
            },

            onDismiss = { showFilterDialog = false },
            onApply = { showFilterDialog = false }
        )
    }
}


