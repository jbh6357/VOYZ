package com.voyz.presentation.screen.reminder

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.voyz.presentation.component.fab.FloatingActionMenu
import com.voyz.presentation.component.reminder.ReminderCalendarComponent
import com.voyz.presentation.component.reminder.ReminderCalendarEvent
import com.voyz.presentation.component.reminder.ReminderCalendarViewModel
import com.voyz.presentation.component.reminder.ReminderDayInfoBox
import com.voyz.presentation.component.reminder.ReminderListBox
import com.voyz.presentation.component.sidebar.SidebarComponent
import com.voyz.presentation.component.topbar.CommonTopBar
import java.time.LocalDate
import androidx.compose.animation.with
import androidx.compose.ui.layout.onGloballyPositioned


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalAnimationApi::class)
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
    val minCalendarHeight = 300.dp
    val maxCalendarHeight = 480.dp

    val calculatedHeight = screenHeight * 0.5f

    val selectedDate = RemindercalendarViewModel.selectedDate
    val selectedEvents: List<ReminderCalendarEvent> =
        remember(selectedDate, RemindercalendarViewModel.events) {
            RemindercalendarViewModel.getEventsForDate(selectedDate ?: LocalDate.MIN)
        }

    val animatedOffset by animateFloatAsState(
        targetValue = if (isSidebarOpen) sidebarWidth else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "sidebar_offset"
    )
    var isFabExpanded by remember { mutableStateOf(false) }

    var isWeekly by remember { mutableStateOf(false) }

    val calendarHeight by animateDpAsState(
        targetValue = if (isWeekly) 220.dp else screenHeight * 0.45f,
        animationSpec = tween(300),
        label = "calendar_height"
    )

    val calendarOffsetRatio = 0.15f
    val reminderExtraHeightRatio = if (isWeekly) 1f + calendarOffsetRatio else 1f

    val calendarWeight by remember { derivedStateOf { if (isWeekly) 0.28f else 0.5f } }
    val reminderWeight by remember { derivedStateOf { 1f - calendarWeight } }


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
                    onMenuClick = { isSidebarOpen = true },
                    onSearchClick = {
                        navController.navigate("search")
                    },
                    onAlarmClick = onAlarmClick,
                    onTodayClick = {
                        RemindercalendarViewModel.goToToday()
                    },
                    today = today
                )
            },

            floatingActionButton = {
                FloatingActionMenu(
                    isExpanded = isFabExpanded,
                    onExpandedChange = { isFabExpanded = it },
                    onReminderCreateClick = {
                        navController.navigate("reminder_create")
                    },
                    onMarketingCreateClick = {
                        navController.navigate("marketing_create")
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // 상단 : 캘린더
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(calendarHeight)
                ) {
                    AnimatedContent(
                        targetState = isWeekly,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) with fadeOut(
                                animationSpec = tween(
                                    300)
                            )
                        },
                        label = "calendar_mode_switch"
                    ) { weekly ->
                        ReminderCalendarComponent(
                            modifier = Modifier.fillMaxSize(),
                            viewModel = RemindercalendarViewModel,
                            isWeekly = weekly,
                            onDateSelected = { RemindercalendarViewModel.selectDate(it) }
                        )
                    }
                }
                Box(// 하단
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-24).dp)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                if (dragAmount < -30) isWeekly = true
                                else if (dragAmount > 30) isWeekly = false
                            }

                        },
                    contentAlignment = Alignment.TopStart
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(-16.dp)
                    ) {
                        ReminderDayInfoBox(
                            selectedDate = selectedDate ?: LocalDate.now(),
                            weatherIcon = "🌤️",
                            temperature = "30°C",
                            events = selectedEvents
                        )

                        // ⬇️ 리마인더 리스트
                        ReminderListBox(
                            events = selectedEvents,
                            selectedDate = selectedDate ?: LocalDate.now(),
                            viewModel = RemindercalendarViewModel,
                            onEventCheckChange = { event, isChecked ->
                                RemindercalendarViewModel.updateEventCheckStatus(
                                    event.id,
                                    isChecked
                                )
                            }
                        )
                    }
                }
            }

        }

        if (isSidebarOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
                    .pointerInput(Unit) {
                        detectTapGestures {
                            isSidebarOpen = false
                            dragOffset = 0f
                        }
                    }
                    .zIndex(0.5f)
            )
        }

        if (isSidebarOpen || animatedOffset > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .graphicsLayer {
                        translationX = animatedOffset + dragOffset - sidebarWidth
                    }
                    .zIndex(1f)
            ) {
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
    }
}
    @RequiresApi(Build.VERSION_CODES.O)
    @Preview(showBackground = true)
    @Composable
    fun ReminderScreenPreview() {
        val navController = rememberNavController()
        ReminderScreen(navController = navController)
    }

