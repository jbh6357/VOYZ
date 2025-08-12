package com.voyz.presentation.screen.management.operation.component

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voyz.datas.repository.AnalyticsRepository
import com.voyz.presentation.screen.management.operation.component.common.PeriodSelector
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CustomerBehaviorCard(
    userId: String?,
    onPeriodChange: (LocalDate, LocalDate) -> Unit = { _, _ -> }
) {
    val analyticsRepository = remember { AnalyticsRepository() }
    val scope = rememberCoroutineScope()
    
    var startDate by remember { mutableStateOf(LocalDate.now().minusMonths(1)) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedPeriod by remember { mutableStateOf("month") }
    var isLoading by remember { mutableStateOf(false) }
    var behaviorData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 고객 행동 데이터 로드
    fun loadBehaviorData() {
        if (userId == null) return
        
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = analyticsRepository.getCustomerBehaviorAnalysis(userId, startDate, endDate, selectedPeriod)
                behaviorData = result
            } catch (e: Exception) {
                errorMessage = "고객 행동 분석 중 오류가 발생했습니다: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // 초기 로드
    LaunchedEffect(userId, startDate, endDate, selectedPeriod) {
        if (userId != null) {
            loadBehaviorData()
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
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "고객 행동 분석",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "고객 행동 분석",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C2C2C)
                    )
                }
                
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF2196F3)
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
                            selectedContainerColor = Color(0xFF2196F3),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 행동 분석 컨텐츠
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF2196F3))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "고객 패턴을 분석하고 있습니다...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
                
                errorMessage != null -> {
                    BehaviorErrorCard(
                        message = errorMessage!!,
                        onRetry = { loadBehaviorData() }
                    )
                }
                
                behaviorData != null -> {
                    BehaviorAnalysisContent(behaviorData = behaviorData!!)
                }
                
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "분석할 고객 데이터가 없습니다",
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
private fun BehaviorAnalysisContent(behaviorData: Map<String, Any>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 시간 패턴
        behaviorData["timePatterns"]?.let { patterns ->
            if (patterns is Map<*, *>) {
                TimePatternsCard(patterns as Map<String, Any>)
            }
        }

        // 국가별 인사이트
        behaviorData["nationalityInsights"]?.let { insights ->
            if (insights is Map<*, *>) {
                NationalityInsightsCard(insights as Map<String, Any>)
            }
        }

        // 메뉴 성과
        behaviorData["menuPerformance"]?.let { performance ->
            if (performance is Map<*, *>) {
                MenuPerformanceCard(performance as Map<String, Any>)
            }
        }

        // 액션 아이템
        behaviorData["actionItems"]?.let { items ->
            if (items is List<*>) {
                ActionItemsCard(items as List<String>)
            }
        }
    }
}

@Composable
private fun TimePatternsCard(patterns: Map<String, Any>) {
    BehaviorSectionCard(
        title = "시간 패턴 분석",
        icon = Icons.Default.Schedule,
        iconColor = Color(0xFF4CAF50)
    ) {
        patterns["busyHours"]?.let { busyHours ->
            if (busyHours is List<*>) {
                Text(
                    text = "바쁜 시간대:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2C2C2C)
                )
                Text(
                    text = busyHours.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }
        
        patterns["quietHours"]?.let { quietHours ->
            if (quietHours is List<*>) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "한가한 시간대:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2C2C2C)
                )
                Text(
                    text = quietHours.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }
        
        patterns["weekdayVsWeekend"]?.let { comparison ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "평일/주말 차이:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2C2C2C)
            )
            Text(
                text = comparison.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun NationalityInsightsCard(insights: Map<String, Any>) {
    BehaviorSectionCard(
        title = "국가별 인사이트",
        icon = Icons.Default.Public,
        iconColor = Color(0xFF2196F3)
    ) {
        insights["topNationalities"]?.let { nationalities ->
            if (nationalities is List<*>) {
                Text(
                    text = "주요 방문 국가:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2C2C2C)
                )
                nationalities.forEach { nationality ->
                    Text(
                        text = "• $nationality",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
            }
        }
        
        insights["spendingPatterns"]?.let { patterns ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "소비 패턴:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2C2C2C)
            )
            Text(
                text = patterns.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
        
        insights["preferences"]?.let { preferences ->
            if (preferences is Map<*, *>) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "국가별 선호도:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2C2C2C)
                )
                preferences.forEach { (country, preference) ->
                    Text(
                        text = "• $country: $preference",
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
private fun MenuPerformanceCard(performance: Map<String, Any>) {
    BehaviorSectionCard(
        title = "메뉴 성과 분석",
        icon = Icons.Default.TrendingUp,
        iconColor = Color(0xFFFF9800)
    ) {
        performance["trending"]?.let { trending ->
            if (trending is List<*>) {
                Text(
                    text = "인기 상승 메뉴:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2C2C2C)
                )
                trending.forEach { menu ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = menu.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
        
        performance["declining"]?.let { declining ->
            if (declining is List<*>) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "관심도 하락 메뉴:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2C2C2C)
                )
                declining.forEach { menu ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = Color(0xFFFF5722),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = menu.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF5722)
                        )
                    }
                }
            }
        }
        
        performance["underrated"]?.let { underrated ->
            if (underrated is List<*>) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "저평가된 메뉴:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2C2C2C)
                )
                underrated.forEach { menu ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = menu.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionItemsCard(items: List<String>) {
    BehaviorSectionCard(
        title = "추천 액션",
        icon = Icons.Default.Assignment,
        iconColor = Color(0xFF9C27B0)
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF9C27B0),
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2C2C2C),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BehaviorSectionCard(
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
private fun BehaviorErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
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
                tint = Color(0xFFF44336),
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("다시 시도", color = Color.White)
            }
        }
    }
}