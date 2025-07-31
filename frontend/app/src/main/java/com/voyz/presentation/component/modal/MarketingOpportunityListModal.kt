package com.voyz.presentation.component.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import kotlin.math.abs
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.voyz.datas.model.MarketingOpportunity
import com.voyz.datas.model.Priority
import com.voyz.ui.theme.MarketingColors
import com.voyz.ui.theme.getMarketingCategoryColors
import com.voyz.ui.theme.getPriorityColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MarketingOpportunityListModal(
    date: LocalDate,
    opportunities: List<MarketingOpportunity>,
    onDismiss: () -> Unit,
    onOpportunityClick: (MarketingOpportunity) -> Unit,
    onFabClick: () -> Unit = {},
    onMarketingCreateClick: () -> Unit = {},
    onReminderCreateClick: (LocalDate) -> Unit = {},
    onDateChange: (LocalDate) -> Unit = {}
) {
    var isFabExpanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 배경 터치로 모달 닫기
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
            )
            
            // 메인 카드 - 애니메이션 적용
            AnimatedContent(
                targetState = date,
                transitionSpec = {
                    val isNext = targetState > initialState
                    val slideDirection = if (isNext) 1 else -1
                    
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> slideDirection * fullWidth },
                        animationSpec = tween(300)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -slideDirection * fullWidth },
                        animationSpec = tween(300)
                    )
                },
                label = "date_transition",
                modifier = Modifier.align(Alignment.Center)
            ) { animatedDate ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 40.dp)
                        .heightIn(max = 500.dp)
                        .pointerInput(animatedDate) {
                            var totalDrag = 0f
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (abs(totalDrag) > 150) {
                                        coroutineScope.launch {
                                            kotlinx.coroutines.delay(300) // 애니메이션 완료 대기
                                            if (totalDrag > 0) {
                                                // 오른쪽 스와이프 -> 이전 날
                                                onDateChange(animatedDate.minusDays(1))
                                            } else {
                                                // 왼쪽 스와이프 -> 다음 날
                                                onDateChange(animatedDate.plusDays(1))
                                            }
                                        }
                                    }
                                }
                            ) { _, dragAmount ->
                                totalDrag += dragAmount
                            }
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* 카드 내부 클릭 차단 */ },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // 헤더
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                // 상단 핸들바
                                Box(
                                    modifier = Modifier
                                        .width(50.dp)
                                        .height(5.dp)
                                        .background(
                                            MarketingColors.TextTertiary.copy(alpha = 0.4f),
                                            RoundedCornerShape(25.dp)
                                        )
                                        .align(Alignment.CenterHorizontally)
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // 날짜 정보
                                Text(
                                    text = animatedDate.format(DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MarketingColors.TextPrimary,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // 날씨 및 온도 정보
                                Row(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "🌤️ 맑음",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MarketingColors.TextSecondary
                                    )
                                    Text(
                                        text = "25°C",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MarketingColors.TextSecondary
                                    )
                                    Text(
                                        text = "음력 6월 15일",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MarketingColors.TextSecondary
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // 일정 개수 (리마인더 + 제안 구분)
                                android.util.Log.d("Modal", "=== MarketingOpportunityListModal ===")
                                android.util.Log.d("Modal", "Date: ${animatedDate}, Total opportunities: ${opportunities.size}")
                                opportunities.forEachIndexed { index, opp ->
                                    android.util.Log.d("Modal", "[$index] ID: ${opp.id}, Title: ${opp.title}")
                                }
                                val reminderCount = opportunities.count { it.id.startsWith("reminder_") }
                                val suggestionCount = opportunities.count { it.id.startsWith("suggestion_") }
                                val opportunityCount = opportunities.count { it.id.startsWith("special_day_") }
                                android.util.Log.d("Modal", "Reminder count: $reminderCount, Suggestion count: $suggestionCount")
                                android.util.Log.d("Modal", "=== End MarketingOpportunityListModal ===")
                                
                                Text(
                                    text = when {
                                        reminderCount > 0 && (suggestionCount + opportunityCount) > 0 -> 
                                            "리마인더 ${reminderCount}개, 제안/기회 ${suggestionCount + opportunityCount}개"
                                        reminderCount > 0 -> 
                                            "${reminderCount}개의 리마인더"
                                        suggestionCount > 0 && opportunityCount > 0 ->
                                            "제안 ${suggestionCount}개, 기회 ${opportunityCount}개"
                                        suggestionCount > 0 ->
                                            "${suggestionCount}개의 제안"
                                        else -> 
                                            "${opportunityCount}개의 기회"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MarketingColors.Primary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                            
                            Divider(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                color = MarketingColors.TextTertiary.copy(alpha = 0.3f)
                            )
                            
                            // 기회 리스트
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                userScrollEnabled = true
                            ) {
                                items(opportunities) { opportunity ->
                                    MarketingOpportunityItem(
                                        opportunity = opportunity,
                                        onClick = { onOpportunityClick(opportunity) }
                                    )
                                }
                            }
                        }
                        
                        // FAB 메뉴
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 메뉴 아이템들
                            androidx.compose.animation.AnimatedVisibility(
                                visible = isFabExpanded,
                                enter = fadeIn(animationSpec = tween(200)) + scaleIn(animationSpec = tween(200)),
                                exit = fadeOut(animationSpec = tween(150)) + scaleOut(animationSpec = tween(150))
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    FabMenuItem(
                                        icon = Icons.Default.Notifications,
                                        label = "리마인더 생성",
                                        onClick = {
                                            isFabExpanded = false
                                            onReminderCreateClick(date)
                                        }
                                    )
                                    
                                    FabMenuItem(
                                        icon = Icons.Default.Campaign,
                                        label = "마케팅 생성",
                                        onClick = {
                                            isFabExpanded = false
                                            onMarketingCreateClick()
                                        }
                                    )
                                }
                            }
                            
                            // 메인 FAB
                            FloatingActionButton(
                                onClick = { isFabExpanded = !isFabExpanded },
                                containerColor = MarketingColors.Primary,
                                contentColor = Color.White,
                                elevation = FloatingActionButtonDefaults.elevation(
                                    defaultElevation = 0.dp,
                                    pressedElevation = 0.dp
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "메뉴"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FabMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MarketingColors.Surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MarketingColors.TextPrimary
            )
        }
        
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MarketingColors.Primary,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label
            )
        }
    }
}

@Composable
private fun MarketingOpportunityItem(
    opportunity: MarketingOpportunity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                opportunity.id.startsWith("reminder_") -> {
                    // 리마인더 타입별 색상
                    when (opportunity.priority) {
                        Priority.HIGH -> Color(0xFFFF4444).copy(alpha = 0.4f) // 마케팅 -> 빨간색
                        Priority.MEDIUM -> Color(0xFF2196F3).copy(alpha = 0.4f) // 일정 -> 파란색
                        else -> Color(0xFF2196F3).copy(alpha = 0.4f) // 기본값 파란색
                    }
                }
                opportunity.id.startsWith("suggestion_") -> {
                    Color(0xFFFFC107).copy(alpha = 0.4f) // 제안 -> 노란색
                }
                opportunity.id.startsWith("special_day_") -> {
                    Color(0xFF9E9E9E).copy(alpha = 0.4f) // 기회 -> 회색
                }
                else -> {
                    // 기존 로직 유지
                    when (opportunity.priority) {
                        Priority.MEDIUM -> Color(0xFFFFC107).copy(alpha = 0.4f)
                        Priority.LOW -> Color(0xFF9E9E9E).copy(alpha = 0.4f)
                        else -> Color(0xFF9E9E9E).copy(alpha = 0.4f)
                    }
                }
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        if (opportunity.id.startsWith("special_day_")) {
            // 순수 기회 - 미니멀한 표시
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 타이틀과 카테고리 좌우 배치
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = opportunity.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MarketingColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = "${opportunity.category.emoji} ${opportunity.category.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MarketingColors.TextTertiary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 설명 - 2줄까지 표시
                Text(
                    text = opportunity.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MarketingColors.TextSecondary,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        } else {
            // 리마인더/제안 - 상세한 표시
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 헤더 (카테고리, 우선순위, 신뢰도)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = opportunity.category.emoji,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = opportunity.category.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MarketingColors.TextSecondary
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 우선순위 배지
                        Box(
                            modifier = Modifier
                                .background(
                                    color = when {
                                        opportunity.id.startsWith("reminder_") -> {
                                            when (opportunity.priority) {
                                                Priority.HIGH -> Color(0xFFFF4444)
                                                Priority.MEDIUM -> Color(0xFF2196F3)
                                                else -> Color(0xFF2196F3)
                                            }
                                        }
                                        else -> {
                                            when (opportunity.priority) {
                                                Priority.MEDIUM -> Color(0xFFFFC107)
                                                Priority.LOW -> Color(0xFF9E9E9E)
                                                else -> Color(0xFF9E9E9E)
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = opportunity.priority.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (opportunity.priority == Priority.MEDIUM && !opportunity.id.startsWith("reminder_")) {
                                    Color.Black
                                } else {
                                    Color.White
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // 신뢰도
                        Text(
                            text = if (opportunity.confidence > 1.0f) {
                                "${opportunity.confidence.toInt()}%" // 이미 백분율인 경우
                            } else {
                                "${(opportunity.confidence * 100).toInt()}%" // 소수점인 경우 (0.0-1.0)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MarketingColors.TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 제목
                Text(
                    text = opportunity.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MarketingColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 설명
                Text(
                    text = opportunity.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MarketingColors.TextSecondary,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 하단 정보
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👥 ${opportunity.targetCustomer}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MarketingColors.TextSecondary
                    )
                    
                    Text(
                        text = "자세히 보기 →",
                        style = MaterialTheme.typography.bodySmall,
                        color = MarketingColors.Primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}