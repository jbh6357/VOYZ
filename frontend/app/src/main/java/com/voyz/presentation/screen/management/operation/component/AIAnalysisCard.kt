package com.voyz.presentation.screen.management.operation.component

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voyz.datas.model.dto.SalesAnalyticsDto
import com.voyz.datas.repository.AnalyticsRepository
import com.voyz.utils.MoneyFormats
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AIAnalysisCard(
    userId: String?,
    onPeriodChange: (LocalDate, LocalDate) -> Unit = { _, _ -> }
) {
    val today = LocalDate.now()
    val defaultStart = today.minusMonths(3) // 최근 3개월 데이터
    val defaultEnd = today

    var dateRange by remember { mutableStateOf(defaultStart to defaultEnd) }
    var salesData by remember { mutableStateOf<List<SalesAnalyticsDto>>(emptyList()) }
    var aiComment by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val analyticsRepository = remember { AnalyticsRepository() }
    val scope = rememberCoroutineScope()

    // 실제 매출 데이터 로드 및 AI 분석
    fun loadSalesDataAndAnalysis() {
        val id = userId ?: return
        scope.launch {
            isLoading = true
            try {
                // 1. 실제 매출 데이터 가져오기
                val start = dateRange.first.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val end = dateRange.second.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val realSalesData = analyticsRepository.getSalesAnalytics(id, start, end)
                
                // 디버그 로그
                println("🔍 매출 데이터 조회: $start ~ $end")
                println("📊 받은 데이터 수: ${realSalesData.size}")
                realSalesData.forEach { data ->
                    println("  - 라벨: '${data.label}', 매출: ${data.totalSales}")
                }
                
                salesData = realSalesData
                
                // 2. 매출 데이터를 ML 서비스에 보내서 한줄 분석 요청
                if (realSalesData.isNotEmpty()) {
                    val salesPayload = mapOf(
                        "salesData" to realSalesData.map { 
                            mapOf(
                                "date" to (it.label ?: "unknown"),
                                "amount" to it.totalSales,
                                "orderCount" to 0 // SalesAnalyticsDto에는 orderCount가 없으므로 기본값
                            )
                        },
                        "period" to "month"
                    )
                    
                    val insights = analyticsRepository.getPeriodInsights(id, dateRange.first, dateRange.second, "month")
                    
                    // AI 코멘트 추출
                    val tips = insights["operationTips"] as? List<*>
                    aiComment = tips?.firstOrNull()?.toString() ?: "매출 데이터를 분석 중입니다"
                } else {
                    aiComment = "매출 데이터가 충분하지 않습니다"
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
                aiComment = "데이터 분석 중 오류가 발생했습니다"
            } finally {
                isLoading = false
            }
        }
    }

    // 초기 로드
    LaunchedEffect(userId, dateRange) {
        if (userId != null) {
            loadSalesDataAndAnalysis()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "매출 트렌드",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "매출 트렌드 분석",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C2C2C)
                    )
                }
                
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF4CAF50),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = { loadSalesDataAndAnalysis() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "새로고침",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 실제 매출 차트
            if (salesData.isNotEmpty()) {
                SalesTrendChart(salesData = salesData)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // AI 한줄 코멘트
                aiComment?.let { comment ->
                    AICommentSection(comment = comment)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("실제 매출 데이터를 불러오는 중...", color = Color.Gray)
                        }
                    } else {
                        Text(
                            text = "매출 데이터가 없습니다",
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SalesTrendChart(salesData: List<SalesAnalyticsDto>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "최근 매출 추이",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2C2C)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 간단한 라인 차트
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (salesData.isNotEmpty()) {
                        drawSalesLineChart(salesData, size)
                    }
                }
                
                // 데이터 포인트 값들 표시
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    salesData.takeLast(6).forEachIndexed { index, data ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = MoneyFormats.formatShortKoreanMoney(data.totalSales.toInt()),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4CAF50)
                            )
                            Text(
                                text = when {
                                    data.label != null && data.label != "null" -> data.label
                                    salesData.size == 1 -> "전체"
                                    else -> "${index + 1}번째"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawSalesLineChart(salesData: List<SalesAnalyticsDto>, canvasSize: androidx.compose.ui.geometry.Size) {
    val dataPoints = salesData.takeLast(6) // 최근 6개 데이터포인트만
    println("🎨 차트 그리기 시작: 데이터 포인트 ${dataPoints.size}개")
    
    if (dataPoints.isEmpty()) {
        println("❌ 데이터 포인트가 없어서 차트를 그릴 수 없음")
        return
    }
    
    // 데이터가 1개만 있어도 점은 그리자
    if (dataPoints.size < 2) {
        println("⭐ 데이터 포인트가 1개뿐이므로 점만 그리기")
        val point = Offset(canvasSize.width / 2, canvasSize.height / 2)
        drawCircle(
            color = androidx.compose.ui.graphics.Color.White,
            radius = 8.dp.toPx(),
            center = point
        )
        drawCircle(
            color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
            radius = 6.dp.toPx(),
            center = point
        )
        return
    }
    
    val maxRevenue = dataPoints.maxOfOrNull { it.totalSales } ?: 0f
    val minRevenue = dataPoints.minOfOrNull { it.totalSales } ?: 0f
    val revenueRange = maxRevenue - minRevenue
    
    val chartWidth = canvasSize.width - 40.dp.toPx()
    val chartHeight = canvasSize.height - 80.dp.toPx()
    val startX = 20.dp.toPx()
    val startY = 20.dp.toPx()
    
    val points = dataPoints.mapIndexed { index, data ->
        val x = startX + (index.toFloat() / (dataPoints.size - 1)) * chartWidth
        val y = if (revenueRange > 0) {
            startY + chartHeight - ((data.totalSales - minRevenue) / revenueRange) * chartHeight
        } else {
            startY + chartHeight / 2 // 모든 값이 같으면 가운데에
        }
        Offset(x, y)
    }
    
    // 배경 영역 그리기 (그라데이션 효과)
    val path = Path().apply {
        moveTo(points.first().x, startY + chartHeight)
        points.forEach { point -> lineTo(point.x, point.y) }
        lineTo(points.last().x, startY + chartHeight)
        close()
    }
    
    drawPath(
        path = path,
        color = Color(0xFF4CAF50).copy(alpha = 0.2f)
    )
    
    // 라인 그리기
    for (i in 0 until points.size - 1) {
        drawLine(
            color = Color(0xFF4CAF50),
            start = points[i],
            end = points[i + 1],
            strokeWidth = 4.dp.toPx()
        )
    }
    
    // 데이터 포인트 그리기
    points.forEach { point ->
        drawCircle(
            color = Color.White,
            radius = 6.dp.toPx(),
            center = point
        )
        drawCircle(
            color = Color(0xFF4CAF50),
            radius = 4.dp.toPx(),
            center = point
        )
    }
}

@Composable
private fun AICommentSection(comment: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = "AI 분석",
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI 분석 코멘트",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2C2C2C),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

