package com.voyz.presentation.component.calendar

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
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
import com.voyz.presentation.component.calendar.components.getMarketingDatesOfMonth


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


    // 데이터 로딩
    LaunchedEffect(userId, isLoggedIn, currentMonth) {
        if (isLoggedIn && userId != null) {
            calendarViewModel.loadCalendarData(userId!!)

            if (calendarViewModel.selectedDate == null) {
                calendarViewModel.selectDate(LocalDate.now())
            }
        }
    }
    var totalDrag by remember { mutableStateOf(0f) }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(userId) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        totalDrag = 0f // 드래그 시작 시 초기화
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount
                    },
                    onDragEnd = {
                        if (userId != null) {
                            when {
                                totalDrag > 50f -> calendarViewModel.goToPreviousMonth(userId!!)
                                totalDrag < -50f -> calendarViewModel.goToNextMonth(userId!!)
                            }
                        }
                        totalDrag = 0f // 리셋
                    }
                )
            }
    ) {
        CalendarHeader(currentMonth)
        DaysOfWeekHeader()

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White)
        ) {

            AnimatedContent(
                targetState = currentMonth,
                transitionSpec = {
                    val isNext = targetState > initialState
                    val dir = if (isNext) 1 else -1
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth * dir },
                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth * dir },
                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                    )
                }
            ) { month ->
                MarketingCalendarGrid(
                    yearMonth             = month,
                    selectedDate          = selectedDate,
                    marketingOpportunities = dailyOpps,
                    calendarHeight = maxHeight,     // 전체 높이 넘겨 줌
                    isWeekly = false,   // ← 주 단위 여부
                    onDateClick           = { date ->
                        // 날짜 클릭 로직 (달 이동 + 모달)
                        val clicked = YearMonth.from(date)
                        if (clicked != month && userId != null) {
                            calendarViewModel.goToMonth(clicked, userId!!)
                        }
                        dailyOpps[date]
                            ?.opportunities
                            ?.takeIf { it.isNotEmpty() }
                            ?.let { onDateClick(date, it) }
                        calendarViewModel.selectDate(date)
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
