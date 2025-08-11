package com.voyz.presentation.screen.management.operation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voyz.datas.repository.AnalyticsRepository
import com.voyz.datas.model.dto.MenuSalesDto
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MenuSalesCard(
    userId: String?,
    onPeriodChange: (LocalDate, LocalDate) -> Unit = { _, _ -> }
) {
    val today = LocalDate.now()
    val defaultStart = today.minusMonths(1).withDayOfMonth(1)
    val defaultEnd = today

    var dateRange by remember { mutableStateOf(defaultStart to defaultEnd) }
    var menuSalesData by remember { mutableStateOf<List<MenuSalesDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val analyticsRepository = remember { AnalyticsRepository() }
    val scope = rememberCoroutineScope()

    // 메뉴 매출 데이터 로드
    fun loadMenuSalesData() {
        val id = userId ?: return
        scope.launch {
            isLoading = true
            try {
                val start = dateRange.first.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val end = dateRange.second.format(DateTimeFormatter.ISO_LOCAL_DATE)
                // TODO: 실제 메뉴 매출 데이터 API 호출
                // menuSalesData = analyticsRepository.getMenuSales(id, start, end)
                
                // 임시 데이터
                menuSalesData = listOf(
                    MenuSalesDto("김치찌개", 45f),
                    MenuSalesDto("불고기", 38f),
                    MenuSalesDto("비빔밥", 32f),
                    MenuSalesDto("삼겹살", 28f),
                    MenuSalesDto("냉면", 22f)
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
            loadMenuSalesData()
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
                    text = "메뉴별 매출 TOP 5",
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
                MenuSalesDonutChart(
                    data = menuSalesData,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }
        }
    }
}

@Composable
private fun MenuSalesDonutChart(
    data: List<MenuSalesDto>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "데이터가 없습니다",
                color = Color(0xFF8E8E93),
                fontSize = 14.sp
            )
        }
        return
    }

    // VOYZ Korean Red 기반 색상 팔레트
    val colors = listOf(
        Color(0xFFE53E3E), // 부드러운 Red
        Color(0xFF4299E1), // 부드러운 Blue 
        Color(0xFF38B2AC), // 부드러운 Teal
        Color(0xFFED8936), // 부드러운 Orange
        Color(0xFFAD7AED)  // 부드러운 Purple
    )

    val total = data.map { it.count }.sum()
    val menuItems = data.mapIndexed { index, menu ->
        MenuSalesItem(
            name = menu.name,
            count = menu.count.toInt(),
            percentage = if (total > 0) menu.count / total else 0f,
            color = colors[index % colors.size]
        )
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .aspectRatio(1f)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            // 도넛 차트
            DonutChart(
                data = menuItems,
                modifier = Modifier.fillMaxSize()
            )
            
            // 중앙 통계
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "총 ${total}개",
                    fontSize = 12.sp,
                    color = Color(0xFF8E8E93)
                )
                Text(
                    text = "판매량",
                    fontSize = 10.sp,
                    color = Color(0xFF8E8E93)
                )
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            menuItems.take(5).forEach { item ->
                MenuSalesRow(item = item)
            }
        }
    }
}

@Composable
private fun MenuSalesRow(
    item: MenuSalesItem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 색상 표시기
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(item.color, androidx.compose.foundation.shape.CircleShape)
        )
        
        // 메뉴명
        Text(
            text = item.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1D1D1F),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        // 판매량
        Text(
            text = "${item.count}개",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF007AFF)
        )
    }
}

@Composable
private fun DonutChart(
    data: List<MenuSalesItem>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val radius = (minOf(canvasWidth, canvasHeight) / 2) * 0.7f
        val innerRadius = radius * 0.5f
        val center = Offset(canvasWidth / 2, canvasHeight / 2)
        
        var startAngle = -90f
        
        data.forEach { item ->
            val sweepAngle = item.percentage * 360f
            
            drawArc(
                color = item.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = radius - innerRadius)
            )
            
            startAngle += sweepAngle
        }
    }
}

data class MenuSalesItem(
    val name: String,
    val count: Int,
    val percentage: Float,
    val color: Color
)