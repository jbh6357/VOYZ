package com.voyz.presentation.screen.management.review

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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.voyz.presentation.component.sidebar.SidebarComponent
import com.voyz.presentation.component.topbar.CommonTopBar
import java.time.LocalDate
import com.voyz.presentation.screen.management.review.model.Review

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerManagementScreen(
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

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("리뷰 분석", "전체 리뷰")

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .offset(x = with(density) { animatedOffset.toDp() }),
            topBar = {
                CommonTopBar(
                    onMenuClick = { isSidebarOpen = true },
                    onSearchClick = onSearchClick,
                    onAlarmClick = onAlarmClick,
                    onTodayClick = onTodayClick,
                    today = today
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleLarge // 운영관리 스타일 그대로
                                    )
                                }
                            )
                        }
                    }
                    when (selectedTabIndex) {
                        0 -> ReviewAnalysisScreen()
                        1 -> ReviewListScreen()
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
}