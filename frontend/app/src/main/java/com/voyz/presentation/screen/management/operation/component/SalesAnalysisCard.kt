package com.voyz.presentation.screen.management.operation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voyz.datas.repository.AnalyticsRepository
import com.voyz.presentation.screen.management.operation.graph.MonthlyRevenueBarChartAnimated
import com.voyz.utils.MoneyFormats
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun SalesAnalysisCard(
    userId: String?,
    onPeriodChange: (LocalDate, LocalDate) -> Unit = { _, _ -> }
) {
    val today = LocalDate.now()
    val defaultStart = today.minusMonths(1).withDayOfMonth(1)
    val defaultEnd = today

    var dateRange by remember { mutableStateOf(defaultStart to defaultEnd) }
    var salesData by remember { mutableStateOf<List<SalesDataDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val analyticsRepository = remember { AnalyticsRepository() }
    val scope = rememberCoroutineScope()

    // 매출 데이터 로드
    fun loadSalesData() {
        val id = userId ?: return
        scope.launch {
            isLoading = true
            try {
                val start = dateRange.first.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val end = dateRange.second.format(DateTimeFormatter.ISO_LOCAL_DATE)
                // TODO: 실제 매출 데이터 API 호출
                // salesData = analyticsRepository.getSalesData(id, start, end)
                
                // 임시 데이터
                salesData = listOf(
                    SalesDataDto("2024-12", 1500000f),
                    SalesDataDto("2025-01", 1800000f),
                    SalesDataDto("2025-02", 1600000f)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // 초기 로드
    LaunchedEffect(userId, dateRange) {
        if (userId != null) {
            loadSalesData()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 제목과 기간 세그먼트 컨트롤
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "기간별 매출",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D1D1F)
                )
                
                // 기간 세그먼트 컨트롤
                val periodOptions = listOf(
                    "지난 7일" to { 
                        val end = LocalDate.now()
                        val start = end.minusDays(6)
                        start to end
                    },
                    "이번 달" to {
                        val today = LocalDate.now()
                        val start = today.withDayOfMonth(1)
                        val end = today
                        start to end
                    },
                    "올해" to {
                        val today = LocalDate.now()
                        val start = today.withDayOfYear(1)
                        val end = today
                        start to end
                    }
                )
                
                var selectedIndex by remember { mutableStateOf(1) } // 기본값: 이번 달
                
                Box(
                    modifier = Modifier
                        .width(161.dp)
                        .background(
                            color = Color(0xFFF2F2F7),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(2.dp)
                ) {
                    Row {
                        periodOptions.forEachIndexed { index, (label, rangeFn) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (selectedIndex == index) Color.White
                                        else Color.Transparent
                                    )
                                    .clickable { 
                                        selectedIndex = index
                                        dateRange = rangeFn()
                                        onPeriodChange(dateRange.first, dateRange.second)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (selectedIndex == index) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (selectedIndex == index) Color(0xFF1D1D1F) else Color(0xFF8E8E93)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFCD212A)
                    )
                }
            } else {
                val salesValues = salesData.map { it.totalSales }
                MonthlyRevenueBarChartAnimated(
                    data = salesValues,
                    periodInfo = "매출 분석",
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }
        }
    }
}

// 임시 DTO (실제로는 기존 DTO 사용)
private data class SalesDataDto(
    val period: String,
    val totalSales: Float
)