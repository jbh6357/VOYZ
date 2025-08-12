package com.voyz.presentation.screen.management.operation.component

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voyz.datas.repository.AnalyticsRepository
import com.voyz.presentation.screen.management.operation.component.common.PeriodSelector
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PeriodInsightsCard(
    userId: String?,
    onPeriodChange: (LocalDate, LocalDate) -> Unit = { _, _ -> }
) {
    val analyticsRepository = remember { AnalyticsRepository() }
    val scope = rememberCoroutineScope()
    
    var startDate by remember { mutableStateOf(LocalDate.now().minusMonths(1)) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedPeriod by remember { mutableStateOf("month") }
    var isLoading by remember { mutableStateOf(false) }
    var insights by remember { mutableStateOf<Map<String, Any>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 인사이트 데이터 로드
    fun loadInsights() {
        if (userId == null) return
        
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = analyticsRepository.getPeriodInsights(userId, startDate, endDate, selectedPeriod)
                insights = result
            } catch (e: Exception) {
                errorMessage = "AI 인사이트 로드 중 오류가 발생했습니다: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // 초기 로드
    LaunchedEffect(userId, startDate, endDate, selectedPeriod) {
        if (userId != null) {
            loadInsights()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
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
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "AI 인사이트",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "AI 기간별 인사이트",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C2C2C)
                    )
                }
                
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 기간 선택기
            PeriodSelector(
                startDate = startDate,
                endDate = endDate,
                onDateRangeSelected = { start: LocalDate, end: LocalDate ->
                    startDate = start
                    endDate = end
                    onPeriodChange(start, end)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 분석 기간 선택
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val periods = listOf("week" to "주간", "month" to "월간", "quarter" to "분기")
                
                periods.forEach { (period, label) ->
                    FilterChip(
                        onClick = { selectedPeriod = period },
                        label = { Text(label) },
                        selected = selectedPeriod == period,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF4CAF50),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 인사이트 컨텐츠
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "AI가 데이터를 분석하고 있습니다...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
                
                errorMessage != null -> {
                    InsightErrorCard(
                        message = errorMessage!!,
                        onRetry = { loadInsights() }
                    )
                }
                
                insights != null -> {
                    InsightsContent(insights = insights!!)
                }
                
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "분석할 데이터가 없습니다",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightsContent(insights: Map<String, Any>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 매출 예측
        insights["salesForecast"]?.let { forecast ->
            if (forecast is Map<*, *>) {
                SalesForecastCard(forecast as Map<String, Any>)
            }
        }

        // 고객 패턴
        insights["customerPatterns"]?.let { patterns ->
            if (patterns is Map<*, *>) {
                CustomerPatternsCard(patterns as Map<String, Any>)
            }
        }

        // 메뉴 추천
        insights["menuRecommendations"]?.let { recommendations ->
            if (recommendations is Map<*, *>) {
                MenuRecommendationsCard(recommendations as Map<String, Any>)
            }
        }

        // 운영 팁
        insights["operationTips"]?.let { tips ->
            if (tips is List<*>) {
                OperationTipsCard(tips as List<String>)
            }
        }
    }
}

@Composable
private fun SalesForecastCard(forecast: Map<String, Any>) {
    InsightSectionCard(
        title = "매출 예측",
        icon = Icons.Default.TrendingUp,
        iconColor = Color(0xFF2196F3)
    ) {
        forecast["prediction"]?.let { prediction ->
            Text(
                text = prediction.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2C2C2C)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            forecast["confidence"]?.let { confidence ->
                Text(
                    text = "신뢰도: ${confidence}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        
        forecast["factors"]?.let { factors ->
            if (factors is List<*>) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "주요 요인:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2C2C2C)
                )
                factors.forEach { factor ->
                    Text(
                        text = "• $factor",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerPatternsCard(patterns: Map<String, Any>) {
    InsightSectionCard(
        title = "고객 패턴",
        icon = Icons.Default.Groups,
        iconColor = Color(0xFF9C27B0)
    ) {
        patterns["peakTimes"]?.let { peakTimes ->
            if (peakTimes is List<*>) {
                Text(
                    text = "피크 시간대: ${peakTimes.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2C2C2C)
                )
            }
        }
        
        patterns["nationalityTrends"]?.let { trends ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "국가별 트렌드:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2C2C2C)
            )
            Text(
                text = trends.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun MenuRecommendationsCard(recommendations: Map<String, Any>) {
    InsightSectionCard(
        title = "메뉴 추천",
        icon = Icons.Default.Restaurant,
        iconColor = Color(0xFFFF9800)
    ) {
        recommendations["promote"]?.let { promote ->
            if (promote is List<*>) {
                Text(
                    text = "프로모션 추천:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2C2C2C)
                )
                promote.forEach { menu ->
                    Text(
                        text = "• $menu",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
            }
        }
        
        recommendations["improve"]?.let { improve ->
            if (improve is List<*>) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "개선 필요:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2C2C2C)
                )
                improve.forEach { menu ->
                    Text(
                        text = "• $menu",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OperationTipsCard(tips: List<String>) {
    InsightSectionCard(
        title = "운영 개선 팁",
        icon = Icons.Default.Lightbulb,
        iconColor = Color(0xFF4CAF50)
    ) {
        tips.forEach { tip ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2C2C2C),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InsightSectionCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C2C2C)
                )
            }
            content()
        }
    }
}

@Composable
private fun InsightErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "오류",
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF2C2C2C),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("다시 시도", color = Color.White)
            }
        }
    }
}