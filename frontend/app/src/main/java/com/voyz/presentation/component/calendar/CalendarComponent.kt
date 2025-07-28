package com.voyz.presentation.component.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voyz.data.model.MarketingOpportunity
import com.voyz.data.model.Priority
import com.voyz.data.repository.MarketingOpportunityRepository
import com.voyz.ui.theme.MarketingColors
import com.voyz.ui.theme.getMarketingCategoryColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
fun CalendarComponent(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = viewModel(),
    onDayClick: (LocalDate, List<MarketingOpportunity>) -> Unit = { _, _ -> }
) {
    val currentMonth = viewModel.currentMonth
    val selectedDate = viewModel.selectedDate
    var totalDrag by remember { mutableStateOf(0f) }
    
    // 마케팅 기회 데이터 가져오기
    val marketingOpportunities = remember {
        MarketingOpportunityRepository.getDailyOpportunities()
            .associateBy { it.date }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.White)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { 
                        totalDrag = 0f
                    },
                    onDragEnd = { 
                        if (abs(totalDrag) > 100) {
                            if (totalDrag > 0) {
                                viewModel.goToPreviousMonth()
                            } else {
                                viewModel.goToNextMonth()
                            }
                        }
                        totalDrag = 0f
                    }
                ) { _, dragAmount ->
                    totalDrag += dragAmount
                }
            },
        verticalArrangement = Arrangement.Top
    ) {
        // 캘린더 헤더
        CalendarHeader(
            currentMonth = currentMonth
        )
        
        // 요일 헤더
        DaysOfWeekHeader()
        
        // 애니메이션이 적용된 캘린더 그리드
        AnimatedContent(
            targetState = currentMonth,
            transitionSpec = {
                val isNext = targetState > initialState
                val slideDirection = if (isNext) 1 else -1
                
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> slideDirection * fullWidth },
                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -slideDirection * fullWidth },
                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                )
            },
            label = "calendar_month_transition"
        ) { animatedCurrentMonth ->
            MarketingCalendarGrid(
                yearMonth = animatedCurrentMonth,
                selectedDate = selectedDate,
                marketingOpportunities = marketingOpportunities,
                onDateClick = { date ->
                    // 다른 달 날짜도 클릭 가능하게 변경
                    val opportunities = marketingOpportunities[date]?.opportunities ?: emptyList()
                    if (opportunities.isNotEmpty()) {
                        onDayClick(date, opportunities)
                    }
                    if (date == selectedDate) {
                        viewModel.clearSelection()
                    } else {
                        viewModel.selectDate(date)
                    }
                }
            )
        }
    }
}

@Composable
private fun CalendarHeader(
    currentMonth: YearMonth
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 20.dp, start = 40.dp, end = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        // 애니메이션이 적용된 월 텍스트
        Crossfade(
            targetState = currentMonth,
            animationSpec = tween(200, easing = FastOutSlowInEasing),
            label = "month_text_transition"
        ) { animatedMonth ->
            Text(
                text = "${animatedMonth.monthValue}월",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DaysOfWeekHeader() {
    val daysOfWeek = remember {
        listOf("일", "월", "화", "수", "목", "금", "토")
    }
    
    Column {
        // 요일 헤더만 표시
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            daysOfWeek.forEachIndexed { index, dayName ->
                val textColor = when (index) {
                    0 -> MarketingColors.HighPriority // 일요일
                    6 -> MarketingColors.TextSecondary // 토요일  
                    else -> MarketingColors.TextPrimary // 평일
                }
                
                Text(
                    text = dayName,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            }
        }
        
        // 구분선
        Divider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            color = MarketingColors.TextTertiary.copy(alpha = 0.3f),
            thickness = 0.5.dp
        )
    }
}

@Composable
private fun MarketingCalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    marketingOpportunities: Map<LocalDate, com.voyz.data.model.DailyMarketingOpportunities>,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    val daysFromFirstDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal - firstDayOfWeek.ordinal
    val adjustedDaysFromFirstDayOfWeek = if (daysFromFirstDayOfWeek < 0) daysFromFirstDayOfWeek + 7 else daysFromFirstDayOfWeek
    
    val days = remember(yearMonth) {
        val monthLength = yearMonth.lengthOfMonth()
        val prevMonth = yearMonth.minusMonths(1)
        val prevMonthLength = prevMonth.lengthOfMonth()
        
        // 이전 달 마지막 날들
        val prevMonthDays = (prevMonthLength - adjustedDaysFromFirstDayOfWeek + 1..prevMonthLength).map { day ->
            CalendarDate(prevMonth.atDay(day), false)
        }
        
        // 현재 달 날들
        val currentMonthDays = (1..monthLength).map { day ->
            CalendarDate(yearMonth.atDay(day), true)
        }
        
        // 다음 달 첫 날들
        val nextMonth = yearMonth.plusMonths(1)
        val totalCells = 35 // 5주 * 7일로 제한
        val remainingCells = totalCells - prevMonthDays.size - currentMonthDays.size
        val nextMonthDays = if (remainingCells > 0) {
            (1..remainingCells).map { day ->
                CalendarDate(nextMonth.atDay(day), false)
            }
        } else {
            emptyList()
        }
        
        prevMonthDays + currentMonthDays + nextMonthDays
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        items(days) { calendarDate ->
            MarketingCalendarDayCell(
                date = calendarDate.date,
                isCurrentMonth = calendarDate.isCurrentMonth,
                isSelected = selectedDate == calendarDate.date,
                dailyOpportunities = marketingOpportunities[calendarDate.date],
                onClick = { onDateClick(calendarDate.date) } // isCurrentMonth 조건 제거
            )
        }
    }
}

@Composable
private fun MarketingCalendarDayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    dailyOpportunities: com.voyz.data.model.DailyMarketingOpportunities?,
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
                                    MarketingColors.Selected,
                                    CircleShape
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
        
        // 마케팅 기회 영역 - 확장된 크기
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp), // 기회 영역 높이 대폭 증가
            verticalArrangement = Arrangement.Top
        ) {
            dailyOpportunities?.let { daily ->                
                // 최대 2개 기회만 표시
                daily.opportunities.take(2).forEach { opportunity ->
                    val backgroundColor = if (opportunity.priority == com.voyz.data.model.Priority.HIGH) {
                        MarketingColors.HighPriority.copy(alpha = 0.3f) // 높은 우선순위는 빨간색 배경
                    } else {
                        getMarketingCategoryColors(opportunity.category).second
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp) // 높이 증가
                            .padding(vertical = 2.dp)
                            .background(
                                color = backgroundColor,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 3.dp)
                            .alpha(if (!isCurrentMonth) 0.3f else 1.0f), // 다른 달은 연하게
                        contentAlignment = Alignment.CenterStart // 가운데 정렬
                    ) {
                        Text(
                            text = opportunity.title,
                            color = MarketingColors.TextPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            minLines = 2, // 최소 2줄 표시
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            lineHeight = 10.sp, // 줄 간격 조정
                            modifier = Modifier.fillMaxWidth() // 전체 너비 사용
                        )
                    }
                }
                
                // 더 많은 기회가 있을 때 표시
                if (daily.totalCount > 2) {
                    Text(
                        text = "+${daily.totalCount - 2}",
                        color = MarketingColors.TextSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        modifier = Modifier
                            .padding(top = 2.dp, start = 4.dp)
                            .alpha(if (!isCurrentMonth) 0.3f else 1.0f) // 다른 달은 연하게
                    )
                }
            }
        }
    }
}

private data class CalendarDate(
    val date: LocalDate,
    val isCurrentMonth: Boolean
)

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
fun CalendarComponentPreview() {
    CalendarComponent()
}