package com.voyz.presentation.component.calendar.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.voyz.datas.model.DailyMarketingOpportunities
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

@Composable
fun MarketingCalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    marketingOpportunities: Map<LocalDate, DailyMarketingOpportunities>,
    onDateClick: (LocalDate) -> Unit,
    calendarHeight: Dp
) {
    // 1) 이번 달의 날짜와 주 수 계산
    val monthData = remember(yearMonth) { getMarketingDatesOfMonth(yearMonth) }
    val days = monthData.dates
    val numberOfWeeks = monthData.numberOfWeeks

    // 2) 셀 높이 = 전체 달력 높이 ÷ 주 수
    val cellHeight = calendarHeight / numberOfWeeks

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .height(calendarHeight)
            .padding(horizontal = 8.dp)
    ) {
        items(days) { dateData ->
            MarketingCalendarDayCell(
                modifier = Modifier
                    .height(cellHeight)
                    .fillMaxWidth(),
                date = dateData.date,
                isCurrentMonth = dateData.isCurrentMonth,
                isSelected = selectedDate == dateData.date,
                dailyOpportunities = marketingOpportunities[dateData.date],
                onClick = { onDateClick(dateData.date) }
            )
        }
    }
}

fun getMarketingDatesOfMonth(yearMonth: YearMonth): MarketingMonthData {
    val firstDay = yearMonth.atDay(1)
    val lastDay = yearMonth.atEndOfMonth()
    val start = firstDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    val end = lastDay.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))

    val totalDays = ChronoUnit.DAYS.between(start, end).toInt() + 1
    val allDates = (0 until totalDays).map { start.plusDays(it.toLong()) }

    val filled = if (allDates.size < 35) {
        val needed = 35 - allDates.size
        allDates + (1..needed).map { end.plusDays(it.toLong()) }
    } else allDates

    val finalDates = filled.take(42)
    val numberOfWeeks = (finalDates.size + 6) / 7

    return MarketingMonthData(
        dates = finalDates.map { date -> MarketingCalendarDate(date, date.month == yearMonth.month) },
        numberOfWeeks = numberOfWeeks
    )
}

data class MarketingCalendarDate(val date: LocalDate, val isCurrentMonth: Boolean)
data class MarketingMonthData(val dates: List<MarketingCalendarDate>, val numberOfWeeks: Int)
