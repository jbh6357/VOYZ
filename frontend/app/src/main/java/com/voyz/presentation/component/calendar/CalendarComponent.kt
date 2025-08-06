package com.voyz.presentation.component.calendar

import android.util.Log
import androidx.compose.animation.Animatedontent
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
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.font.FontWeight
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
    isWeekly: Boolean = false, // feat/des/reminder의 기능 추가
    onDateClick: (LocalDate, List<MarketingOpportunity>) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    val userId by userPreferencesManager.userId.collectAsState(initial = null)
    val isLoggedIn by userPreferencesManager.isLoggedIn.collectAsState(initial = false)

    // 뷰모델
    val calendarViewModel = viewModel ?: remember(context) { CalendarViewModel(context) }
    val currentMonth = calendarViewModel.currentMonth
    val selectedDate = calendarViewModel.selectedDate
    val dailyOpportunities = calendarViewModel.dailyOpportunities
    val isLoading = calendarViewModel.isLoading

    // 데이터 로딩
    LaunchedEffect(userId, isLoggedIn, currentMonth) {
        Log.d("CalendarComponent", "LaunchedEffect triggered")
        Log.d("CalendarComponent", "- userId: $userId")
        Log.d("CalendarComponent", "- isLoggedIn: $isLoggedIn")
        Log.d("CalendarComponent", "- currentMonth: $currentMonth")
        Log.d("CalendarComponent", "- Current system date: ${LocalDate.now()}")
        
        if (isLoggedIn && userId != null) {
            Log.d("CalendarComponent", "User is logged in. Loading calendar data for user: $userId")
            calendarViewModel.loadCalendarData(userId!!)

            // feat/des/reminder의 기능: 첫 로드 시 오늘 날짜 선택
            if (calendarViewModel.selectedDate == null) {
                calendarViewModel.selectDate(LocalDate.now())
            }
        } else {
            Log.w("CalendarComponent", "User not logged in or userId is null. Skipping data load.")
            Log.w("CalendarComponent", "- isLoggedIn: $isLoggedIn, userId: $userId")
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
                            // 드래그 임계값을 설정 가능하도록 (기본값 50f)
                            val threshold = if (isWeekly) 30f else 50f
                            when {
                                totalDrag > threshold -> calendarViewModel.goToPreviousMonth(userId!!)
                                totalDrag < -threshold -> calendarViewModel.goToNextMonth(userId!!)
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
                },
                label = "calendar_month_transition"
            ) { month ->
                MarketingCalendarGrid(
                    yearMonth = month,
                    selectedDate = selectedDate,
                    marketingOpportunities = dailyOpportunities,
                    calendarHeight = maxHeight, // BoxWithConstraints에서 제공하는 동적 높이
                    isWeekly = isWeekly, // feat/des/reminder의 기능
                    onDateClick = { date ->
                        // 날짜 클릭 로직 통합
                        val clicked = YearMonth.from(date)
                        if (clicked != month && userId != null) {
                            calendarViewModel.goToMonth(clicked, userId!!)
                        }
                        
                        val dailyOpps = dailyOpportunities[date]
                        if (dailyOpps != null && dailyOpps.opportunities.isNotEmpty()) {
                            onDateClick(date, dailyOpps.opportunities)
                        }
                        
                        // dev 브랜치의 clearSelection 기능 통합
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
}

@Composable
private fun CalendarHeader(currentMonth: YearMonth) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 20.dp, start = 40.dp, end = 40.dp), // dev 브랜치의 padding
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = currentMonth, 
            animationSpec = tween(200, easing = FastOutSlowInEasing),
            label = "month_text_transition"
        ) { animatedMonth ->
            Text(
                text = "${animatedMonth.monthValue}월",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Normal, // dev 브랜치의 스타일
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp) // dev 브랜치의 padding
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
                    fontWeight = FontWeight.Medium, // dev 브랜치의 스타일
                    color = textColor
                )
            }
        }
        
        // dev 브랜치의 구분선
        Divider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            color = MarketingColors.TextTertiary.copy(alpha = 0.3f),
            thickness = 0.5.dp
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
fun CalendarComponentPreview() {
    CalendarComponent()
}