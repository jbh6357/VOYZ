package com.voyz.presentation.fragment

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.voyz.presentation.component.calendar.CalendarComponent
import com.voyz.presentation.component.fab.FloatingActionMenu
import com.voyz.presentation.component.sidebar.SidebarComponent
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.voyz.presentation.component.topbar.CommonTopBar
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import com.voyz.data.model.MarketingOpportunity
import com.voyz.data.repository.MarketingOpportunityRepository
import com.voyz.presentation.component.modal.MarketingOpportunityListModal

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
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
    
    var isFabExpanded by remember { mutableStateOf(false) }
    var showOpportunityModal by remember { mutableStateOf(false) }
    var selectedOpportunities by remember { mutableStateOf<List<MarketingOpportunity>>(emptyList()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // 메인 콘텐츠 (슬라이딩)
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
            contentWindowInsets = WindowInsets(0),
            topBar = {
                CommonTopBar(
                    onMenuClick = { isSidebarOpen = true },
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
                    .blur(if (isFabExpanded) 8.dp else 0.dp),
                contentAlignment = Alignment.Center
            ) {
                CalendarComponent(
                    onDayClick = { date, opportunities ->
                        selectedDate = date
                        selectedOpportunities = opportunities
                        showOpportunityModal = true
                    }
                )
            }
        }
        
        // FAB 메뉴가 열렸을 때 배경 차단 오버레이
        if (isFabExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // 배경 클릭 시 FAB 메뉴 닫기
                        isFabExpanded = false
                    }
            )
        }
        
        // FloatingActionMenu (블러 효과 제외) - 모달이 열리면 숨김
        AnimatedVisibility(
            visible = !showOpportunityModal,
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
                    isExpanded = isFabExpanded,
                    onExpandedChange = { expanded ->
                        isFabExpanded = expanded
                    },
                    onMarketingCreateClick = {
                        navController.navigate("marketing_create")
                    },
                    onReminderCreateClick = {
                        navController.navigate("reminder_create")
                    }
                )
            }
        }
        
        // 사이드바 (최상단 오버레이)
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
        
        // 마케팅 기회 리스트 모달
        if (showOpportunityModal && selectedDate != null) {
            val marketingOpportunities = remember {
                MarketingOpportunityRepository.getDailyOpportunities()
                    .associateBy { it.date }
            }
            
            MarketingOpportunityListModal(
                date = selectedDate!!,
                opportunities = selectedOpportunities,
                onDismiss = { 
                    showOpportunityModal = false 
                },
                onOpportunityClick = { opportunity ->
                    showOpportunityModal = false
                    navController.navigate("marketing_opportunity_detail/${opportunity.id}")
                },
                onFabClick = {
                    // FAB 클릭 시 메뉴 확장
                    isFabExpanded = !isFabExpanded
                },
                onMarketingCreateClick = {
                    showOpportunityModal = false
                    navController.navigate("marketing_create")
                },
                onReminderCreateClick = {
                    showOpportunityModal = false
                    navController.navigate("reminder_create")
                },
                onDateChange = { newDate ->
                    selectedDate = newDate
                    // 일정이 없어도 날짜 변경 허용
                    selectedOpportunities = marketingOpportunities[newDate]?.opportunities ?: emptyList()
                }
            )
        }
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