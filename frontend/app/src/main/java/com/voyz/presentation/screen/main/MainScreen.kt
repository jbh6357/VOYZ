package com.voyz.presentation.screen.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.voyz.presentation.screen.main.components.MainContent
import com.voyz.presentation.screen.main.components.OverlayManager
import com.voyz.presentation.component.calendar.CalendarViewModel
import java.time.LocalDate

/**
 * 메인 화면 - 리팩토링된 버전
 * 
 * 주요 개선사항:
 * - 상태 관리를 MainScreenState로 분리
 * - UI 컴포넌트를 MainContent와 OverlayManager로 분리
 * - 드래그 제스처 로직을 별도 컴포넌트로 분리
 * - 코드 가독성과 유지보수성 향상
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    navController: NavController,
    onSearchClick: () -> Unit = {},
    onAlarmClick: () -> Unit = {},
    onTodayClick: () -> Unit = {},
    today: LocalDate = LocalDate.now()
) {
    var screenState by remember { mutableStateOf(MainScreenState()) }
    
    // CalendarViewModel을 MainScreen 레벨에서 유지하여 상태 보존
    val context = LocalContext.current
    val sharedCalendarViewModel = remember(context) { CalendarViewModel(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 메인 컨텐츠 (캘린더 + FAB)
        MainContent(
            state = screenState,
            calendarViewModel = sharedCalendarViewModel, // ViewModel 전달
            onSearchClick = onSearchClick,
            onAlarmClick = onAlarmClick,
            onTodayClick = onTodayClick,
            onSidebarOpen = { 
                screenState = screenState.openSidebar() 
            },
            onDragOffsetChange = { offset ->
                screenState = screenState.updateDragOffset(offset)
            },
            onFabToggle = {
                screenState = screenState.toggleFab()
            },
            onDateClick = { date, opportunities ->
                android.util.Log.d("MainScreen", "onDateClick - date: $date")
                android.util.Log.d("MainScreen", "onDateClick - date.year: ${date.year}")
                android.util.Log.d("MainScreen", "onDateClick - date.monthValue: ${date.monthValue}")
                android.util.Log.d("MainScreen", "onDateClick - date.dayOfMonth: ${date.dayOfMonth}")
                screenState = screenState.openOpportunityModal(date, opportunities)
            },
            onMarketingCreateClick = {
                screenState = screenState.closeFab()
                navController.navigate("marketing_create")
            },
            onReminderCreateClick = {
                screenState = screenState.closeFab()
                navController.navigate("reminder_create")
            }
        )

        // 오버레이 요소들 (사이드바, 모달, FAB 배경)
        OverlayManager(
            state = screenState,
            navController = navController,
            onSidebarClose = {
                screenState = screenState.closeSidebar()
            },
            onFabClose = {
                screenState = screenState.closeFab()
            },
            onModalClose = {
                screenState = screenState.closeOpportunityModal()
            },
            onDateChange = { date, opportunities ->
                screenState = screenState.updateSelectedDate(date, opportunities)
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun MainScreenPreview() {
    val context = LocalContext.current
    val fakeNavController = rememberNavController()

    MainScreen(navController = fakeNavController)
}