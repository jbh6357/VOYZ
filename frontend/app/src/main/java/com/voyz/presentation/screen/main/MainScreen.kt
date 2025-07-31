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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    navController: NavController,
    onSearchClick: () -> Unit = {},
    onAlarmClick: () -> Unit = {},
    onTodayClick: () -> Unit = {},
    today: LocalDate = LocalDate.now()
) {
    var state by remember { mutableStateOf(MainScreenState()) }
    val context = LocalContext.current
    val vm = remember(context) { CalendarViewModel(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        MainContent(
            state = state,
            calendarViewModel = vm,
            onSearchClick = onSearchClick,
            onAlarmClick = onAlarmClick,
            onTodayClick = { vm.goToToday() },
            onSidebarOpen = { state = state.openSidebar() },
            onDragOffsetChange = { offset -> state = state.updateDragOffset(offset) },
            onFabToggle = { state = state.toggleFab() },
            onDateClick = { date, opps ->
                state = state.openOpportunityModal(date, opps)
            },
            onMarketingCreateClick = {
                state = state.closeFab()
                navController.navigate("marketing_create")
            },
            onReminderCreateClick = {
                state = state.closeFab()
                navController.navigate("reminder_create")
            }
        )

        OverlayManager(
            state = state,
            navController = navController,
            onSidebarClose = { state = state.closeSidebar() },
            onFabClose = { state = state.closeFab() },
            onModalClose = { state = state.closeOpportunityModal() },
            onDateChange = { date, opps -> state = state.updateSelectedDate(date, opps) }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, widthDp = 400, heightDp = 800)
@Composable
fun MainScreenPreview() {
    val fakeNav = rememberNavController()
    MainScreen(navController = fakeNav)
}
