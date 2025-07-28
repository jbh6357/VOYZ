package com.voyz.presentation.fragment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.voyz.presentation.component.calendar.CalendarComponent
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
                        onSearchClick = onSearchClick,
                        onAlarmClick = onAlarmClick,
                        onTodayClick = onTodayClick,
                        today = today
                    )
            }

        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                CalendarComponent(modifier = Modifier.fillMaxWidth())
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