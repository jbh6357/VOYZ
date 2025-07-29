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
    date: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    dailyOpportunities: DailyMarketingOpportunities?,
    onClick: () -> Unit
) {
    val textColor = when {
        !isCurrentMonth -> MarketingColors.TextTertiary
        date.dayOfWeek.value == 7 -> MarketingColors.HighPriority // 일요일 빨간색
        date.dayOfWeek.value == 6 -> MarketingColors.TextSecondary // 토요일 회색
        else -> MarketingColors.TextPrimary
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .drawBehind {
                // 윗줄만 그리기
                drawLine(
                    color = MarketingColors.TextTertiary.copy(alpha = 0.2f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 0.5.dp.toPx()
                )
            }
            .clickable { onClick() }
            .padding(4.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // 날짜 숫자와 날씨
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .then(
                        if (isSelected) {
                            Modifier
                                .background(
                                    color = Color(0xFF2196F3),
                                    shape = CircleShape
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        } else {
                            Modifier.padding(2.dp)
                        }
                    )
            )
            
            // 날씨 아이콘 (현재 주의 7일만 표시)
            val today = LocalDate.now()
            val startOfWeek = today.minusDays(today.dayOfWeek.value % 7L)
            val endOfWeek = startOfWeek.plusDays(6)
            
            if (date >= startOfWeek && date <= endOfWeek) {
                val weatherEmoji = remember(date) {
                    val weatherList = listOf("☀️", "🌤️", "☁️", "🌧️", "⛅")
                    weatherList[date.dayOfMonth % weatherList.size]
                }
                Text(
                    text = weatherEmoji,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    modifier = if (!isCurrentMonth) {
                        Modifier.alpha(0.3f) // 다른 달은 연하게
                    } else {
                        Modifier
                    }
                )
            }
        }
        
        // 마케팅 기회 표시
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp), // 2줄 텍스트를 위해 높이 증가 70 -> 75
            verticalArrangement = Arrangement.Top
        ) {
            dailyOpportunities?.let { daily ->
                // 처음 2개만 표시
                daily.opportunities.take(2).forEach { opportunity ->
                    // 리마인더인지 확인 (제목이 [리마인더]로 시작)
                    if (opportunity.title.startsWith("[리마인더]")) {
                        // 리마인더: 세로 바 + 텍스트 형태
                        val barColor = when (opportunity.priority) {
                            Priority.HIGH -> Color(0xFFFF4444) // 마케팅 -> 빨간색
                            Priority.MEDIUM -> Color(0xFF2196F3) // 일정 -> 파란색
                            else -> Color(0xFF2196F3) // 기본값 파란색
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(vertical = 1.dp)
                                .alpha(if (!isCurrentMonth) 0.3f else 1.0f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 왼쪽 세로 바
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(16.dp)
                                    .background(
                                        color = barColor,
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            // 텍스트
                            Text(
                                text = opportunity.title.removePrefix("[리마인더] "),
                                color = MarketingColors.TextPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        // 제안/기회: 기존 전체 배경색 스타일 유지
                        val backgroundColor = when (opportunity.priority) {
                            Priority.MEDIUM -> Color(0xFFFFC107).copy(alpha = 0.4f) // 제안 있음 -> 노란색
                            Priority.LOW -> Color(0xFF9E9E9E).copy(alpha = 0.4f) // 제안 없음 -> 회색
                            else -> Color(0xFF9E9E9E).copy(alpha = 0.4f) // 기본값 회색
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(vertical = 1.dp) 
                                .background(
                                    color = backgroundColor,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 3.dp)
                                .alpha(if (!isCurrentMonth) 0.3f else 1.0f),
                            contentAlignment = Alignment.TopStart
                        ) {
                            Text(
                                text = opportunity.title,
                                color = MarketingColors.TextPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                maxLines = 2, // 최대 2줄까지 표시
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 12.sp, // 2줄 표시를 위해 적절한 줄 간격
                                textAlign = TextAlign.Start, // 왼쪽 정렬
                                modifier = Modifier.fillMaxWidth() // 박스가 동적 높이이므로 fillMaxHeight 제거
                            )
                        }
                    }
                }
                
                // 더 많은 기회가 있을 때 표시
                if (daily.totalCount > 2) {
                    Text(
                        text = "+${daily.totalCount - 2}",
                        color = MarketingColors.TextSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp, // 9 -> 8로 감소 (카운터는 작게)
                        modifier = Modifier
                            .padding(top = 1.dp, start = 4.dp) // top padding 2 -> 1로 감소
                            .alpha(if (!isCurrentMonth) 0.3f else 1.0f)
                    )
                }
            }
        }
    }
}