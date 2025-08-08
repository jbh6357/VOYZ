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
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.voyz.presentation.component.fab.FloatingActionMenu
import com.voyz.presentation.component.reminder.*
import com.voyz.presentation.component.sidebar.SidebarComponent
import com.voyz.presentation.component.topbar.CommonTopBar
import java.time.LocalDate
import java.time.YearMonth

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

    val viewModel: ReminderCalendarViewModel = viewModel()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedEvents = remember(selectedDate, viewModel.events) {
        viewModel.getEventsForDate(selectedDate)
    }

    var isWeekly by remember { mutableStateOf(false) }
    val animatedOffset by animateFloatAsState(
        targetValue = if (isSidebarOpen) sidebarWidth else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "sidebar_offset"
    )

    var isFabExpanded by remember { mutableStateOf(false) }


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
                            dragOffset = (dragOffset + dragAmount).coerceIn(0f, sidebarWidth)
                        }
                    }
                },
            topBar = {
                CommonTopBar(
                    onMenuClick = { isSidebarOpen = true },
                    onSearchClick = { navController.navigate("search") },
                    onAlarmClick = onAlarmClick,
                    onTodayClick = { viewModel.goToToday() },
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
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val totalHeight = this.maxHeight       // or this@BoxWithConstraints.maxHeight
                val calendarTargetHeight = if (isWeekly)
                    totalHeight * 0.20f else totalHeight * 0.45f

                val animatedCalendarHeight by animateDpAsState(
                    targetValue = calendarTargetHeight,
                    animationSpec = tween(300),
                    label = "calendar_height"
                )

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 캘린더 영역
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(animatedCalendarHeight)

                    ) {
                        AnimatedContent(
                            targetState = isWeekly,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                            },
                            label = "calendar_mode_switch"
                        ) { weekly ->
                            ReminderCalendarComponent(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(animatedCalendarHeight),
                                calendarHeight = animatedCalendarHeight,
                                viewModel = viewModel,
                                isWeekly = weekly,
                                onDateSelected = { viewModel.selectDate(it) }
                            )
                        }
                    }

                // 하단 : 리마인더 리스트
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
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
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.Top
                        ) {
                            ReminderDayInfoBox(
                                selectedDate = selectedDate ?: LocalDate.now(),
                                weatherIcon = "\uD83C\uDF24️",
                                temperature = "30°C",
                                events = selectedEvents
                            )

                            ReminderListBox(
                                events = selectedEvents,
                                selectedDate = selectedDate ?: LocalDate.now(),
                                viewModel = viewModel,
                                onEventCheckChange = { event, isChecked ->
                                    viewModel.updateEventCheckStatus(event.id, isChecked)
                                },
                                navController = navController,
                                onDateChange = { newDate ->
                                    val newMonth = YearMonth.from(newDate)
                                    if (newMonth != viewModel.currentMonth.value) {
                                        viewModel.goToMonth(newMonth)
                                    }
                                    viewModel.selectDate(newDate)
                                }
                            )
                        }
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
