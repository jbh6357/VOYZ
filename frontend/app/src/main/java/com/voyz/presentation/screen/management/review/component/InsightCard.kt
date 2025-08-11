package com.voyz.presentation.screen.management.review.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class InsightItem(
    val type: String, // "trend", "improvement", "strength"
    val title: String,
    val description: String,
    val priority: String, // "high", "medium", "low"
    val suggestedFilters: Map<String, String> = emptyMap()
)

@Composable
fun InsightCard(
    insight: InsightItem,
    onClick: (InsightItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (insight.type) {
        "trend" -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
        "improvement" -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        "strength" -> Color(0xFFD1FAE5) to Color(0xFF065F46)
        else -> Color(0xFFF1F5F9) to Color(0xFF475569)
    }
    
    val iconEmoji = when (insight.type) {
        "trend" -> "📈"
        "improvement" -> "⚠️"
        "strength" -> "✨"
        else -> "💡"
    }
    
    // 텍스트 길이 제한 (8자 이내)
    val shortTitle = if (insight.title.length > 8) {
        insight.title.take(7) + "…"
    } else {
        insight.title
    }
    
    Card(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onClick(insight) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = iconEmoji,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = shortTitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                maxLines = 1,
                style = androidx.compose.ui.text.TextStyle(
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            )
        }
    }
}

@Composable
fun InsightCardsSection(
    insights: List<InsightItem>,
    onInsightClick: (InsightItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (insights.isEmpty()) {
        // 로딩 상태 - 한 행으로
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { index ->
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .background(
                                    color = Color(0xFFE2E8F0),
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(11.dp)
                                .background(
                                    color = Color(0xFFE2E8F0),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }
            }
        }
        return
    }
    
    // 인사이트를 한 행으로 배치
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        insights.take(3).forEach { insight ->
            InsightCard(
                insight = insight,
                onClick = onInsightClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}