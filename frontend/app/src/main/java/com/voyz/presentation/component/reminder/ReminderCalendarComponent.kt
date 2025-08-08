package com.voyz.presentation.component.reminder

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

@Composable
fun ReminderCalendarComponent(
    modifier: Modifier = Modifier,
    viewModel: ReminderCalendarViewModel = viewModel(),
    isWeekly: Boolean = false,
    calendarHeight: Dp,
    onDateSelected: (LocalDate) -> Unit
) {
    val currentMonth by viewModel.currentMonth
    val selectedDate by viewModel.selectedDate.collectAsState()
    var totalDrag by remember { mutableStateOf(0f) }
    val animationKey: Any? = if (isWeekly) {
        // 주간 모드에서는 해당 주의 시작일(日요일)을 기준으로 애니메이션 결정
        selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    } else {
        currentMonth
    }
    val monthData by remember(currentMonth, selectedDate, isWeekly) {
        derivedStateOf {
            if (isWeekly) ReminderMonthData(getDatesOfWeek(selectedDate), 1)
            else getDatesOfMonth(currentMonth)
        }
    }


    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(calendarHeight)
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onDragEnd = {
                        if (kotlin.math.abs(totalDrag) > 100) {
                            if (isWeekly) {
                                val currentDate = viewModel.selectedDate.value
                                val newDate = if (totalDrag > 0) currentDate.minusWeeks(1) else currentDate.plusWeeks(1)
                                viewModel.selectDate(newDate)
                                viewModel.goToMonth(YearMonth.from(newDate))
                            } else {
                                if (totalDrag > 0) viewModel.goToPreviousMonth() else viewModel.goToNextMonth()
                            }
                        }
                        totalDrag = 0f
                    }
                ) { _, dragAmount -> totalDrag += dragAmount }
            },
        verticalArrangement = Arrangement.Top
    ) {
        CalendarHeader(currentMonth = currentMonth)


        Box(modifier = Modifier) {
            AnimatedContent(
                targetState = animationKey,
                transitionSpec = {
                    val isNext = (animationKey as? Comparable<Any>)
                        ?.let { newState ->
                            val oldState = initialState as? Comparable<Any>
                            if (oldState != null) newState > oldState else true
                        } ?: true
                    val direction = if (isNext) 1 else -1
                    slideInHorizontally(
                        initialOffsetX = { it * direction },
                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { -it * direction },
                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                    )
                },
                label = "calendar_transition",
                modifier = Modifier.fillMaxSize()
            ) {
                SimpleCalendarGrid(
                    dates = monthData.dates,
                    numberOfWeeks = monthData.numberOfWeeks,
                    selectedDate = selectedDate,
                    events = viewModel.events,
                    calendarHeight = calendarHeight,
                    onDateClick = { date ->
                        val clickedMonth = YearMonth.from(date)
                        if (clickedMonth != currentMonth) viewModel.goToMonth(clickedMonth)
                        viewModel.selectDate(date)
                        onDateSelected(date)
                    },
                    isWeekly = isWeekly
                )
            }
        }
    }
}

@Composable
private fun CalendarHeader(currentMonth: YearMonth) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = currentMonth,
            animationSpec = tween(200, easing = FastOutSlowInEasing),
            label = "month_text_transition"
        ) {
            Text(
                text = "${it.monthValue}월",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DaysOfWeekHeader(
    modifier: Modifier = Modifier

) {
    val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = modifier
            .fillMaxWidth(),
        userScrollEnabled = false
    ) {
        items(daysOfWeek.size) { index ->
            val dayName = daysOfWeek[index]
            val textColor = when (index) {
                0 -> Color(0xFFE57373)
                6 -> Color(0xFF9E9E9E)
                else -> Color(0xFF333333)
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Normal,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun SimpleCalendarGrid(
    dates: List<ReminderCalendarDate>,
    numberOfWeeks: Int,
    selectedDate: LocalDate?,
    events: Map<LocalDate, List<ReminderCalendarEvent>>,
    onDateClick: (LocalDate) -> Unit,
    isWeekly: Boolean,
    calendarHeight: Dp
) {
    val headerHeightDp = 32.dp
    val gridHeightDp = calendarHeight - headerHeightDp
    // 1/3 정도 더 줄이기 위한 축소 비율
    val shrinkFactor = 0.76f
    val rowHeightDp = (gridHeightDp / numberOfWeeks) * shrinkFactor

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(calendarHeight)
    ) {
        DaysOfWeekHeader(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeightDp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeightDp),
            userScrollEnabled = false
        ) {
            items(dates) { calendarDate ->
                CalendarDayCell(
                    date = calendarDate.date,
                    isCurrentMonth = calendarDate.isCurrentMonth,
                    isSelected = selectedDate == calendarDate.date,
                    events = events[calendarDate.date] ?: emptyList(),
                    onClick = { onDateClick(calendarDate.date) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowHeightDp)
                )
            }
        }
    }
}
@Composable
private fun CalendarDayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isSelected: Boolean,
    events: List<ReminderCalendarEvent>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = if (!isCurrentMonth) Color(0xFFBDBDBD) else Color(0xFF333333)
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(2.dp),  // 패딩 축소
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            fontSize = 14.sp,      // 폰트 크기 축소
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .padding(2.dp)
                .then(
                    if (isSelected) Modifier
                        .background(Color(0xFF64B5F6), CircleShape)
                        .padding(2.dp)
                    else Modifier
                )
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(1.dp), // spacing 축소
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 1.dp)            // 상단 여백 축소
        ) {
            val maxDots = 3
            val dotsToShow = events.take(maxDots)
            dotsToShow.forEach { event ->
                if (event.isChecked) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(8.dp) // 크기 축소
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(4.dp)           // 크기 축소
                            .background(
                                color = when (event.id) {
                                    "1" -> Color(0xFFFFE0B2)
                                    "2" -> Color(0xFFF8BBD9)
                                    "3" -> Color(0xFFE8F5E8)
                                    "4" -> Color(0xFFE3F2FD)
                                    else -> Color(0xFFF3E5F5)
                                },
                                shape = CircleShape
                            )
                    )
                }
            }
            val extraCount = events.size - dotsToShow.size
            if (extraCount > 0) {
                Text(
                    text = "+$extraCount",
                    fontSize = 6.sp, // 폰트 크기 축소
                    color = Color(0xFF666666)
                )
            }
        }
    }
}

fun getDatesOfWeek(selectedDate: LocalDate?): List<ReminderCalendarDate> {
    val referenceDate = selectedDate ?: LocalDate.now()
    val startOfWeek = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    return (0..6).map {
        val date = startOfWeek.plusDays(it.toLong())
        ReminderCalendarDate(date, true)
    }
}
data class ReminderMonthData(
    val dates: List<ReminderCalendarDate>,
    val numberOfWeeks: Int
)

fun getDatesOfMonth(yearMonth: YearMonth): ReminderMonthData {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()
    val start = firstDayOfMonth.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    val end = lastDayOfMonth.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))

    var allDates = (0..ChronoUnit.DAYS.between(start, end)).map {
        start.plusDays(it)
    }

    if (allDates.size < 35) {
        val needed = 35 - allDates.size
        allDates = allDates + List(needed) { i -> end.plusDays((i + 1).toLong()) }
    }

    val finalDates = allDates.take(42)
    val numberOfWeeks = (finalDates.size + 6) / 7

    return ReminderMonthData(
        dates = finalDates.map { date -> ReminderCalendarDate(date, date.month == yearMonth.month) },
        numberOfWeeks = numberOfWeeks
    )
}

data class ReminderCalendarDate(val date: LocalDate, val isCurrentMonth: Boolean)

@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 400, heightDp = 700)
fun ReminderCalendarComponentPreview() {
    ReminderCalendarComponent(
        calendarHeight = 400.dp, // 💡 프리뷰용 고정값
        onDateSelected = {}
    )
}
