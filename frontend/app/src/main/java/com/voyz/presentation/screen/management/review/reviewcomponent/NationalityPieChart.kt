package com.voyz.presentation.screen.management.review.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NationalityPieChart(
    koreanCount: Int,
    nationalityBreakdown: List<Pair<String, Int>>, // ex) [("🇺🇸 미국", 10), ("🇯🇵 일본", 8)]
    modifier: Modifier = Modifier
) {
    val total = koreanCount + nationalityBreakdown.sumOf { it.second }

    // ✅ 애니메이션 비율 제어
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(300) // 살짝 딜레이 후 시작 (선택)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400)
        )
    }

    val pieData = buildList {
        add("🇰🇷 한국" to koreanCount)
        addAll(nationalityBreakdown)
    }

    val colors = listOf(
        Color(0xFF42A5F5), // Blue
        Color(0xFFEF5350), // Red
        Color(0xFF66BB6A), // Green
        Color(0xFFFFA726), // Orange
        Color(0xFFAB47BC), // Purple
        Color(0xFFFFD54F), // Yellow
        Color(0xFF26C6DA), // Cyan
        Color(0xFFA1887F), // Brown
        Color(0xFF78909C)  // Gray Blue
    )

    Canvas(modifier = modifier) {
        val canvasSize = size.minDimension
        val radius = canvasSize / 2f
        val pieSize = Size(radius * 2, radius * 2)
        val centerX = size.width / 2
        val centerY = size.height / 2
        val topLeftX = centerX - radius
        val topLeftY = centerY - radius

        var startAngle = -90f
        pieData.forEachIndexed { index, (_, count) ->
            val sweep = if (total == 0) 0f else 360f * count / total.toFloat()
            val animatedSweep = sweep * animatedProgress.value

            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = animatedSweep,
                useCenter = true,
                topLeft = androidx.compose.ui.geometry.Offset(topLeftX, topLeftY),
                size = pieSize
            )

            startAngle += sweep
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NationalityLegend(labels: List<String>, colors: List<Color>, maxItems: Int = 6, modifier: Modifier = Modifier) {
    val items = labels.take(maxItems)
    FlowRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEachIndexed { idx, label ->
            val color = colors[idx % colors.size]
            Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
                    Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
