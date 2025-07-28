package com.voyz.presentation.fragment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.voyz.presentation.component.reminder.ReminderCalendarComponent
import com.voyz.presentation.component.reminder.ReminderCalendarViewModel
import com.voyz.presentation.component.reminder.ReminderListBox
import com.voyz.presentation.component.sidebar.SidebarComponent
import com.voyz.presentation.component.topbar.CommonTopBar
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    navController: NavController,
    onSearchClick: () -> Unit = {},
    onAlarmClick: () -> Unit = {},
    onTodayClick: () -> Unit = {},
    today: LocalDate = LocalDate.now()
) {
    var isSidebarOpen by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val sidebarWidth = with(density) { 280.dp.toPx() }
    val RemindercalendarViewModel: ReminderCalendarViewModel = viewModel()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val selectedDate = RemindercalendarViewModel.selectedDate
    val selectedEvents = remember(selectedDate, RemindercalendarViewModel.events) {
        RemindercalendarViewModel.getEventsForDate(selectedDate ?: LocalDate.MIN)
    }

    val animatedOffset by animateFloatAsState(
        targetValue = if (isSidebarOpen) sidebarWidth else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "sidebar_offset"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .offset(x = with(density) { (animatedOffset + dragOffset).toDp() })
                .pointerInput(isSidebarOpen) {
                    if (!isSidebarOpen) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (dragOffset > sidebarWidth * 0.3f) {
                                    isSidebarOpen = true
                                }
                                dragOffset = 0f
                            }
                        ) { _, dragAmount ->
                            val newOffset = (dragOffset + dragAmount).coerceIn(0f, sidebarWidth)
                            dragOffset = newOffset
                        }
                    }
                },
            topBar = {
                CommonTopBar(
                        onMenuClick = {isSidebarOpen=true},
                        onSearchClick = {
                            navController.navigate("search")
                        },
                        onAlarmClick = onAlarmClick,
                        onTodayClick = {
                            RemindercalendarViewModel.goToToday()
                        },
                        today = today
                    )
            }

        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // 상단 절반: 캘린더
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),// 화면의 절반 높이

                ) {
                    ReminderCalendarComponent(
                        modifier = Modifier.fillMaxSize(),
                        viewModel = RemindercalendarViewModel,
                        isWeekly = false,
                        onDateSelected = { RemindercalendarViewModel.selectDate(it) }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.TopStart
                ) {
                    ReminderListBox(events = selectedEvents)
                }
            }
        }
    }
        if (isSidebarOpen || animatedOffset > 0f) {
            SidebarComponent(
                isOpen = isSidebarOpen,
                animatedOffset = animatedOffset + dragOffset,
                onClose = {
                    isSidebarOpen = false
                    dragOffset = 0f
                },
                navController = navController,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun ReminderScreenPreview() {
    val navController = rememberNavController()
    ReminderScreen(
        navController = navController
    )
}