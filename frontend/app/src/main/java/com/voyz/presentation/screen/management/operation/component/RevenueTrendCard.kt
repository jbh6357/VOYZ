package com.voyz.presentation.screen.management.operation.component

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voyz.datas.repository.AnalyticsRepository
import com.voyz.presentation.screen.management.operation.graph.HourlySalesBarChart
import com.voyz.utils.MoneyFormats
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RevenueTrendCard(
    userId: String?,
    modifier: Modifier = Modifier
) {
    val repo = remember { AnalyticsRepository() }
    val scope = rememberCoroutineScope()

    // 기간 토글 상태
    val periodOptions = listOf("지난 7일", "이번 달", "올해")
    val periodKeys = listOf("week", "month", "year")
    var selectedPeriodIndex by remember { mutableStateOf(1) } // 기본: 이번 달

    // 데이터 상태
    var isLoading by remember { mutableStateOf(false) }
    var summary by remember { mutableStateOf<Map<String, Any>?>(null) }
    var insights by remember { mutableStateOf<List<String>>(emptyList()) }
    var hourlyLabels by remember { mutableStateOf<List<String>>(emptyList()) }
    var hourlyAmounts by remember { mutableStateOf<List<Double>>(emptyList()) }

    fun calcRange(index: Int): Pair<LocalDate, LocalDate> = when (index) {
        0 -> LocalDate.now().minusDays(6) to LocalDate.now()       // 지난 7일
        1 -> LocalDate.now().withDayOfMonth(1) to LocalDate.now()  // 이번 달
        else -> LocalDate.now().withDayOfYear(1) to LocalDate.now()// 올해
    }

    fun loadData() {
        val id = userId ?: return
        val (start, end) = calcRange(selectedPeriodIndex)
        scope.launch {
            isLoading = true
            try {
                // 1) 매출 인사이트(요약/증감/예측)
                val si = repo.getSalesInsights(id, periodKeys[selectedPeriodIndex])
                val s = si["summary"] as? Map<*, *>
                summary = s?.mapKeys { it.key.toString() } as? Map<String, Any>
                val ins = (si["insights"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                val pred = si["predictions"]?.toString()?.let { if (it.isNotBlank()) listOf(it) else emptyList() } ?: emptyList()
                insights = (ins + pred).take(1) // 미니멀: 한 줄만 표시

                // 2) 시간대별 매출
                val hourly = repo.getHourlySales(id, start.toString(), end.toString())
                hourlyLabels = hourly.map { it.hour ?: "00" }
                hourlyAmounts = hourly.map { it.totalAmount ?: 0.0 }
            } catch (_: Exception) {
                summary = null
                insights = emptyList()
                hourlyLabels = emptyList()
                hourlyAmounts = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(userId, selectedPeriodIndex) {
        if (userId != null) loadData()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 헤더 + 기간 토글
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "스마트 매출 분석",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = Color.Black
                )

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
                        periodOptions.forEachIndexed { index, label ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (selectedPeriodIndex == index) Color.White else Color.Transparent
                                    )
                                    .clickable { selectedPeriodIndex = index }
                                    .padding(horizontal = 8.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (selectedPeriodIndex == index) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (selectedPeriodIndex == index) Color(0xFF1D1D1F) else Color(0xFF8E8E93)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFCD212A), modifier = Modifier.size(24.dp))
                }
            } else {
                // 요약 배지 (미니멀)
                val totalSales = (summary?.get("totalSales") as? Number)?.toDouble() ?: 0.0
                val avgDaily = (summary?.get("averageDailySales") as? Number)?.toDouble() ?: 0.0
                val growthRate = (summary?.get("growthRate") as? Number)?.toDouble() ?: 0.0

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("총 매출", fontSize = 14.sp, color = Color(0xFF8E8E93))
                        Text(MoneyFormats.formatShortKoreanMoney(totalSales), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("일평균", fontSize = 14.sp, color = Color(0xFF8E8E93))
                        Text(MoneyFormats.formatShortKoreanMoney(avgDaily), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("전기 대비", fontSize = 14.sp, color = Color(0xFF8E8E93))
                        val gt = when {
                            growthRate > 0 -> "↑ ${"%.1f".format(growthRate)}%"
                            growthRate < 0 -> "↓ ${"%.1f".format(kotlin.math.abs(growthRate))}%"
                            else -> "→ 0.0%"
                        }
                        val gc = when {
                            growthRate > 0 -> Color(0xFF34C759)
                            growthRate < 0 -> Color(0xFFFF3B30)
                            else -> Color(0xFF8E8E93)
                        }
                        Text(gt, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = gc)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF2F2F7))

                // 시간대별 매출 차트
                Text(
                    text = "시간대별 매출",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1D1F)
                )
                Spacer(modifier = Modifier.height(8.dp))
                HourlySalesBarChart(
                    hours = hourlyLabels,
                    amounts = hourlyAmounts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8F9FA))
                )

                // AI 한줄 액션
                if (insights.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = insights.first(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2C2C2C),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}


