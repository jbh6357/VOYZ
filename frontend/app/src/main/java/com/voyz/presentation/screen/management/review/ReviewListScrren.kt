package com.voyz.presentation.screen.management.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.voyz.presentation.screen.management.review.component.ReviewItemCard
import com.voyz.presentation.screen.management.review.model.Review

@Composable
fun ReviewListScreen() {
    val allReviews = listOf(
        Review(
            content = "맛있어요",
            translatedContent = "Delicious!",
            rating = 5.0f,
            nationality = "한국",
            timestamp = "2025.08.01 13:20",
            isPositive = true,
            recommendedMenu = "김치찌개"
        ),
        Review(
            content = "친절하지 않았어요",
            translatedContent = "Not friendly",
            rating = 2.0f,
            nationality = "미국",
            timestamp = "2025.08.02 11:15",
            isPositive = false,
            recommendedMenu = "불고기"
        ),
        Review(
            content = "기다림이 길었어요",
            translatedContent = "Waited too long",
            rating = 1.5f,
            nationality = "일본",
            timestamp = "2025.08.03 18:05",
            isPositive = false,
            recommendedMenu = "된장찌개"
        ),
        Review(
            content = "서비스 최고",
            translatedContent = "Excellent service",
            rating = 4.5f,
            nationality = "미국",
            timestamp = "2025.08.04 12:45",
            isPositive = true,
            recommendedMenu = "삼겹살"
        ),
        Review(
            content = "보통이었어요",
            translatedContent = "It was okay",
            rating = 3.0f,
            nationality = "한국",
            timestamp = "2025.08.05 14:10",
            isPositive = true,
            recommendedMenu = "김치찌개"
        )
    )

    var selectedReviewType by remember { mutableStateOf("전체") }
    var selectedNationalities by remember { mutableStateOf(setOf<String>()) }
    var selectedRatingRanges by remember { mutableStateOf(setOf<String>()) }
    var showFilterDialog by remember { mutableStateOf(false) }

    // 필터 메뉴 목록 추가
    val allMenus = listOf("김치찌개", "불고기", "된장찌개", "삼겹살", "떡볶이")
    var selectedMenus by remember { mutableStateOf(setOf<String>()) }

    val filteredReviews = allReviews.filter { review ->
        val typeMatch = when (selectedReviewType) {
            "긍정" -> review.isPositive
            "부정" -> !review.isPositive
            else -> true
        }
        val nationalityMatch =
            selectedNationalities.isEmpty() || selectedNationalities.contains(review.nationality)
        val ratingMatch = if (selectedRatingRanges.isEmpty()) true
        else selectedRatingRanges.any {
            val (min, max) = it.split("~").map(String::toFloat)
            review.rating in min..max
        }
        val menuMatch =
            selectedMenus.isEmpty() || selectedMenus.contains(review.recommendedMenu)

        typeMatch && nationalityMatch && ratingMatch && menuMatch
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp)
    ) {
        // 상단 필터 아이콘
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { showFilterDialog = true }) {
                Icon(Icons.Default.FilterList, contentDescription = "리뷰 필터")
            }
        }

        // 필터된 리뷰 리스트
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp) // 상하 패딩 추가
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