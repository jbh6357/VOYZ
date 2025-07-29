package com.voyz.presentation.component.reminder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.YearMonth


class ReminderCalendarViewModel : ViewModel() {

    var selectedDate by mutableStateOf(LocalDate.now())

    var currentMonth by mutableStateOf(YearMonth.now())
        private set

    var events by mutableStateOf<Map<LocalDate, List<ReminderCalendarEvent>>>(
        // 테스트용 이벤트 데이터
        mapOf(
            LocalDate.now().withDayOfMonth(5) to listOf(
                ReminderCalendarEvent("1", "회의"),
                ReminderCalendarEvent("2", "회의1"),
                ReminderCalendarEvent("3", "회의2"),
                ReminderCalendarEvent("4", "회의3")
            ),
            LocalDate.now().withDayOfMonth(7) to listOf(ReminderCalendarEvent("5", "약속")),
            LocalDate.now().withDayOfMonth(14) to listOf(ReminderCalendarEvent("6", "생일")),
            LocalDate.now().withDayOfMonth(21) to listOf(ReminderCalendarEvent("7", "병원")),
            LocalDate.now().withDayOfMonth(26) to listOf(ReminderCalendarEvent("8", "여행"))
        )
    )
        private set

    fun selectDate(date: LocalDate) {
        selectedDate = date
    }

    fun clearSelection() {
        selectedDate = null
    }

    fun goToNextMonth() {
        currentMonth = currentMonth.plusMonths(1)
    }

    fun goToPreviousMonth() {
        currentMonth = currentMonth.minusMonths(1)
    }

    fun goToNextMonthWithDirection(): Pair<YearMonth, Boolean> {
        val newMonth = currentMonth.plusMonths(1)
        currentMonth = newMonth
        return newMonth to true // true = 다음 달로 이동
    }

    fun goToPreviousMonthWithDirection(): Pair<YearMonth, Boolean> {
        val newMonth = currentMonth.minusMonths(1)
        currentMonth = newMonth
        return newMonth to false // false = 이전 달로 이동
    }

    fun goToMonth(yearMonth: YearMonth) {
        currentMonth = yearMonth
    }

    fun addEvent(date: LocalDate, event: ReminderCalendarEvent) {
        val currentEvents = events[date] ?: emptyList()
        events = events.toMutableMap().apply {
            put(date, currentEvents + event)
        }
    }

    fun getEventsForDate(date: LocalDate): List<ReminderCalendarEvent> {
        return events[date] ?: emptyList()
    }

    fun goToToday() {
        val today = LocalDate.now()
        selectedDate = today
        currentMonth = YearMonth.from(today)
    }

    fun updateEventCheckStatus(event: ReminderCalendarEvent, isChecked: Boolean) {
        val date = events.entries.find { it.value.contains(event) }?.key ?: return
        val updatedEvents = events[date]?.map {
            if (it.id == event.id) it.copy(isChecked = isChecked) else it
        } ?: return

        events = events.toMutableMap().apply {
            put(date, updatedEvents)
        }
    }
}
data class ReminderCalendarEvent(
    val id: String,
    val title: String,
    val description: String? = null,
    val time: String? = null,
    var isChecked: Boolean = false
)