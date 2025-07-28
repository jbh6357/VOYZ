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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun ReminderCalendarComponent(
    modifier: Modifier = Modifier,
    viewModel: ReminderCalendarViewModel = viewModel(),
    isWeekly: Boolean = false,
    onDateSelected: (LocalDate) -> Unit
) {
    val currentMonth = viewModel.currentMonth
    val selectedDate = viewModel.selectedDate
    var totalDrag by remember { mutableStateOf(0f) }
    val datesToShow = remember(currentMonth, selectedDate, isWeekly) {
        if (isWeekly) getDatesOfWeek(selectedDate) else getDatesOfMonth(currentMonth)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onDragEnd = {
                        if (kotlin.math.abs(totalDrag) > 100) {
                            if (totalDrag > 0) viewModel.goToPreviousMonth() else viewModel.goToNextMonth()
                        }
                        totalDrag = 0f
                    }
                ) { _, dragAmount -> totalDrag += dragAmount }
            },
        verticalArrangement = Arrangement.Top
    ) {
        CalendarHeader(currentMonth = currentMonth)

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = currentMonth,
                transitionSpec = {
                    val isNext = targetState > initialState
                    val direction = if (isNext) 1 else -1
                    slideInHorizontally(
                        initialOffsetX = { it * direction },
                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { -it * direction },
                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                    )
                },
                label = "calendar_month_transition",
                modifier = Modifier.fillMaxSize()
            ) {
                SimpleCalendarGrid(
                    dates = datesToShow,
                    selectedDate = selectedDate,
                    events = viewModel.events,
                    onDateClick = { date ->
                        if (date == selectedDate) viewModel.clearSelection() else {
                            viewModel.selectDate(date)
                            onDateSelected(date)
                        }
                    }
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
private fun DaysOfWeekHeader() {
    val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        daysOfWeek.forEachIndexed { index, dayName ->
            val textColor = when (index) {
                0 -> androidx.compose.ui.graphics.Color(0xFFE57373)
                6 -> androidx.compose.ui.graphics.Color(0xFF9E9E9E)
                else -> androidx.compose.ui.graphics.Color(0xFF333333)
            }
            Text(
                text = dayName,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
                color = textColor
            )
        }
    }
}

@Composable
private fun SimpleCalendarGrid(
    dates: List<ReminderCalendarDate>,
    selectedDate: LocalDate?,
    events: Map<LocalDate, List<CalendarEvent>>,
    onDateClick: (LocalDate) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        DaysOfWeekHeader()
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp),
            userScrollEnabled = false
        ) {
            items(dates) { calendarDate ->
                CalendarDayCell(
                    date = calendarDate.date,
                    isCurrentMonth = calendarDate.isCurrentMonth,
                    isSelected = selectedDate == calendarDate.date,
                    events = events[calendarDate.date] ?: emptyList(),
                    onClick = {
                        if (calendarDate.isCurrentMonth) onDateClick(calendarDate.date)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
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
    events: List<CalendarEvent>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = if (!isCurrentMonth) androidx.compose.ui.graphics.Color(0xFFBDBDBD) else androidx.compose.ui.graphics.Color(0xFF333333)

    Column(
        modifier = modifier
            .clickable(enabled = isCurrentMonth) { onClick() }
            .padding(4.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
            modifier = Modifier
                .padding(2.dp)
                .then(
                    if (isSelected) Modifier
                        .background(androidx.compose.ui.graphics.Color(0xFF64B5F6), CircleShape)
                        .padding(4.dp)
                    else Modifier.padding(4.dp)
                )
        )

        val maxDots = 3
        val dotsToShow = events.take(maxDots)
        val extraCount = events.size - dotsToShow.size

        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 2.dp)
        ) {
            dotsToShow.forEach { event ->
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            color = when (event.id) {
                                "1" -> androidx.compose.ui.graphics.Color(0xFFFFE0B2)
                                "2" -> androidx.compose.ui.graphics.Color(0xFFF8BBD9)
                                "3" -> androidx.compose.ui.graphics.Color(0xFFE8F5E8)
                                "4" -> androidx.compose.ui.graphics.Color(0xFFE3F2FD)
                                else -> androidx.compose.ui.graphics.Color(0xFFF3E5F5)
                            },
                            shape = CircleShape
                        )
                )
            }

            if (extraCount > 0) {
                Text(
                    text = "+$extraCount",
                    fontSize = 8.sp,
                    color = androidx.compose.ui.graphics.Color(0xFF666666)
                )
            }
        }
    }
}

fun getDatesOfWeek(selectedDate: LocalDate?): List<ReminderCalendarDate> {
    val referenceDate = selectedDate ?: LocalDate.now()
    val startOfWeek = referenceDate.with(DayOfWeek.MONDAY)
    return (0..6).map {
        val date = startOfWeek.plusDays(it.toLong())
        ReminderCalendarDate(date, true)
    }
}

fun getDatesOfMonth(yearMonth: YearMonth): List<ReminderCalendarDate> {
    val firstOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstOfMonth.dayOfWeek.value % 7
    val startDate = firstOfMonth.minusDays(firstDayOfWeek.toLong())
    val totalDays = 35
    return (0 until totalDays).map { offset ->
        val date = startDate.plusDays(offset.toLong())
        ReminderCalendarDate(date = date, isCurrentMonth = date.month == yearMonth.month)
    }
}

data class ReminderCalendarDate(val date: LocalDate, val isCurrentMonth: Boolean)

@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 400, heightDp = 700)
fun ReminderCalendarComponentPreview() {
    ReminderCalendarComponent(onDateSelected = {})
}
