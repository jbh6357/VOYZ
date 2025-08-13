package com.voyz.presentation.screen.management.review.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FilterChipItem(
    val id: String,
    val label: String,
    val emoji: String = "",
    val isSelected: Boolean = false,
    val type: FilterType
)

enum class FilterType {
    SENTIMENT, NATIONALITY, RATING, SORT
}

@Composable
fun FilterChips(
    availableNationalities: List<String> = emptyList(),
    selectedSentiment: String = "전체",
    selectedNationality: String? = null,
    selectedRating: Int? = null,
    selectedSort: String = "최신순",
    onSentimentChange: (String) -> Unit = {},
    onNationalityChange: (String?) -> Unit = {},
    onRatingChange: (Int?) -> Unit = {},
    onSortChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 필터 칩 데이터 생성
    val chips = remember(availableNationalities, selectedSentiment, selectedNationality, selectedRating, selectedSort) {
        buildList {
            // 감정 필터
            add(FilterChipItem("all", "전체", "", selectedSentiment == "전체", FilterType.SENTIMENT))
            add(FilterChipItem("positive", "긍정만", "😊", selectedSentiment == "긍정", FilterType.SENTIMENT))
            add(FilterChipItem("negative", "부정만", "😞", selectedSentiment == "부정", FilterType.SENTIMENT))
            
            // 국가 필터 (사용 가능한 국가만)
            availableNationalities.take(5).forEach { nationality ->
                val flag = getNationalityFlag(nationality)
                val isSelected = selectedNationality == nationality
                add(FilterChipItem(nationality, nationality, flag, isSelected, FilterType.NATIONALITY))
            }
            
            // 평점 필터
            listOf(5, 4, 3, 2, 1).forEach { rating ->
                val stars = "⭐".repeat(rating)
                val isSelected = selectedRating == rating
                add(FilterChipItem("rating_$rating", "${rating}점", stars, isSelected, FilterType.RATING))
            }
            
            // 정렬
            add(FilterChipItem("latest", "최신순", "📅", selectedSort == "최신순", FilterType.SORT))
            add(FilterChipItem("oldest", "오래된순", "📊", selectedSort == "오래된순", FilterType.SORT))
        }
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { chip ->
            FilterChip(
                chip = chip,
                onClick = {
                    when (chip.type) {
                        FilterType.SENTIMENT -> {
                            when (chip.id) {
                                "all" -> onSentimentChange("전체")
                                "positive" -> onSentimentChange("긍정")
                                "negative" -> onSentimentChange("부정")
                            }
                        }
                        FilterType.NATIONALITY -> {
                            if (chip.isSelected) {
                                onNationalityChange(null)
                            } else {
                                onNationalityChange(chip.id)
                            }
                        }
                        FilterType.RATING -> {
                            val rating = chip.id.removePrefix("rating_").toIntOrNull()
                            if (chip.isSelected) {
                                onRatingChange(null)
                            } else {
                                onRatingChange(rating)
                            }
                        }
                        FilterType.SORT -> {
                            when (chip.id) {
                                "latest" -> onSortChange("최신순")
                                "oldest" -> onSortChange("오래된순")
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun FilterChip(
    chip: FilterChipItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (chip.isSelected) Color(0xFFCD212A) else Color.White
    val textColor = if (chip.isSelected) Color.White else Color(0xFF374151)
    val borderColor = if (chip.isSelected) Color(0xFFCD212A) else Color(0xFFE5E7EB)
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (chip.emoji.isNotEmpty()) {
                Text(
                    text = chip.emoji,
                    fontSize = 14.sp
                )
            }
            
            Text(
                text = chip.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                maxLines = 1
            )
        }
    }
}

private fun getNationalityFlag(nationality: String): String {
    return com.voyz.presentation.screen.management.review.util.NationalityFlagMapper.flagFor(nationality)
}