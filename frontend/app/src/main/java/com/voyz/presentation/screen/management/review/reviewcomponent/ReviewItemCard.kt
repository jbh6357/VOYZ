package com.voyz.presentation.screen.management.review.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voyz.presentation.screen.management.review.model.Review
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ReviewItemCard(review: Review) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 상단: 국기 + 국가명 + 평점
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Text(
                        text = getFlagEmoji(review.nationality),
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

            Spacer(modifier = Modifier.height(8.dp))

            // 리뷰 본문 (한글)
            Text(
                text = review.content,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 작성일
            val formattedDateTime = try {
                val parsed = LocalDateTime.parse(review.timestamp, DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
                parsed.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
            } catch (e: Exception) {
                review.timestamp
            }

            Text(
                text = "작성일: $formattedDateTime",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

// 국가명 → 국기 이모지 변환 함수
fun getFlagEmoji(nationality: String): String {
    return when (nationality) {
        "한국" -> "\uD83C\uDDF0\uD83C\uDDF7" // 🇰🇷
        "미국" -> "\uD83C\uDDFA\uD83C\uDDF8" // 🇺🇸
        "일본" -> "\uD83C\uDDEF\uD83C\uDDF5" // 🇯🇵
        "중국" -> "\uD83C\uDDE8\uD83C\uDDF3" // 🇨🇳
        "영국" -> "\uD83C\uDDEC\uD83C\uDDE7" // 🇬🇧
        "프랑스" -> "\uD83C\uDDEB\uD83C\uDDF7" // 🇫🇷
        else -> "\uD83C\uDFF3️" // 기본: 흰 깃발
    }
}
