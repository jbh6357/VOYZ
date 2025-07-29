package com.voyz.presentation.component.calendar.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.voyz.datas.model.DailyMarketingOpportunities
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.*

@Composable
fun MarketingCalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    marketingOpportunities: Map<LocalDate, DailyMarketingOpportunities>,
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

data class CalendarDate(
    val date: LocalDate,
    val isCurrentMonth: Boolean
)