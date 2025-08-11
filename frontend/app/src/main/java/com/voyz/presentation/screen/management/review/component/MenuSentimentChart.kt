package com.voyz.presentation.screen.management.review.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voyz.datas.model.dto.MenuSentimentDto

@Composable
fun MenuSentimentChart(
    data: List<MenuSentimentDto>,
    modifier: Modifier = Modifier,
    onMenuClick: (MenuSentimentDto) -> Unit = {}
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(200.dp),
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

    // 수평 스크롤 방식으로 변경
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            data.forEach { menu ->
                MenuSentimentCard(
                    menu = menu,
                    onClick = { onMenuClick(menu) },
                    modifier = Modifier.width(280.dp) // 고정 폭
                )
            }
        }
    }
}

@Composable
private fun MenuSentimentCard(
    menu: MenuSentimentDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalReviews = menu.positiveCount + menu.neutralCount + menu.negativeCount
    val positiveRatio = if (totalReviews > 0) menu.positiveCount.toFloat() / totalReviews else 0f
    val neutralRatio = if (totalReviews > 0) menu.neutralCount.toFloat() / totalReviews else 0f
    val negativeRatio = if (totalReviews > 0) menu.negativeCount.toFloat() / totalReviews else 0f
    
    // 애니메이션
    var animationStarted by remember { mutableStateOf(false) }
    val animatedPositiveRatio by animateFloatAsState(
        targetValue = if (animationStarted) positiveRatio else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 200)
    )
    val animatedNeutralRatio by animateFloatAsState(
        targetValue = if (animationStarted) neutralRatio else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 400)
    )
    val animatedNegativeRatio by animateFloatAsState(
        targetValue = if (animationStarted) negativeRatio else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 600)
    )
    
    LaunchedEffect(Unit) {
        animationStarted = true
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 메뉴 정보 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = menu.menuName ?: "메뉴 ${menu.menuId}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1D1D1F),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "총 ${totalReviews}개 리뷰",
                        fontSize = 14.sp,
                        color = Color(0xFF8E8E93)
                    )
                }
                
                // 평점 표시
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(5) { index ->
                        Text(
                            text = if (index < menu.averageRating.toInt()) "★" else "☆",
                            color = if (index < menu.averageRating.toInt()) Color(0xFFFF9F0A) else Color(0xFFE5E5EA),
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = "%.1f".format(menu.averageRating),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1D1D1F),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 감성 분석 바 차트
            Column {
                // 진행바
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            color = Color(0xFFF2F2F7),
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // 긍정
                        if (animatedPositiveRatio > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animatedPositiveRatio)
                                    .background(
                                        color = Color(0xFF30D158),
                                        shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                                    )
                            )
                        }
                        
                        // 중립
                        if (animatedNeutralRatio > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animatedNeutralRatio / (1f - animatedPositiveRatio))
                                    .background(Color(0xFFFF9F0A))
                            )
                        }
                        
                        // 부정
                        if (animatedNegativeRatio > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth()
                                    .background(
                                        color = Color(0xFFFF453A),
                                        shape = RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                                    )
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 감성 분석 레이블
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SentimentLabel(
                        color = Color(0xFF30D158),
                        label = "긍정",
                        count = menu.positiveCount,
                        percentage = positiveRatio
                    )
                    
                    SentimentLabel(
                        color = Color(0xFFFF9F0A),
                        label = "중립",
                        count = menu.neutralCount,
                        percentage = neutralRatio
                    )
                    
                    SentimentLabel(
                        color = Color(0xFFFF453A),
                        label = "부정",
                        count = menu.negativeCount,
                        percentage = negativeRatio
                    )
                }
            }
        }
    }
}

@Composable
private fun SentimentLabel(
    color: Color,
    label: String,
    count: Long,
    percentage: Float
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(50))
        )
        
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF8E8E93),
                fontWeight = FontWeight.Medium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D1D1F)
                )
                Text(
                    text = "(${(percentage * 100).toInt()}%)",
                    fontSize = 11.sp,
                    color = Color(0xFF8E8E93)
                )
            }
        }
    }
}