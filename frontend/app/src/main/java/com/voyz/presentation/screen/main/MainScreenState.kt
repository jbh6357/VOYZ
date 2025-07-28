package com.voyz.presentation.screen.main

import com.voyz.data.model.MarketingOpportunity
import java.time.LocalDate

/**
 * MainScreen의 UI 상태를 관리하는 데이터 클래스
 */
data class MainScreenState(
    val isSidebarOpen: Boolean = false,
    val isFabExpanded: Boolean = false,
    val showOpportunityModal: Boolean = false,
    val selectedDate: LocalDate? = null,
    val selectedOpportunities: List<MarketingOpportunity> = emptyList(),
    val dragOffset: Float = 0f
) {
    /**
     * 사이드바 열기
     */
    fun openSidebar() = copy(isSidebarOpen = true, dragOffset = 0f)
    
    /**
     * 사이드바 닫기
     */
    fun closeSidebar() = copy(isSidebarOpen = false, dragOffset = 0f)
    
    /**
     * FAB 메뉴 토글
     */
    fun toggleFab() = copy(isFabExpanded = !isFabExpanded)
    
    /**
     * FAB 메뉴 닫기
     */
    fun closeFab() = copy(isFabExpanded = false)
    
    /**
     * 마케팅 기회 모달 열기
     */
    fun openOpportunityModal(
        date: LocalDate,
        opportunities: List<MarketingOpportunity>
    ) = copy(
        showOpportunityModal = true,
        selectedDate = date,
        selectedOpportunities = opportunities
    )
    
    /**
     * 마케팅 기회 모달 닫기
     */
    fun closeOpportunityModal() = copy(
        showOpportunityModal = false,
        selectedDate = null,
        selectedOpportunities = emptyList()
    )
    
    /**
     * 드래그 오프셋 업데이트
     */
    fun updateDragOffset(offset: Float) = copy(dragOffset = offset)
    
    /**
     * 선택된 날짜 변경 (모달 내에서)
     */
    fun updateSelectedDate(
        date: LocalDate,
        opportunities: List<MarketingOpportunity>
    ) = copy(
        selectedDate = date,
        selectedOpportunities = opportunities
    )
}