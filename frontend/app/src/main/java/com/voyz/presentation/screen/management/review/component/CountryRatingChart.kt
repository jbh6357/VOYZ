package com.voyz.presentation.screen.management.review.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voyz.datas.model.dto.CountryRatingItem
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CountryRatingChart(
    data: List<CountryRatingItem>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(180.dp),
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

    // VOYZ Korean Red 기반 밝고 부드러운 색상 팔레트
    val colors = listOf(
        Color(0xFFE53E3E), // 부드러운 Red (Korean Red 밝게)
        Color(0xFF4299E1), // 부드러운 Blue 
        Color(0xFF38B2AC), // 부드러운 Teal
        Color(0xFFED8936), // 부드러운 Orange
        Color(0xFFAD7AED)  // 부드러운 Purple
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
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
                data = data,
                colors = colors,
                modifier = Modifier.fillMaxSize()
            )
            
            // 중앙 통계
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val totalReviews = data.sumOf { it.count }
                val avgRating = if (totalReviews > 0) data.map { it.averageRating * it.count }.sum() / totalReviews else 0.0
                
                Text(
                    text = "총 ${totalReviews}개",
                    fontSize = 12.sp,
                    color = Color(0xFF8E8E93)
                )
                Text(
                    text = "%.1f".format(avgRating),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1D1F)
                )
                Text(
                    text = "평균 평점",
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
            data.take(5).forEachIndexed { index, item ->
                CountryRatingInlineRow(
                    item = item,
                    color = colors[index % colors.size]
                )
            }
        }
    }
}

@Composable
private fun CountryRatingInlineRow(
    item: CountryRatingItem,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 색상 표시기
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, androidx.compose.foundation.shape.CircleShape)
        )
        
        // 국기
        Text(
            text = item.flag,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        
        // 평점
        Text(
            text = "%.1f★".format(item.averageRating),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF007AFF)
        )
        
        // 참여인원 (괄호)
        Text(
            text = "(${item.count}명)",
            fontSize = 12.sp,
            color = Color(0xFF8E8E93)
        )
    }
}

@Composable
private fun DonutChart(
    data: List<CountryRatingItem>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val radius = (minOf(canvasWidth, canvasHeight) / 2) * 0.7f
        val innerRadius = radius * 0.5f
        val center = Offset(canvasWidth / 2, canvasHeight / 2)
        
        var startAngle = -90f
        
        data.forEachIndexed { index, item ->
            val sweepAngle = item.percentage * 360f
            val color = colors[index % colors.size]
            
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = Size(radius * 2, radius * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = radius - innerRadius)
            )
            
            startAngle += sweepAngle
        }
    }
}

@Composable
private fun CountryRatingRow(
    item: CountryRatingItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = Color(0xFFFAFAFA)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // 국가 플래그
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.flag,
                        fontSize = 14.sp
                    )
                }
                
                Column {
                    Text(
                        text = item.nationality,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1D1D1F),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.count}명 작성",
                        fontSize = 12.sp,
                        color = Color(0xFF8E8E93)
                    )
                }
            }
            
            // 평점 표시
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "%.1f".format(item.averageRating),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D1D1F)
                )
                Text(
                    text = "⭐",
                    fontSize = 12.sp
                )
            }
        }
    }
}