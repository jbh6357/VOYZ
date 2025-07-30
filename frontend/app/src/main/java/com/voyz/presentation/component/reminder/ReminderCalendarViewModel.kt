package com.voyz.presentation.component.reminder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.YearMonth
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class ReminderCalendarViewModel : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()


    private val _currentMonth = mutableStateOf(YearMonth.now())
    val currentMonth: State<YearMonth> get() = _currentMonth


    var events by mutableStateOf<Map<LocalDate, List<ReminderCalendarEvent>>>(
        // 테스트용 이벤트 데이터
        mapOf(
            LocalDate.now().withDayOfMonth(5) to listOf(
                ReminderCalendarEvent("1", "회의"),
                ReminderCalendarEvent("2", "회의1"),
                ReminderCalendarEvent("3", "회의2"),
                ReminderCalendarEvent("4", "회의3"),
                ReminderCalendarEvent("5", "회의4")
            ),
            LocalDate.now().withDayOfMonth(7) to listOf(ReminderCalendarEvent("6", "약속")),
            LocalDate.now().withDayOfMonth(14) to listOf(ReminderCalendarEvent("7", "생일")),
            LocalDate.now().withDayOfMonth(21) to listOf(ReminderCalendarEvent("8", "병원")),
            LocalDate.now().withDayOfMonth(26) to listOf(ReminderCalendarEvent("9", "여행"))
        )
    )
        private set

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun clearSelection() {
        _selectedDate.value = LocalDate.now() // 또는 고정값
    }

    fun goToNextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    fun goToPreviousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun goToNextMonthWithDirection(): Pair<YearMonth, Boolean> {
        val newMonth = currentMonth.value.plusMonths(1)
        _currentMonth.value = newMonth
        return newMonth to true // true = 다음 달로 이동
    }

    fun goToPreviousMonthWithDirection(): Pair<YearMonth, Boolean> {
        val newMonth = currentMonth.value.minusMonths(1)
        _currentMonth.value = newMonth
        return newMonth to false // false = 이전 달로 이동
    }

    fun goToMonth(yearMonth: YearMonth) {
        _currentMonth.value = yearMonth
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
        _selectedDate.value = today
        _currentMonth.value = YearMonth.from(today)
    }

    private val _reminderEvents = mutableStateOf<Map<LocalDate, List<ReminderCalendarEvent>>>(emptyMap())
    val reminderEvents: State<Map<LocalDate, List<ReminderCalendarEvent>>> = _reminderEvents

    fun updateEventCheckStatus(eventId: String, isChecked: Boolean) {
        events = events.mapValues { (_, eventList) ->
            eventList.map { event ->
                if (event.id == eventId) event.copy(isChecked = isChecked) else event
            }
        }
    }

}
data class ReminderCalendarEvent(
    val id: String,
    val title: String,
    val description: String? = null,
    val time: String? = null,
    var isChecked: Boolean = false,
    val originalIndex: Boolean = false
)