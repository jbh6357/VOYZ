package com.voyz.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyz.datas.model.dto.MenuSalesDto
import com.voyz.datas.model.dto.SalesAnalyticsDto
import com.voyz.datas.repository.AnalyticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RevenueViewModel(
    private val repo: AnalyticsRepository = AnalyticsRepository()
) : ViewModel() {
    private val _salesData = MutableStateFlow<List<SalesAnalyticsDto>>(emptyList())
    val salesData: StateFlow<List<SalesAnalyticsDto>> = _salesData

    private val _menuData = MutableStateFlow<List<MenuSalesDto>>(emptyList())
    val menuData: StateFlow<List<MenuSalesDto>> = _menuData

    // 기간별 매출만 조회
    fun loadSales(userId: String, start: String, end: String) {
        viewModelScope.launch {
            _salesData.value = try {
                repo.getSalesAnalytics(userId, start, end)
            } catch (e: retrofit2.HttpException) {
                // 404든 500이든 에러시 빈 리스트로 대체
                emptyList<SalesAnalyticsDto>().also {
                    // 필요시 로깅
                    Log.e("RevenueVM", "Sales API error", e)
                }
            }
        }
    }

    // 메뉴별 매출만 조회
    fun loadMenus(userId: String, start: String, end: String, category: String? = null) {
        viewModelScope.launch {
            Log.d("RevenueVM", "Menu API call → userId=$userId, start=$start, end=$end, category=$category")  // ✅ 여기에 추가
            _menuData.value = try {
                repo.getTopMenus(userId, start, end, category)
            } catch (e: retrofit2.HttpException) {
                emptyList<MenuSalesDto>().also {
                    Log.e("RevenueVM", "Menu API error", e)
                }
            }
        }
    }
}

