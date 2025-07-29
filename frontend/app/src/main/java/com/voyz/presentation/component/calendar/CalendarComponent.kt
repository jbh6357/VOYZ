package com.voyz.presentation.component.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.voyz.datas.datastore.UserPreferencesManager
import com.voyz.datas.model.MarketingOpportunity
import com.voyz.datas.model.Priority
import com.voyz.ui.theme.MarketingColors
import com.voyz.ui.theme.getMarketingCategoryColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
fun CalendarComponent(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel? = null,
    onDateClick: (LocalDate, List<MarketingOpportunity>) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    val userId = userPreferencesManager.userId.collectAsState(initial = null)
    val isLoggedIn = userPreferencesManager.isLoggedIn.collectAsState(initial = false)
    
    // 전달받은 ViewModel이 있으면 사용, 없으면 새로 생성
    val calendarViewModel = viewModel ?: remember(context) { CalendarViewModel(context) }
    
    val currentMonth = calendarViewModel.currentMonth
    val selectedDate = calendarViewModel.selectedDate
    val dailyOpportunities = calendarViewModel.dailyOpportunities
    val isLoading = calendarViewModel.isLoading
    var totalDrag by remember { mutableStateOf(0f) }
    
    // 사용자 ID가 있으면 캘린더 데이터 로딩
    LaunchedEffect(userId.value, isLoggedIn.value, currentMonth) {
        Log.d("CalendarComponent", "LaunchedEffect triggered")
        Log.d("CalendarComponent", "- userId: ${userId.value}")
        Log.d("CalendarComponent", "- isLoggedIn: ${isLoggedIn.value}")
        Log.d("CalendarComponent", "- currentMonth: $currentMonth")
        Log.d("CalendarComponent", "- Current system date: ${java.time.LocalDate.now()}")
        
        if (isLoggedIn.value && userId.value != null) {
            val id = userId.value!!
            Log.d("CalendarComponent", "User is logged in. Loading calendar data for user: $id")
            calendarViewModel.loadCalendarData(id)
        } else {
            Log.w("CalendarComponent", "User not logged in or userId is null. Skipping data load.")
            Log.w("CalendarComponent", "- isLoggedIn: ${isLoggedIn.value}, userId: ${userId.value}")
        }
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
                            userId.value?.let { id ->
                            if (totalDrag > 0) {
                                                            calendarViewModel.goToPreviousMonth(id)
                            } else {
                        calendarViewModel.goToNextMonth(id)
                                }
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
                marketingOpportunities = dailyOpportunities,
                onDateClick = { date ->
                    // 마케팅 기회가 있는 날짜 클릭 시 처리
                    val dailyOpps = dailyOpportunities[date]
                    if (dailyOpps != null && dailyOpps.opportunities.isNotEmpty()) {
                        onDateClick(date, dailyOpps.opportunities)
                    }
                    if (date == selectedDate) {
                        calendarViewModel.clearSelection()
                    } else {
                        calendarViewModel.selectDate(date)
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
    marketingOpportunities: Map<LocalDate, com.voyz.datas.model.DailyMarketingOpportunities>,
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
        val prevMonthDays = (prevMonthLength - adjustedDaysFromFirstDayOfWeek + 1..prevMonthLength).map { day: Int ->
            CalendarDate(prevMonth.atDay(day), false)
        }
        
        // 현재 달 날들
        val currentMonthDays = (1..monthLength).map { day: Int ->
            CalendarDate(yearMonth.atDay(day), true)
        }
        
        // 다음 달 첫 날들
        val nextMonth = yearMonth.plusMonths(1)
        val totalCells = 35 // 5주 * 7일로 제한
        val remainingCells = totalCells - prevMonthDays.size - currentMonthDays.size
        val nextMonthDays: List<CalendarDate> = if (remainingCells > 0) {
            (1..remainingCells).map { day: Int ->
                CalendarDate(nextMonth.atDay(day), false)
            }
        } else {
            emptyList<CalendarDate>()
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
                onClick = { onDateClick(calendarDate.date) }
            )
        }
    }
}

@Composable
private fun MarketingCalendarDayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    dailyOpportunities: com.voyz.datas.model.DailyMarketingOpportunities?,
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
        
        // 마케팅 기회 영역 - 기존 디자인 복제
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp), // 2줄 텍스트를 위해 높이 증가 70 -> 75
            verticalArrangement = Arrangement.Top
        ) {
            dailyOpportunities?.let { daily ->                
                // 최대 2개 기회만 표시
                daily.opportunities.take(2).forEach { opportunity ->
                    val isReminder = opportunity.title.startsWith("[리마인더]")
                    
                    if (isReminder) {
                        // 리마인더: 왼쪽 세로 바 스타일
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
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
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
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
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

data class CalendarDate(
    val date: LocalDate,
    val isCurrentMonth: Boolean
)

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
fun CalendarComponentPreview() {
    CalendarComponent()
}