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
    maxOpportunitiesToShow: Int = 3, // 표시할 기회 개수를 파라미터로 설정 가능
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
                color = if (isSelected) Color.White else textColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                modifier = if (isSelected) {
                    Modifier.background(
                        Color(0xFF2196F3),
                        CircleShape
                    ).padding(horizontal = 6.dp, vertical = 2.dp)
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
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    modifier = if (!isCurrentMonth) Modifier.alpha(0.3f) else Modifier
                )
            }
        }

        // 마케팅 기회 영역
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Top
        ) {
            dailyOpportunities?.opportunities?.take(maxOpportunitiesToShow)?.forEach { opportunity ->
                val contentModifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 1.dp) // feat/des/reminder의 컴팩트한 패딩

                when {
                    // ID 기반 구분 (dev의 개선사항)
                    opportunity.id.startsWith("reminder_") -> {
                        val barColor = when (opportunity.priority) {
                            Priority.HIGH -> Color(0xFFFF4444) // 마케팅 -> 빨간색
                            Priority.MEDIUM -> Color(0xFF2196F3) // 일정 -> 파란색
                            else -> Color(0xFF2196F3) // 기본값 파란색
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = contentModifier
                                .alpha(if (!isCurrentMonth) 0.3f else 1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(14.dp) // 조금 더 작게
                                    .background(barColor, RoundedCornerShape(2.dp))
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = opportunity.title.removePrefix("[리마인더] "),
                                color = MarketingColors.TextPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp, // feat/des/reminder의 작은 폰트
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // AI 제안
                    opportunity.id.startsWith("suggestion_") -> {
                        Box(
                            modifier = contentModifier
                                .background(
                                    Color(0xFFFFC107).copy(alpha = 0.4f), // 제안 -> 노란색
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 3.dp, vertical = 2.dp) // 더 작은 내부 패딩
                                .alpha(if (!isCurrentMonth) 0.3f else 1f)
                        ) {
                            Text(
                                text = opportunity.title,
                                color = MarketingColors.TextPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp, // feat/des/reminder의 작은 폰트
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 10.sp,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                    
                    // 기념일/특별한 날
                    opportunity.id.startsWith("special_day_") -> {
                        Box(
                            modifier = contentModifier
                                .background(
                                    Color(0xFF9E9E9E).copy(alpha = 0.4f), // 기회 -> 회색
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 3.dp, vertical = 2.dp)
                                .alpha(if (!isCurrentMonth) 0.3f else 1f)
                        ) {
                            Text(
                                text = opportunity.title,
                                color = MarketingColors.TextPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 10.sp,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                    
                    // 타이틀 기반 리마인더 (하위 호환성)
                    opportunity.title.startsWith("[리마인더]") -> {
                        val barColor = when (opportunity.priority) {
                            Priority.HIGH -> Color(0xFFFF4444)
                            Priority.MEDIUM -> Color(0xFF2196F3)
                            else -> Color(0xFF2196F3)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = contentModifier
                                .alpha(if (!isCurrentMonth) 0.3f else 1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(14.dp)
                                    .background(barColor, RoundedCornerShape(2.dp))
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = opportunity.title.removePrefix("[리마인더] "),
                                color = MarketingColors.TextPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // 기본 케이스
                    else -> {
                        val backgroundColor = when (opportunity.priority) {
                            Priority.MEDIUM -> Color(0xFFFFC107).copy(alpha = 0.4f)
                            Priority.LOW -> Color(0xFF9E9E9E).copy(alpha = 0.4f)
                            else -> Color(0xFF9E9E9E).copy(alpha = 0.4f)
                        }
                        Box(
                            modifier = contentModifier
                                .background(backgroundColor, RoundedCornerShape(4.dp))
                                .padding(horizontal = 3.dp, vertical = 2.dp)
                                .alpha(if (!isCurrentMonth) 0.3f else 1f)
                        ) {
                            Text(
                                text = opportunity.title,
                                color = MarketingColors.TextPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 10.sp,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }
            
            // 더 많은 기회가 있을 때 카운터 표시
            dailyOpportunities?.let {
                val extra = it.totalCount - maxOpportunitiesToShow
                if (extra > 0) {
                    Text(
                        text = "+$extra",
                        color = MarketingColors.TextSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp, // feat/des/reminder의 작은 폰트
                        modifier = Modifier
                            .padding(top = 2.dp, start = 2.dp)
                            .alpha(if (!isCurrentMonth) 0.3f else 1.0f)
                    )
                }
            }
        }
    }
}