package com.voyz.presentation.component.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voyz.datas.model.DailyMarketingOpportunities
import com.voyz.datas.model.Priority
import com.voyz.ui.theme.MarketingColors
import java.time.LocalDate

@Composable
fun MarketingCalendarDayCell(
    modifier: Modifier = Modifier,
    date: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    dailyOpportunities: DailyMarketingOpportunities?,
    onClick: () -> Unit
) {
    val textColor = when {
        !isCurrentMonth -> MarketingColors.TextTertiary
        date.dayOfWeek.value == 7 -> MarketingColors.HighPriority
        date.dayOfWeek.value == 6 -> MarketingColors.TextSecondary
        else -> MarketingColors.TextPrimary
    }

    Column(
        modifier = modifier
            .background(Color.White)
            .drawBehind {
                drawLine(
                    color = MarketingColors.TextTertiary.copy(alpha = 0.2f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .clickable { onClick() }
            .padding(4.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // 날짜 + 날씨
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                fontWeight = FontWeight.Medium,
                modifier = if (isSelected) {
                    Modifier.background(Color(0xFF2196F3), CircleShape)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                } else Modifier.padding(2.dp)
            )
            val today = LocalDate.now()
            val startOfWeek = today.minusDays(today.dayOfWeek.value % 7L)
            val endOfWeek = startOfWeek.plusDays(6)
            if (date in startOfWeek..endOfWeek) {
                val emoji = remember(date) {
                    listOf("☀️", "🌤️", "☁️", "🌧️", "⛅")[date.dayOfMonth % 5]
                }
                Text(
                    text = emoji,
                    fontSize = 10.sp,
                    modifier = if (!isCurrentMonth) Modifier.alpha(0.3f) else Modifier
                )
            }
        }

        // 마케팅 기회 영역 (weight 사용)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Top
        ) {
            // 변경: 처음 5개만 표시
            dailyOpportunities?.opportunities?.take(5)?.forEach { opportunity ->
                // 리마인더 vs 제안 처리 로직 그대로
                val contentModifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 2.dp)  // padding을 2dp로 줄임

                if (opportunity.title.startsWith("[리마인더]")) {
                    // 리마인더 박스
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = contentModifier
                            .alpha(if (!isCurrentMonth) 0.3f else 1f)
                    ) {
                        // 세로 바는 그대로
                        // …
                        Text(
                            text = opportunity.title.removePrefix("[리마인더] "),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,     // 글자 크기 8sp로 줄임
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    // 제안/기회 박스
                    Box(
                        modifier = contentModifier
                            .background(
                                color = when (opportunity.priority) {
                                    Priority.MEDIUM -> Color(0xFFFFC107).copy(alpha = 0.4f)
                                    Priority.LOW -> Color(0xFF9E9E9E).copy(alpha = 0.4f)
                                    else -> Color(0xFF9E9E9E).copy(alpha = 0.4f)
                                },
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 2.dp, vertical = 2.dp)  // 내부 패딩도 줄임
                            .alpha(if (!isCurrentMonth) 0.3f else 1f)
                    ) {
                        Text(
                            text = opportunity.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,     // 글자 크기 8sp로 줄임
                            maxLines = 2,        // 한 줄로 충분하다면 1줄로 줄이기
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 더 많은 기회가 있을 때 +카운터 (5개 이후)
            dailyOpportunities?.let {
                val extra = it.totalCount - 5
                if (extra > 0) {
                    Text(
                        text = "+$extra",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,   // 카운터도 좀 더 작게
                        modifier = Modifier.padding(top = 2.dp, start = 2.dp)
                    )
                }
            }
        }
    }
}