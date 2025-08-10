package com.voyz.presentation.screen.management.operation.graph

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RevenueGraphPager(graphList: List<@Composable () -> Unit>) {
    val pagerState = rememberPagerState(initialPage = 0) { graphList.size }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                graphList[page]()
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(graphList.size) { idx ->
                val isSelected = pagerState.currentPage == idx
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 10.dp else 8.dp)
                        .background(
                            color = if (isSelected) Color.Blue else Color.LightGray,
                            shape = CircleShape
                        )
                )
                if (idx < graphList.size - 1) Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

data class MenuSales(
    val name: String,
    val count: Float,
    val color: Color
)

@Composable
fun MonthlyRevenueBarChartAnimated(
    data: List<Float>,
    periodInfo: String,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing)
    )
    LaunchedEffect(Unit) { startAnimation = true }

    Box(
        modifier = modifier
            .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        // 1) 기간 텍스트
        Text(
            text = periodInfo,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.TopStart)
        )
        // 2) 막대 차트 그리는 캔버스
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp) // 텍스트와 겹치지 않게
        ) {
            val barAreaHeight = size.height * 0.85f
            val spacing = size.width / (data.size * 2f)
            val barWidth = spacing
            val startX = spacing / 2f
            val maxY = (data.maxOrNull() ?: 0f) * 1.2f

            // Y축 점선
            repeat(5) { i ->
                val y = barAreaHeight - (i * (maxY / 5) * barAreaHeight / maxY)
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }
            // 데이터 바
            data.forEachIndexed { idx, value ->
                val barHeight = value * barAreaHeight / maxY * progress
                val left = startX + idx * spacing * 2f
                drawRoundRect(
                    color = Color(0xFFFFCC80),
                    topLeft = Offset(left, barAreaHeight - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(10f, 10f)
                )
            }
        }
        // 3) X축 레이블
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.indices.forEach { i ->
                Text(
                    text = "${i + 1}주차",
                    fontSize = 12.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

}
@Composable
fun TopMenuDonutChartAnimated(
    menuSales: List<MenuSales>,
    periodInfo: String,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
    )
    LaunchedEffect(Unit) { startAnimation = true }

    Box(
        modifier = modifier
            .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        // 기간 텍스트
        Text(
            text = periodInfo,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.TopStart)
        )

        // 차트와 범례를 Row로 배치
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp), // 기간 텍스트와 겹치지 않게
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽: 도넛 차트 (전체 너비의 50%)
            Box(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                // 도넛 차트
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(1f) // 정사각형 유지
                ) {
                    // 도넛 크기를 Canvas의 80%로 제한
                    val chartSize = size.minDimension * 0.8f
                    val center = Offset(size.width / 2, size.height / 2)
                    val strokeWidth = chartSize / 3f // 도넛 두께
                    val radius = (chartSize - strokeWidth) / 2

                    var startAngle = -90f
                    val sweepAngles = menuSales.map {
                        it.count / menuSales.sumOf { it.count.toDouble() }.toFloat() * 360f
                    }

                    sweepAngles.map { it * progress }.forEachIndexed { idx, sweep ->
                        drawArc(
                            color = menuSales[idx].color,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = Offset(
                                center.x - radius - strokeWidth/2,
                                center.y - radius - strokeWidth/2
                            ),
                            size = Size(
                                (radius + strokeWidth/2) * 2,
                                (radius + strokeWidth/2) * 2
                            ),
                            style = Stroke(width = strokeWidth)
                        )
                        startAngle += sweep
                    }
                }

                // 도넛 중앙 텍스트
                Text(
                    "TOP 5\n판매 순위",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.weight(0.05f))
            // 오른쪽: 범례 (전체 너비의 50%)
            Column(
                modifier = Modifier
                    .weight(0.25f)
                    .fillMaxHeight()
                    .padding(all = 5.dp),
                verticalArrangement = Arrangement.Center
            ) {
                val total = menuSales.sumOf { it.count.toDouble() }.toFloat()
                menuSales.forEachIndexed { idx, item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .background(item.color, CircleShape)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${item.name}",
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${"%.1f".format(item.count.toFloat() / total * 100)}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}