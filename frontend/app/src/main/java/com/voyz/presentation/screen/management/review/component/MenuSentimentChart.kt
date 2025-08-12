package com.voyz.presentation.screen.management.review.component

import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
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

    // HorizontalPager로 무한 순환 스크롤 구현
    val pagerState = rememberPagerState(
        initialPage = Int.MAX_VALUE / 2,
        pageCount = { Int.MAX_VALUE }
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        if (data.size == 1) {
            // 메뉴가 1개일 때는 단일 카드 표시 (스와이프 불가)
            val pulseAnimation by rememberInfiniteTransition(label = "pulse").animateFloat(
                initialValue = 1f,
                targetValue = 1.02f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            )
            
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                MenuSentimentCard(
                    menu = data[0],
                    onClick = { }, // 클릭 비활성화
                    modifier = Modifier
                        .width(280.dp)
                        .graphicsLayer {
                            scaleX = pulseAnimation
                            scaleY = pulseAnimation
                        }
                )
            }
        } else {
            // 메뉴가 2개 이상일 때는 페이저 사용
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        // 페이저 영역에서의 수평 드래그를 가로채서 사이드바 제스처 방지
                        detectHorizontalDragGestures { _, _ -> 
                            // 드래그 이벤트를 소비해서 사이드바로 전파되지 않도록 함
                        }
                    },
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                pageSpacing = 12.dp
            ) { page ->
                val actualPage = page % data.size
                MenuSentimentCard(
                    menu = data[actualPage],
                    onClick = { }, // 클릭 비활성화
                    modifier = Modifier.width(280.dp)
                )
            }
        }
        
        // 페이지 인디케이터 (도트)
        if (data.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(data.size) { index ->
                    val isSelected = pagerState.currentPage % data.size == index
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 8.dp else 6.dp)
                            .background(
                                color = if (isSelected) Color(0xFFCD212A) else Color(0xFFE5E5EA),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                    if (index < data.size - 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
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
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // 메뉴 정보 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = menu.menuName ?: "메뉴 ${menu.menuId}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f, false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "( ${totalReviews} )",
                            fontSize = 14.sp,
                            color = Color(0xFF8E8E93)
                        )
                    }
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
            
            // 리뷰 한줄평 표시
            Spacer(modifier = Modifier.height(8.dp))
            val summary = menu.reviewSummary?.takeIf { it.isNotBlank() && it != "리뷰가 없습니다" } 
                ?: generateSimpleSummary(menu)
            if (summary.isNotBlank()) {
                Text(
                    text = "\" $summary \"",
                    fontSize = 13.sp,
                    color = Color(0xFF8E8E93),
                    fontWeight = FontWeight.Medium,
                    style = androidx.compose.ui.text.TextStyle(
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
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
                            shape = RoundedCornerShape(6.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(6.dp))
                    ) {
                        // 긍정
                        if (animatedPositiveRatio > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animatedPositiveRatio)
                                    .background(Color(0xFF30D158))
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
                                    .background(Color(0xFFFF453A))
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
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF8E8E93),
                fontWeight = FontWeight.Medium
            )
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

// 다수 의견 기반 한줄평 생성 함수 (ML 서비스 실패시 대안)
@Composable
private fun generateSimpleSummary(menu: MenuSentimentDto): String {
    val totalReviews = menu.positiveCount + menu.negativeCount + menu.neutralCount
    
    return when {
        totalReviews == 0L -> ""
        menu.positiveCount > menu.negativeCount && menu.positiveCount > menu.neutralCount -> "맛있다고 해요"
        menu.negativeCount > menu.positiveCount && menu.negativeCount > menu.neutralCount -> "개선이 필요해요"
        menu.neutralCount > menu.positiveCount && menu.neutralCount > menu.negativeCount -> "괜찮은 편이에요"
        else -> "의견이 다양해요"
    }
}