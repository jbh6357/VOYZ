package com.voyz.presentation.component.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
                    strokeWidth = 0.5.dp.toPx()
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
            dailyOpportunities?.opportunities?.take(2)?.forEach { opp ->
                if (opp.title.startsWith("[리마인더]")) {
                    val barColor = when (opp.priority) {
                        Priority.HIGH -> Color(0xFFFF4444)
                        Priority.MEDIUM -> Color(0xFF2196F3)
                        else -> Color(0xFF2196F3)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp)
                            .alpha(if (!isCurrentMonth) 0.3f else 1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(16.dp)
                                .background(barColor, RoundedCornerShape(2.dp))
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = opp.title.removePrefix("[리마인더] "),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    val bg = when (opp.priority) {
                        Priority.MEDIUM -> Color(0xFFFFC107).copy(alpha = 0.4f)
                        Priority.LOW -> Color(0xFF9E9E9E).copy(alpha = 0.4f)
                        else -> Color(0xFF9E9E9E).copy(alpha = 0.4f)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp)
                            .background(bg, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 3.dp)
                            .alpha(if (!isCurrentMonth) 0.3f else 1f)
                    ) {
                        Text(
                            text = opp.title,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            dailyOpportunities?.let {
                val extra = it.totalCount - 2
                if (extra > 0) {
                    Text(text = "+$extra", fontSize = 8.sp)
                }
            }
        }
    }
}
