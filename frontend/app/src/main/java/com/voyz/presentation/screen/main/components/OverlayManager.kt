package com.voyz.presentation.screen.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.voyz.datas.model.MarketingOpportunity
import com.voyz.datas.repository.MarketingOpportunityRepository
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
    // FAB 메뉴가 열렸을 때 배경 차단 오버레이
    if (state.isFabExpanded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
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
        val marketingOpportunities = remember {
            MarketingOpportunityRepository.getDailyOpportunities()
                .associateBy { it.date }
        }

        MarketingOpportunityListModal(
            date = state.selectedDate!!,
            opportunities = state.selectedOpportunities,
            onDismiss = onModalClose,
            onOpportunityClick = { opportunity ->
                onModalClose()
                navController.navigate("marketing_opportunity_detail/${opportunity.id}")
            },
            onFabClick = {
                // FAB 클릭 시 메뉴 확장
                onFabClose()
            },
            onMarketingCreateClick = {
                onModalClose()
                navController.navigate("marketing_create")
            },
            onReminderCreateClick = {
                onModalClose()
                navController.navigate("reminder_create")
            },
            onDateChange = { newDate ->
                val opportunities = marketingOpportunities[newDate]?.opportunities ?: emptyList()
                onDateChange(newDate, opportunities)
            }
        )
    }
}