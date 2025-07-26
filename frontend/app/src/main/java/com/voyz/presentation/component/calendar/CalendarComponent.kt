package com.voyz.presentation.component.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
    viewModel: CalendarViewModel = viewModel()
) {
    val currentMonth = viewModel.currentMonth
    val selectedDate = viewModel.selectedDate
    var dragStarted by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.White)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { 
                        dragStarted = true 
                    },
                    onDragEnd = { 
                        dragStarted = false 
                    }
                ) { _, dragAmount ->
                    if (dragStarted && abs(dragAmount.x) > 100) {
                        if (dragAmount.x > 0) {
                            viewModel.goToPreviousMonth()
                        } else {
                            viewModel.goToNextMonth()
                        }
                        dragStarted = false
                    }
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
                    animationSpec = tween(300)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -slideDirection * fullWidth },
                    animationSpec = tween(300)
                )
            },
            label = "calendar_month_transition"
        ) { animatedCurrentMonth ->
            SimpleCalendarGrid(
                yearMonth = animatedCurrentMonth,
                selectedDate = selectedDate,
                events = viewModel.events,
                onDateClick = { date ->
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
            animationSpec = tween(150),
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
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        daysOfWeek.forEachIndexed { index, dayName ->
            val textColor = when (index) {
                0 -> Color(0xFFE57373) // 일요일 - 자연스러운 빨간색
                6 -> Color(0xFF9E9E9E) // 토요일 - 회색
                else -> Color(0xFF333333) // 평일 - 자연스러운 검은색
            }
            
            Text(
                text = dayName,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal,
                color = textColor
            )
        }
    }
}

@Composable
private fun SimpleCalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    events: Map<LocalDate, List<CalendarEvent>>,
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
        val totalCells = 42 // 6주 * 7일
        val remainingCells = totalCells - prevMonthDays.size - currentMonthDays.size
        val nextMonthDays = (1..remainingCells).map { day ->
            CalendarDate(nextMonth.atDay(day), false)
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
            CalendarDayCell(
                date = calendarDate.date,
                isCurrentMonth = calendarDate.isCurrentMonth,
                isSelected = selectedDate == calendarDate.date,
                events = events[calendarDate.date] ?: emptyList(),
                onClick = { if (calendarDate.isCurrentMonth) onDateClick(calendarDate.date) }
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    events: List<CalendarEvent>,
    onClick: () -> Unit
) {
    val textColor = when {
        !isCurrentMonth -> Color(0xFFBDBDBD)
        else -> Color(0xFF333333)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .drawBehind {
                // 윗줄만 그리기
                drawLine(
                    color = Color(0xFFF0F0F0),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .clickable(enabled = isCurrentMonth) { onClick() }
            .padding(4.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // 날짜 숫자
        Text(
            text = date.dayOfMonth.toString(),
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Normal,
            modifier = Modifier
                .padding(2.dp)
                .then(
                    if (isSelected) {
                        Modifier
                            .background(
                                Color(0xFF64B5F6), // 블루 계열로 변경 (Blue300)
                                CircleShape
                            )
                            .padding(4.dp)
                    } else {
                        Modifier.padding(4.dp)
                    }
                )
        )
        
        // 일정 영역 - 고정 크기
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp), // 일정 영역 높이 고정
            verticalArrangement = Arrangement.Top
        ) {
            // 최대 2개 이벤트만 표시
            events.take(2).forEach { event ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .padding(vertical = 1.dp)
                        .background(
                            color = when (event.id) {
                                "1" -> Color(0xFFFFE0B2) // 연한 주황
                                "2" -> Color(0xFFF8BBD9) // 연한 분홍
                                "3" -> Color(0xFFE8F5E8) // 연한 초록
                                "4" -> Color(0xFFE3F2FD) // 연한 파랑
                                else -> Color(0xFFF3E5F5) // 연한 보라
                            },
                            shape = RoundedCornerShape(2.dp)
                        )
                        .padding(horizontal = 2.dp, vertical = 1.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = event.title,
                        color = Color(0xFF333333),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            
            // 더 많은 이벤트가 있을 때 표시
            if (events.size > 2) {
                Text(
                    text = "+${events.size - 2}",
                    color = Color(0xFF888888),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    modifier = Modifier.padding(top = 2.dp, start = 2.dp)
                )
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