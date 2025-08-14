package com.voyz.presentation.screen.management.review.component

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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

data class FilterCategory(
    val id: String,
    val title: String,
    val emoji: String,
    val options: List<FilterOption>
)

data class FilterOption(
    val id: String,
    val label: String,
    val emoji: String = ""
)

data class SelectedFilter(
    val categoryId: String,
    val optionId: String,
    val categoryTitle: String,
    val optionLabel: String
)

@Composable
fun ExpandableFilterChips(
    availableNationalities: List<String> = emptyList(),
    availableMenus: List<String> = emptyList(),
    selectedFilters: List<SelectedFilter> = emptyList(),
    onFilterAdd: (categoryId: String, optionId: String) -> Unit = { _, _ -> },
    onFilterRemove: (SelectedFilter) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expandedCategoryId by remember { mutableStateOf<String?>(null) }
    
    // 카테고리 데이터
    val allCategories = remember(availableNationalities, availableMenus) {
        listOf(
            FilterCategory(
                id = "sentiment",
                title = "감정",
                emoji = "😊",
                options = listOf(
                    FilterOption("positive", "긍정만", "😊"),
                    FilterOption("negative", "부정만", "😞")
                )
            ),
            FilterCategory(
                id = "nationality",
                title = "국가",
                emoji = "🌍",
                options = availableNationalities.map { nationality ->
                    val flag = getNationalityFlag(nationality)
                    FilterOption(nationality, nationality, flag)
                }
            ),
            FilterCategory(
                id = "rating",
                title = "평점",
                emoji = "⭐",
                options = listOf(
                    FilterOption("5", "5", "⭐⭐⭐⭐⭐"),
                    FilterOption("4", "4", "⭐⭐⭐⭐"),
                    FilterOption("3", "3", "⭐⭐⭐"),
                    FilterOption("2", "2", "⭐⭐"),
                    FilterOption("1", "1", "⭐")
                )
            ),
            FilterCategory(
                id = "menu",
                title = "메뉴",
                emoji = "🍽️",
                options = availableMenus.map { menu ->
                    FilterOption(menu, menu, "🍽️")
                }
            ),
            FilterCategory(
                id = "sort",
                title = "정렬",
                emoji = "📊",
                options = listOf(
                    FilterOption("latest", "최신순", "📅"),
                    FilterOption("oldest", "오래된순", "📊")
                )
            )
        )
    }
    
    // 선택된 카테고리를 앞으로 정렬
    val sortedCategories = remember(allCategories, selectedFilters) {
        val selectedCategoryIds = selectedFilters.map { it.categoryId }.distinct()
        val selectedCategories = allCategories.filter { it.id in selectedCategoryIds }
        val unselectedCategories = allCategories.filter { it.id !in selectedCategoryIds }
        selectedCategories + unselectedCategories
    }
    
    Column(modifier = modifier) {
        // 카테고리들을 한 행에 배치하면서, 확장된 카테고리 사이에 옵션 삽입
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            sortedCategories.forEachIndexed { index, category ->
                // 카테고리 칩
                CategoryChip(
                    category = category,
                    isExpanded = expandedCategoryId == category.id,
                    selectedCount = selectedFilters.count { it.categoryId == category.id },
                    selectedOptions = selectedFilters
                        .filter { it.categoryId == category.id }
                        .mapNotNull { selectedFilter ->
                            category.options.find { it.id == selectedFilter.optionId }
                        },
                    onClick = {
                        expandedCategoryId = if (expandedCategoryId == category.id) null else category.id
                    }
                )
                
                // 이 카테고리가 확장된 경우 바로 옆에 옵션들 표시
                AnimatedVisibility(
                    visible = expandedCategoryId == category.id,
                    enter = expandHorizontally(
                        animationSpec = tween(250)
                    ) + fadeIn(animationSpec = tween(250)),
                    exit = shrinkHorizontally(
                        animationSpec = tween(250)
                    ) + fadeOut(animationSpec = tween(250))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        category.options.forEach { option ->
                            val isSelected = selectedFilters.any { 
                                it.categoryId == category.id && it.optionId == option.id 
                            }
                            OptionChip(
                                option = option,
                                isSelected = isSelected,
                                onClick = {
                                    val existingFilter = selectedFilters.find { 
                                        it.categoryId == category.id && it.optionId == option.id 
                                    }
                                    if (existingFilter != null) {
                                        onFilterRemove(existingFilter)
                                    } else {
                                        onFilterAdd(category.id, option.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: FilterCategory,
    isExpanded: Boolean,
    selectedCount: Int,
    onClick: () -> Unit,
    selectedOptions: List<FilterOption> = emptyList(),
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selectedCount > 0 || isExpanded) Color(0xFFCD212A) else Color.White
    val textColor = if (selectedCount > 0 || isExpanded) Color.White else Color(0xFF374151)
    
    Card(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = category.emoji,
                fontSize = 14.sp
            )
            
            Text(
                text = category.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            
            // 선택된 옵션들 표시 (A타입: 고정 표시)
            if (selectedOptions.isNotEmpty()) {
                Text(
                    text = ": ${selectedOptions.joinToString(", ") { it.label }}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor,
                    maxLines = 1
                )
            }
        }
    }
}


@Composable
private fun OptionChip(
    option: FilterOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color(0xFFCD212A) else Color(0xFFF8FAFC)
    val textColor = if (isSelected) Color.White else Color(0xFF374151)
    
    Card(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (option.emoji.isNotEmpty()) {
                Text(
                    text = option.emoji,
                    fontSize = 12.sp
                )
            }
            
            Text(
                text = option.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}


private fun getNationalityFlag(nationality: String): String {
    return com.voyz.presentation.screen.management.review.util.NationalityFlagMapper.flagFor(nationality)
}