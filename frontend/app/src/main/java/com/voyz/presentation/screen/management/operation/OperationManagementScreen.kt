package com.voyz.presentation.screen.management.operation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.voyz.datas.datastore.UserPreferencesManager
import com.voyz.datas.repository.AnalyticsRepository
import com.voyz.presentation.component.sidebar.SidebarComponent
import com.voyz.presentation.component.topbar.CommonTopBar
import com.voyz.presentation.screen.management.operation.component.SalesAnalysisCard
import com.voyz.presentation.screen.management.operation.component.MenuSalesCard
import com.voyz.presentation.screen.management.operation.component.MenuManagementCard
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OperationManagementScreen(
    navController: NavController,
    onSearchClick: () -> Unit = {},
    onAlarmClick: () -> Unit = {},
    onTodayClick: () -> Unit = {},
    today: LocalDate = LocalDate.now()
) {
    var isSidebarOpen by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val sidebarWidth = with(density) { 280.dp.toPx() }

    val animatedOffset by animateFloatAsState(
        targetValue = if (isSidebarOpen) sidebarWidth else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "sidebar_offset"
    )

    // 사용자 정보
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    val scope = rememberCoroutineScope()
    var userId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        userPreferencesManager.userId.collect { fetched ->
            userId = fetched
        }
    }

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
            var selectedTabIndex by remember { mutableStateOf(0) }
            val tabTitles = listOf("매출", "메뉴")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
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
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        )
                    }
                }

                when (selectedTabIndex) {
                    0 -> {
                        // 매출 탭
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // 상단 여백
                            Spacer(modifier = Modifier.height(24.dp))

                            // 매출 분석 카드
                            SalesAnalysisCard(
                                userId = userId,
                                onPeriodChange = { startDate, endDate ->
                                    // 매출 데이터 로드 로직
                                }
                            )

                            // 메뉴별 매출 카드
                            MenuSalesCard(
                                userId = userId,
                                onPeriodChange = { startDate, endDate ->
                                    // 메뉴 매출 데이터 로드 로직
                                }
                            )

                            // 하단 패딩
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                    1 -> {
                        // 메뉴 탭
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // 상단 여백
                            Spacer(modifier = Modifier.height(24.dp))

                            // 메뉴 관리 카드
                            MenuManagementCard(
                                userId = userId,
                                onMenuUpload = {
                                    navController.navigate("operation_management_menu_upload")
                                },
                                onMenuInput = {
                                    navController.navigate("operation_management_menu_input")
                                }
                            )

                            // 하단 패딩
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }

        // 사이드바
        if (isSidebarOpen || animatedOffset > 0f) {
            SidebarComponent(
                isOpen = isSidebarOpen,
                animatedOffset = animatedOffset,
                onClose = {
                    isSidebarOpen = false
                },
                navController = navController,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}