package com.voyz.presentation.screen.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.voyz.datas.model.MarketingOpportunity
import android.util.Log
import com.voyz.presentation.component.modal.MarketingOpportunityListModal
import com.voyz.presentation.component.sidebar.SidebarComponent
import com.voyz.presentation.screen.main.MainScreenState
import java.time.LocalDate

/**
 * MainScreen의 모든 오버레이 요소들을 관리하는 컴포넌트
 */
@Composable
fun OverlayManager(
    state: MainScreenState,
    navController: NavController,
    onSidebarClose: () -> Unit,
    onFabClose: () -> Unit,
    onModalClose: () -> Unit,
    onDateChange: (LocalDate, List<MarketingOpportunity>) -> Unit,
    modifier: Modifier = Modifier
) {
    // FAB 메뉴가 열렸을 때 배경 차단 오버레이 (FAB 영역 제외)
    if (state.isFabExpanded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp, end = 80.dp) // FAB 영역 제외
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onFabClose()
                }
        )
    }

    // 사이드바 오버레이
    val density = LocalDensity.current
    val sidebarWidth = with(density) { 280.dp.toPx() }
    val animatedOffset = if (state.isSidebarOpen) sidebarWidth else 0f

    if (state.isSidebarOpen || animatedOffset > 0f) {
        SidebarComponent(
            isOpen = state.isSidebarOpen,
            animatedOffset = animatedOffset + state.dragOffset,
            onClose = onSidebarClose,
            navController = navController,
            modifier = Modifier.fillMaxSize()
        )
    }

    // 마케팅 기회 리스트 모달
    if (state.showOpportunityModal && state.selectedDate != null) {

        MarketingOpportunityListModal(
            date = state.selectedDate!!,
            opportunities = state.selectedOpportunities,
            onDismiss = onModalClose,
            onOpportunityClick = { opportunity ->
                Log.d("OverlayManager", "Opportunity clicked - ID: ${opportunity.id}, Title: ${opportunity.title}")
                onModalClose()
                
                // ID를 구분해서 올바른 페이지로 이동
                when {
                    opportunity.id.startsWith("reminder_") -> {
                        // 리마인더 상세보기로 이동
                        val reminderPart = opportunity.id.removePrefix("reminder_")
                        val marketingIdx = reminderPart.split("_").firstOrNull()?.toIntOrNull() ?: 0
                        navController.navigate("reminder_detail/$marketingIdx")
                    }
                    opportunity.id.startsWith("suggestion_") -> {
                        // 제안 상세보기로 이동
                        val suggestionPart = opportunity.id.removePrefix("suggestion_")
                        val ssuIdx = suggestionPart.split("_").firstOrNull()?.toIntOrNull() ?: 0
                        navController.navigate("marketing_opportunity/$ssuIdx")
                    }
                    opportunity.id.startsWith("special_day_") -> {
                        // 특일 제안 상세보기로 이동 (suggestion과 동일하게 처리)
                        val specialDayPart = opportunity.id.removePrefix("special_day_")
                        val sdIdx = specialDayPart.split("_").firstOrNull()?.toIntOrNull() ?: 0
                        // 특일은 제안으로 처리 (백엔드 구조상 ssu_idx가 필요)
                        navController.navigate("marketing_opportunity/$sdIdx")
                    }
                    else -> {
                        Log.w("OverlayManager", "Unknown opportunity ID format: ${opportunity.id}")
                    }
                }
            },
            onFabClick = {
                // FAB 클릭 시 메뉴 확장
                onFabClose()
            },
            onMarketingCreateClick = {
                onModalClose()
                navController.navigate("marketing_create")
            },
            onReminderCreateClick = { selectedDate ->
                android.util.Log.d("OverlayManager", "=== Navigating to reminder_create ===")
                android.util.Log.d("OverlayManager", "selectedDate: $selectedDate")
                android.util.Log.d("OverlayManager", "selectedDate.year: ${selectedDate.year}, month: ${selectedDate.monthValue}, day: ${selectedDate.dayOfMonth}")
                android.util.Log.d("OverlayManager", "Current LocalDate.now(): ${java.time.LocalDate.now()}")
                
                // 안전장치: 이상한 연도면 현재 날짜로 대체
                val safeDate = if (selectedDate.year < 2020 || selectedDate.year > 2030) {
                    android.util.Log.w("OverlayManager", "Invalid year ${selectedDate.year}, using current date")
                    java.time.LocalDate.now()
                } else {
                    selectedDate
                }
                
                // 날짜를 ISO 형식으로 변환
                val dateString = safeDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                val navigationUrl = "reminder_create?date=${dateString}"
                android.util.Log.d("OverlayManager", "Safe date: $safeDate")
                android.util.Log.d("OverlayManager", "Date string: $dateString") 
                android.util.Log.d("OverlayManager", "Final navigation URL: $navigationUrl")
                
                onModalClose()
                navController.navigate(navigationUrl)
            },
            onDateChange = { newDate ->
                // 새로운 날짜의 기회는 이미 캘린더에서 로드된 데이터를 사용
                onDateChange(newDate, emptyList()) // 임시로 빈 리스트, 실제로는 캘린더에서 관리
            }
        )
    }
}