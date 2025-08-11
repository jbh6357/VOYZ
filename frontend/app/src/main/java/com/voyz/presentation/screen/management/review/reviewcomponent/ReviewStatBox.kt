package com.voyz.presentation.screen.management.review.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewStatBox(
    totalReviews: Int,
    averageRating: Float,
    positiveReviews: Int,
    negativeReviews: Int,
    topPositiveKeywords: List<String>,
    topNegativeKeywords: List<String>
) {
    // ✅ 애니메이션 상태
    val animatedTotal = animateIntAsState(
        targetValue = totalReviews,
        animationSpec = tween(durationMillis = 600),
        label = "totalReviewCount"
    )

    val animatedRating = animateFloatAsState(
        targetValue = averageRating,
        animationSpec = tween(durationMillis = 600),
        label = "averageRating"
    )

    val showKeywords = remember { mutableStateOf(false) }

    // ✅ 진입 시 키워드 박스 등장 애니메이션
    LaunchedEffect(Unit) {
        showKeywords.value = true
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ✅ 총 리뷰 수 + 평균 별점
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "총 리뷰 수: ${animatedTotal.value}개",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = "⭐ 평균 별점: ${String.format("%.1f", animatedRating.value)}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        color = Color(0xFFFFC107)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ 긍정/부정 리뷰 수 + 키워드 박스
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "긍정 리뷰: ${positiveReviews}개", color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    AnimatedVisibility(visible = showKeywords.value, enter = fadeIn(tween(400)) + slideInVertically(tween(400))) {
                        KeywordChips(title = "긍정 키워드", keywords = topPositiveKeywords, backgroundColor = Color(0xFFE8F5E9))
                    }
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "부정 리뷰: ${negativeReviews}개", color = Color(0xFFF44336), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    AnimatedVisibility(visible = showKeywords.value, enter = fadeIn(tween(500)) + slideInVertically(tween(500))) {
                        KeywordChips(title = "부정 키워드", keywords = topNegativeKeywords, backgroundColor = Color(0xFFFFEBEE))
                    }
                }
            }
        }
    }
}

@Composable
private fun KeywordColumn(
    title: String,
    keywords: List<String>,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp), modifier = Modifier.padding(bottom = 6.dp))
        keywords.forEach { Text(text = "• $it", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, color = Color.DarkGray)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KeywordChips(title: String, keywords: List<String>, backgroundColor: Color) {
    Column(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp))
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            keywords.take(10).forEach { k ->
                Surface(shape = RoundedCornerShape(999.dp), color = Color.White, tonalElevation = 1.dp) {
                    Text(text = k, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), fontSize = 13.sp)
                }
            }
        }
    }
}
