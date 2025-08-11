package com.voyz.presentation.screen.management.review.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voyz.presentation.screen.management.review.model.Review
import com.voyz.presentation.screen.management.review.util.NationalityFlagMapper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ReviewItemCard(review: Review) {
    var showTranslation by remember { mutableStateOf(true) }  // 기본적으로 번역 보여주기
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ⬆️ 상단: 국기 + 국가명 + 평점
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = NationalityFlagMapper.flagFor(review.nationality),
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = review.nationality,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "⭐ ${"%.1f".format(review.rating)} / 5.0",
                    color = Color(0xFFFFC107),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // ✅ 여기에 추천 메뉴 출력 (국가 줄 아래)
            if (review.recommendedMenu.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "주문한 메뉴: ${review.recommendedMenu}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ⬇️ 리뷰 본문
            Text(
                text = if (showTranslation && review.translatedContent.isNotEmpty()) 
                    review.translatedContent.replace("\n", " ").trim() else review.content.replace("\n", " ").trim(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ⬇️ 작성일
            val formattedDateTime = try {
                when {
                    // 마이크로초 포함된 형식: 11T20:43:18.787851 (년도/월/일 누락)
                    review.timestamp.matches(Regex("\\d{1,2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+")) -> {
                        val today = java.time.LocalDate.now()
                        val timeOnly = review.timestamp.substringAfter('T').substringBefore('.')
                        "${today.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))} $timeOnly"
                    }
                    // 완전한 ISO 8601 형식: 2025-01-15T10:30:45.123456
                    review.timestamp.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+")) -> {
                        val parsed = LocalDateTime.parse(review.timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"))
                        parsed.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
                    }
                    // ISO 8601 형식: 2025-01-15T10:30:45
                    review.timestamp.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")) -> {
                        val parsed = LocalDateTime.parse(review.timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                        parsed.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
                    }
                    // 일반 형식: 2025-01-15 10:30:45
                    review.timestamp.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) -> {
                        val parsed = LocalDateTime.parse(review.timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        parsed.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
                    }
                    // 이미 원하는 형식: 2025.01.15 10:30
                    review.timestamp.matches(Regex("\\d{4}\\.\\d{2}\\.\\d{2} \\d{2}:\\d{2}")) -> {
                        review.timestamp
                    }
                    else -> {
                        // 알 수 없는 형식은 그대로 표시
                        "시간 형식 오류"
                    }
                }
            } catch (e: Exception) {
                "시간 파싱 오류"
            }

            // ⬇️ 작성일과 번역 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "작성일: $formattedDateTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                
                // 원본/번역 토글 버튼 (항상 공간 확보)
                Box(
                    modifier = Modifier.height(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (review.translatedContent.isNotEmpty() && review.translatedContent != review.content) {
                        TextButton(
                            onClick = { showTranslation = !showTranslation },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = if (showTranslation) "원본" else "번역",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
