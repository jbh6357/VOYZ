package com.voyz.presentation.component.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.voyz.datas.datastore.UserPreferencesManager
import com.voyz.datas.model.MarketingOpportunity
import com.voyz.presentation.component.calendar.components.MarketingCalendarGrid
import com.voyz.ui.theme.MarketingColors
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarComponent(
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel? = null,
    onDateClick: (LocalDate, List<MarketingOpportunity>) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    val userId = userPreferencesManager.userId.collectAsState(initial = null)
    val isLoggedIn = userPreferencesManager.isLoggedIn.collectAsState(initial = false)
    
    // 전달받은 ViewModel이 있으면 사용, 없으면 새로 생성
    val calendarViewModel = viewModel ?: remember(context) { CalendarViewModel(context) }
    
    val currentMonth = calendarViewModel.currentMonth
    val selectedDate = calendarViewModel.selectedDate
    val dailyOpportunities = calendarViewModel.dailyOpportunities
    val isLoading = calendarViewModel.isLoading
    var totalDrag by remember { mutableStateOf(0f) }
    
    // 사용자 ID가 있으면 캘린더 데이터 로딩
    LaunchedEffect(userId.value, isLoggedIn.value, currentMonth) {
        Log.d("CalendarComponent", "LaunchedEffect triggered")
        Log.d("CalendarComponent", "- userId: ${userId.value}")
        Log.d("CalendarComponent", "- isLoggedIn: ${isLoggedIn.value}")
        Log.d("CalendarComponent", "- currentMonth: $currentMonth")
        Log.d("CalendarComponent", "- Current system date: ${java.time.LocalDate.now()}")
        
        if (isLoggedIn.value && userId.value != null) {
            val id = userId.value!!
            Log.d("CalendarComponent", "User is logged in. Loading calendar data for user: $id")
            calendarViewModel.loadCalendarData(id)
        } else {
            Log.w("CalendarComponent", "User not logged in or userId is null. Skipping data load.")
            Log.w("CalendarComponent", "- isLoggedIn: ${isLoggedIn.value}, userId: ${userId.value}")
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color.White)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { 
                        totalDrag = 0f
                    },
                    onDragEnd = { 
                        if (abs(totalDrag) > 100) {
                            userId.value?.let { id ->
                            if (totalDrag > 0) {
                                                            calendarViewModel.goToPreviousMonth(id)
                            } else {
                        calendarViewModel.goToNextMonth(id)
                                }
                            }
                        }
                        totalDrag = 0f
                    }
                ) { _, dragAmount ->
                    totalDrag += dragAmount
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
                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -slideDirection * fullWidth },
                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                )
            },
            label = "calendar_month_transition"
        ) { animatedCurrentMonth ->
            MarketingCalendarGrid(
                yearMonth = animatedCurrentMonth,
                selectedDate = selectedDate,
                marketingOpportunities = dailyOpportunities,
                calendarHeight = 420.dp,
                onDateClick = { date ->
                    // 마케팅 기회가 있는 날짜 클릭 시 처리
                    val dailyOpps = dailyOpportunities[date]
                    if (dailyOpps != null && dailyOpps.opportunities.isNotEmpty()) {
                        onDateClick(date, dailyOpps.opportunities)
                    }
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
            animationSpec = tween(200, easing = FastOutSlowInEasing),
            label = "month_text_transition"
        ) { animatedMonth: YearMonth ->
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
    
    Column {
        // 요일 헤더만 표시
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
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
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            }
        }
        
        // 구분선
        Divider(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            color = MarketingColors.TextTertiary.copy(alpha = 0.3f),
            thickness = 0.5.dp
        )
    }
}

// MarketingCalendarGrid는 이제 별도 파일로 분리됨

// MarketingCalendarDayCell과 CalendarDate는 이제 별도 파일로 분리됨

@Preview(showBackground = true, widthDp = 400, heightDp = 700)
@Composable
fun CalendarComponentPreview() {
    CalendarComponent()
}