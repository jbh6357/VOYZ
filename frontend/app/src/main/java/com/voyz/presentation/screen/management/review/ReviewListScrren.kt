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
        Review("맛있어요", "Delicious!", 5.0f, "한국", "2025.08.01", true),
        Review("친절하지 않았어요", "Not friendly", 2.0f, "미국", "2025.08.02", false),
        Review("기다림이 길었어요", "Waited too long", 1.5f, "일본", "2025.08.03", false),
        Review("서비스 최고", "Excellent service", 4.5f, "미국", "2025.08.04", true),
        Review("보통이었어요", "It was okay", 3.0f, "한국", "2025.08.05", true),
    )

    var selectedReviewType by remember { mutableStateOf("전체") }
    var selectedNationalities by remember { mutableStateOf(setOf<String>()) }
    var selectedRatingRanges by remember { mutableStateOf(setOf<String>()) }
    var showFilterDialog by remember { mutableStateOf(false) }

    val filteredReviews = allReviews.filter { review ->
        val typeMatch = when (selectedReviewType) {
            "긍정" -> review.isPositive
            "부정" -> !review.isPositive
            else -> true
        }
        val nationalityMatch = selectedNationalities.isEmpty() || selectedNationalities.contains(review.nationality)
        val ratingMatch = if (selectedRatingRanges.isEmpty()) true
        else selectedRatingRanges.any {
            val (min, max) = it.split("~").map(String::toFloat)
            review.rating in min..max
        }
        typeMatch && nationalityMatch && ratingMatch
    }

    Column(modifier = Modifier.fillMaxSize()
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
            onDismiss = { showFilterDialog = false },
            onApply = { showFilterDialog = false }
        )
    }
}
