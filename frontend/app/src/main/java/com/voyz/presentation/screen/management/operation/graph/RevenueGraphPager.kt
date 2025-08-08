package com.voyz.presentation.screen.management.operation.graph

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voyz.presentation.screen.management.operation.MenuSales
import com.voyz.presentation.screen.management.operation.PeriodTab
import com.voyz.presentation.screen.management.operation.generateXAxisLabels
import java.time.LocalDate


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RevenueGraphPager(graphList: List<@Composable () -> Unit>) {
    val pagerState = rememberPagerState(initialPage = 0) {
        graphList.size
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)          // ColumnScope.weight
                .fillMaxWidth()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()   // 여기서는 weight 제거
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
                if (idx < graphList.size - 1) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}

@Composable
 fun MonthlyRevenueLineChartAnimated(
    startDate: LocalDate,
    endDate:   LocalDate,
    granularity: PeriodTab,
    periodInfo: String,
    modifier: Modifier = Modifier
 ) {
    var startAnimation by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = LinearOutSlowInEasing
        )
    )
    LaunchedEffect(Unit) { startAnimation = true }

    val slots = when(granularity) {
        PeriodTab.YEAR  -> 6   // 분기별 4개: 3,6,9,12월
        PeriodTab.MONTH -> 6   // 주차 1~4주차
        PeriodTab.WEEK  -> 7   // 요일 월~일
    }

    val xLabels = generateXAxisLabels(startDate, endDate, granularity, slots)
    val divisions = xLabels.size

    val currentMonthData = listOf(12000, 14000, 13000, 15000, 16000, 15500, 17000)
    val previousMonthData = listOf(11000, 13500, 12500, 14500, 15000, 14800, 16000)

    val displayCurrent = if (currentMonthData.size >= divisions)
        currentMonthData.take(divisions)
    else
        currentMonthData + List(divisions - currentMonthData.size) { currentMonthData.last() }

    val displayPrevious = if (previousMonthData.size >= divisions)
        previousMonthData.take(divisions)
    else
        previousMonthData + List(divisions - previousMonthData.size) { previousMonthData.last() }

    val ySteps = listOf(20000f,15000f,10000f,5000f)
    val minY = 5000f; val maxY = 20000f; val yRange = maxY - minY


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {

        //파란박스 상단 기간 고정출력
        Text(
            text = periodInfo,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(start = 8.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Y축 레이블
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(40.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                ySteps.forEach { yVal ->
                    Text(
                        text     = yVal.toInt().toString(),
                        fontSize = 12.sp,
                        color    = Color.DarkGray
                    )
                }
            }

            Spacer(Modifier.width(4.dp))

            // 그래프 + X축
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    Canvas(Modifier.fillMaxSize()) {
                        val divisions = xLabels.size
                        val spacingX  = size.width / (divisions - 1)

                        // 수평선
                        for ((index, yVal) in ySteps.withIndex()) {
                            val y = size.height * (index / (ySteps.size - 1).toFloat())
                            drawLine(
                                color       = Color.LightGray,
                                start       = Offset(0f, y),
                                end         = Offset(size.width, y),
                                strokeWidth = 1f
                            )
                        }

                        // 수직선
                        for (i in 0 until divisions) {
                            val x = i * spacingX
                            drawLine(
                                color       = Color(0xFFBDBDBD),
                                start       = Offset(x, 0f),
                                end         = Offset(x, size.height),
                                strokeWidth = 0.5f
                            )
                        }

                        // 애니메이션 적용: 현재 달 선
                        val animCur = displayCurrent.map { minY + (it - minY) * progress }
                        for (i in 0 until animCur.size - 1) {
                            val x1 = i * spacingX
                            val y1 = size.height - ((animCur[i] - minY) / yRange) * size.height
                            val x2 = (i + 1) * spacingX
                            val y2 = size.height - ((animCur[i + 1] - minY) / yRange) * size.height
                            drawLine(Color.Blue, Offset(x1, y1), Offset(x2, y2), strokeWidth = 4f)
                        }

                        // 3) displayPrevious 그리기
                        val animPrev = displayPrevious.map { minY + (it - minY) * progress }
                        for (i in 0 until animPrev.size - 1) {
                            val x1 = i * spacingX
                            val y1 = size.height - ((animPrev[i] - minY) / yRange) * size.height
                            val x2 = (i + 1) * spacingX
                            val y2 = size.height - ((animPrev[i + 1] - minY) / yRange) * size.height
                            drawLine(Color.Gray, Offset(x1, y1), Offset(x2, y2), strokeWidth = 4f)
                        }
                    }
                }

                // X축 레이블
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    xLabels.forEach { label ->
                        Text(
                            text      = label,
                            fontSize  = 12.sp,
                            textAlign = TextAlign.Center,
                            color     = Color.DarkGray
                        )
                    }
                }
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
    // 애니메이션 트리거 상태
    var startAnimation by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = LinearOutSlowInEasing
        )
    )

    // 한 번만 애니메이션 시작
    LaunchedEffect(Unit) {
        startAnimation = true
    }

    // 각 슬라이스 비율 계산
    val total = menuSales.sumOf { it.count }
    val sweepAngles = menuSales.map { it.count.toFloat() / total * 360f }
    val animatedSweepAngles = sweepAngles.map { it * progress }
    val percentages = menuSales.map { it.count.toFloat() / total * 100 }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = periodInfo,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Spacer(modifier = Modifier.width(24.dp))
            // 왼쪽: 애니메이션 도넛 차트
            Box(
                modifier = Modifier
                    .weight(0.8f)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = size.minDimension / 2.5f
                    var startAngle = -90f
                    animatedSweepAngles.forEach { sweep ->
                        drawArc(
                            color = menuSales[animatedSweepAngles.indexOf(sweep)].color,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth)
                        )
                        startAngle += sweep
                    }
                }
                Text(
                    text = "TOP 5\n판매 순위",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 오른쪽: 범례
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 50.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                menuSales.forEachIndexed { index, item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(item.color, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${item.name} - ${String.format("%.1f", percentages[index])}%",
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
