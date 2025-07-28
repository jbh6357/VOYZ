package com.voyz.presentation.screen.main.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.voyz.datas.model.MarketingOpportunity
import com.voyz.presentation.component.calendar.CalendarComponent
import com.voyz.presentation.component.fab.FloatingActionMenu
import com.voyz.presentation.component.gesture.sidebarDragGesture
import com.voyz.presentation.component.topbar.CommonTopBar
import com.voyz.presentation.screen.main.MainScreenState
import java.time.LocalDate

/**
 * MainScreen의 메인 컨텐츠 (캘린더 + FAB)
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    state: MainScreenState,
    onSearchClick: () -> Unit,
    onAlarmClick: () -> Unit,
    onTodayClick: () -> Unit,
    onSidebarOpen: () -> Unit,
    onDragOffsetChange: (Float) -> Unit,
    onFabToggle: () -> Unit,
    onDayClick: (LocalDate, List<MarketingOpportunity>) -> Unit,
    onMarketingCreateClick: () -> Unit,
    onReminderCreateClick: () -> Unit,
    today: LocalDate,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val sidebarWidth = with(density) { 280.dp.toPx() }
    val animatedOffset = if (state.isSidebarOpen) sidebarWidth else 0f

    Scaffold(
        modifier = modifier
            .offset(x = with(density) { (animatedOffset + state.dragOffset).toDp() })
            .sidebarDragGesture(
                isEnabled = !state.isSidebarOpen,
                sidebarWidth = sidebarWidth,
                currentDragOffset = state.dragOffset,
                onDragOffsetChange = onDragOffsetChange,
                onSidebarOpen = onSidebarOpen
            ),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            CommonTopBar(
                onMenuClick = onSidebarOpen,
                onSearchClick = onSearchClick,
                onAlarmClick = onAlarmClick,
                onTodayClick = onTodayClick,
                today = today
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .blur(if (state.isFabExpanded) 8.dp else 0.dp),
            contentAlignment = Alignment.Center
        ) {
            CalendarComponent(
                onDayClick = onDayClick
            )
        }
    }

    // FloatingActionMenu - 모달이 열리면 숨김
    AnimatedVisibility(
        visible = !state.showOpportunityModal,
        enter = scaleIn(animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)),
        exit = scaleOut(animationSpec = tween(150)) + fadeOut(animationSpec = tween(150))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionMenu(
                isExpanded = state.isFabExpanded,
                onExpandedChange = { onFabToggle() },
                onMarketingCreateClick = {
                    navController.navigate("marketing_create")
                    onMarketingCreateClick()
                },
                onReminderCreateClick = {
                    navController.navigate("reminder_create")
                    onReminderCreateClick()
                }
            )
        }
    }
}