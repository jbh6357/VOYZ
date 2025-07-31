package com.voyz.presentation.component.calendar

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.voyz.datas.datastore.UserPreferencesManager
import com.voyz.datas.model.MarketingOpportunity
import com.voyz.presentation.component.calendar.components.MarketingCalendarGrid
import com.voyz.presentation.component.calendar.CalendarViewModel
import com.voyz.ui.theme.MarketingColors
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs

@Composable
fun CalendarComponent(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel? = null,
    onDateClick: (LocalDate, List<MarketingOpportunity>) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferencesManager(context) }
    val userId by userPrefs.userId.collectAsState(initial = null)
    val isLoggedIn by userPrefs.isLoggedIn.collectAsState(initial = false)

    // 뷰모델
    val calendarViewModel = viewModel ?: remember(context) { CalendarViewModel(context) }
    val currentMonth = calendarViewModel.currentMonth
    val selectedDate = calendarViewModel.selectedDate
    val dailyOpps = calendarViewModel.dailyOpportunities

    // 드래그 트래킹
    var totalDrag by remember { mutableStateOf(0f) }

    // 데이터 로딩
    LaunchedEffect(userId, isLoggedIn, currentMonth) {
        Log.d("CalendarComponent", "Loading calendar data - user=$userId, loggedIn=$isLoggedIn, month=$currentMonth")
        if (isLoggedIn && userId != null) {
            calendarViewModel.loadCalendarData(userId!!)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onDragEnd = {
                        if (abs(totalDrag) > 100 && userId != null) {
                            if (totalDrag > 0) calendarViewModel.goToPreviousMonth(userId!!)
                            else calendarViewModel.goToNextMonth(userId!!)
                        }
                        totalDrag = 0f
                    }
                ) { _, delta -> totalDrag += delta }
            }
    ) {
        // ─── 헤더 ───
        CalendarHeader(currentMonth)
        DaysOfWeekHeader()

        // ─── 달력 그리기 영역 ───
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val calendarHeight = maxHeight

            AnimatedContent(
                targetState = currentMonth,
                transitionSpec = {
                    val isNext = targetState > initialState
                    val dir = if (isNext) 1 else -1
                    slideInHorizontally(
                        initialOffsetX = { it * dir },
                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { -it * dir },
                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                    )
                }
            ) { animatedMonth: YearMonth ->
                MarketingCalendarGrid(
                    yearMonth            = animatedMonth,
                    selectedDate         = selectedDate,
                    marketingOpportunities = dailyOpps,
                    onDateClick          = { date ->
                        dailyOpps[date]?.opportunities
                            ?.takeIf { it.isNotEmpty() }
                            ?.let { onDateClick(date, it) }
                        if (date == selectedDate) calendarViewModel.clearSelection()
                        else calendarViewModel.selectDate(date)
                    },
                    calendarHeight       = calendarHeight  // ← 여기서 넘긴 높이로만 셀 높이 계산
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
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(targetState = currentMonth, animationSpec = tween(200, easing = FastOutSlowInEasing)) {
            Text(
                text = "${it.monthValue}월",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DaysOfWeekHeader() {
    val days = listOf("일","월","화","수","목","금","토")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        days.forEachIndexed { i, d ->
            val color = when(i) {
                0 -> MarketingColors.HighPriority
                6 -> MarketingColors.TextSecondary
                else -> MarketingColors.TextPrimary
            }
            Text(
                text = d,
                color = color,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
fun CalendarComponentPreview() {
    CalendarComponent()
}
