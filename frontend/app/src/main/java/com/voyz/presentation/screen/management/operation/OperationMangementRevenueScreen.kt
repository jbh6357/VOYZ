package com.voyz.presentation.screen.management.operation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voyz.datas.model.dto.MenuSalesDto
import com.voyz.presentation.screen.management.operation.graph.*
import com.voyz.presentation.viewmodel.RevenueViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale
import com.voyz.datas.datastore.UserPreferencesManager
import com.voyz.datas.repository.AnalyticsRepository
import com.voyz.utils.MoneyFormats
import kotlinx.coroutines.launch

enum class PeriodTab { YEAR, MONTH, WEEK }

fun formatForDisplay(selected: String, context: String? = null): String = when {
    selected.startsWith("주: ") -> {
        val raw = selected.removePrefix("주: ").trim()
        val parts = raw.split("~").map { it.trim() }
        val start = parts[0].replaceFirst(Regex("""^(\d{4})\s"""), "$1년 ")
        val end   = parts[1]
        val range = "$start ~ $end"
        if (context != null) "$range $context" else range
    }
    selected.startsWith("월: ") -> {
        val raw = selected.removePrefix("월: ").trim()
        val parts = raw.split("~").map { it.trim() }
        val formatted = parts.map { part ->
            val (y, m) = part.split(" ")
            "${y}년 ${m}"
        }.joinToString(" ~ ")
        if (context != null) "$formatted $context" else formatted
    }
    selected.startsWith("연도: ") -> {
        val range = selected
            .removePrefix("연도: ")
            .trim()
            .split("~")
            .joinToString(" ~ ") { "${it.trim()}년" }
        if (context != null) "$range $context" else range
    }
    else -> selected
}

@Composable
fun OperationManagementRevenueScreen() {
    val viewModel: RevenueViewModel = viewModel()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    
    // userId 로드
    val context = androidx.compose.ui.platform.LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    val analyticsRepository = remember { AnalyticsRepository() }
    val scope = rememberCoroutineScope()
    var userId by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        userPreferencesManager.userId.collect { fetched ->
            userId = fetched
        }
    }

    val today = LocalDate.now()
    val lastMonth = today.minusMonths(1)
    val defaultStart = lastMonth.withDayOfMonth(1)
    val defaultEnd = today

    var salesStartDate by remember { mutableStateOf(defaultStart) }
    var salesEndDate by remember { mutableStateOf(defaultEnd) }
    var menuStartDate by remember { mutableStateOf(defaultStart) }
    var menuEndDate by remember { mutableStateOf(defaultEnd) }

    var isSalesDialogOpen by remember { mutableStateOf(false) }
    var isMenuDialogOpen by remember { mutableStateOf(false) }

    var salesPeriodInfo by remember {
        mutableStateOf<String>(
            formatForDisplay(
                "월: ${lastMonth.year} ${lastMonth.monthValue}월 ~ ${today.year} ${today.monthValue}월",
                "매출비교"
            )
        )
    }
    var menuPeriodInfo by remember {
        mutableStateOf<String>(
            formatForDisplay(
                "월: ${lastMonth.year} ${lastMonth.monthValue}월 ~ ${today.year} ${today.monthValue}월",
                "매출비교"
            )
        )
    }

    val salesData by viewModel.salesData.collectAsState()
    val menuData by viewModel.menuData.collectAsState()

    // 시간대별 매출 상태
    var hourlyAmounts by remember { mutableStateOf<List<Double>>(emptyList()) }
    var hourlyLabels by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // 매출 인사이트 상태
    var salesInsights by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoadingInsights by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        userId?.let {
            viewModel.loadSales(it, salesStartDate.toString(), salesEndDate.toString())
            viewModel.loadMenus(it, menuStartDate.toString(), menuEndDate.toString())
            // 시간대별 매출 로드 (지난 7일 기본)
            val start = LocalDate.now().minusDays(6)
            val end = LocalDate.now()
            scope.launch {
                try {
                    val hourly = analyticsRepository.getHourlySales(it, start.toString(), end.toString())
                    hourlyLabels = hourly.map { h -> h.hour }
                    hourlyAmounts = hourly.map { h -> h.totalAmount ?: 0.0 }
                } catch (e: Exception) {
                    println("시간대별 매출 로드 실패: ${e.message}")
                }
            }
        }
    }
    
    LaunchedEffect(menuStartDate, menuEndDate) {
        userId?.let {
            viewModel.loadMenus(it, menuStartDate.toString(), menuEndDate.toString())
        }
    }

    val chartColors = listOf(
        Color(0xFFE57373), Color(0xFF64B5F6),
        Color(0xFF81C784), Color(0xFFFFB74D), Color(0xFFBA68C8)
    )

    val menuItems = menuData.mapIndexed { idx: Int, dto: MenuSalesDto ->
        MenuSales(
            name = dto.name,
            count = dto.count,
            color = chartColors.getOrElse(idx) { Color.Gray })
    }
    val salesValues = salesData.map { it.totalSales }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 상단 여백
        Spacer(modifier = Modifier.height(24.dp))

        // 1. 메뉴별 매출 TOP 5 카드 (위쪽)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "메뉴별 매출 TOP 5",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color.Black
                    )
                    IconButton(onClick = { isMenuDialogOpen = true }) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "기간 선택",
                            tint = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 차트
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TopMenuDonutChartAnimated(
                        menuSales = menuItems,
                        periodInfo = menuPeriodInfo,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // 2. 매출 인사이트 카드 (아래쪽) 
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "매출 인사이트",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color.Black
                    )
                    
                    // 기간 세그먼트 컨트롤
                    val periodOptions = listOf("지난 7일", "이번 달", "올해")
                    val periodKeys = listOf("week", "month", "year")
                    var selectedPeriodIndex by remember { mutableStateOf(1) } // 기본값: 이번 달
                    
                    // 매출 인사이트 로드
                    LaunchedEffect(selectedPeriodIndex, userId) {
                        userId?.let { id ->
                            isLoadingInsights = true
                            try {
                                val period = periodKeys[selectedPeriodIndex]
                                salesInsights = analyticsRepository.getSalesInsights(id, period)
                            } catch (e: Exception) {
                                println("매출 인사이트 로드 실패: ${e.message}")
                            }
                            isLoadingInsights = false
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .width(161.dp)
                            .background(
                                color = Color(0xFFF2F2F7),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(2.dp)
                    ) {
                        Row {
                            periodOptions.forEachIndexed { index, label ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (selectedPeriodIndex == index) Color.White
                                            else Color.Transparent
                                        )
                                        .clickable { 
                                            selectedPeriodIndex = index
                                        }
                                        .padding(horizontal = 8.dp, vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (selectedPeriodIndex == index) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (selectedPeriodIndex == index) Color(0xFF1D1D1F) else Color(0xFF8E8E93)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 매출 요약 섹션
                if (isLoadingInsights) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFCD212A),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    val summary = salesInsights?.get("summary") as? Map<*, *>
                    val totalSales = (summary?.get("totalSales") as? Number)?.toDouble() ?: 0.0
                    val avgDaily = (summary?.get("averageDailySales") as? Number)?.toDouble() ?: 0.0
                    val growthRate = (summary?.get("growthRate") as? Number)?.toDouble() ?: 0.0
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 총 매출
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "총 매출",
                                fontSize = 14.sp,
                                color = Color(0xFF8E8E93)
                            )
                            Text(
                                text = MoneyFormats.formatShortKoreanMoney(totalSales),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        }
                        
                        // 일평균 매출
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "일평균 매출",
                                fontSize = 14.sp,
                                color = Color(0xFF8E8E93)
                            )
                            Text(
                                text = MoneyFormats.formatShortKoreanMoney(avgDaily),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        }
                        
                        // 전기 대비
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "전기 대비",
                                fontSize = 14.sp,
                                color = Color(0xFF8E8E93)
                            )
                            val growthText = when {
                                growthRate > 0 -> "↑ ${String.format("%.1f", growthRate)}%"
                                growthRate < 0 -> "↓ ${String.format("%.1f", kotlin.math.abs(growthRate))}%"
                                else -> "→ 0.0%"
                            }
                            val growthColor = when {
                                growthRate > 0 -> Color(0xFF34C759)
                                growthRate < 0 -> Color(0xFFFF3B30)
                                else -> Color(0xFF8E8E93)
                            }
                            Text(
                                text = growthText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = growthColor
                            )
                        }
                    }
                }
                
                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color(0xFFF2F2F7)
                )
                
                // AI 인사이트 섹션
                if (!isLoadingInsights) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "AI 인사이트",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF8E8E93)
                        )
                        
                        // 인사이트 아이템들
                        val insightsList = (salesInsights?.get("insights") as? List<*>) ?: listOf<String>()
                        val predictions = salesInsights?.get("predictions") as? String
                        
                        val allInsights = buildList<String> {
                            addAll(insightsList.filterIsInstance<String>())
                            if (!predictions.isNullOrEmpty()) {
                                add(predictions)
                            }
                        }
                        
                        if (allInsights.isEmpty()) {
                            Text(
                                text = "데이터를 분석 중입니다...",
                                fontSize = 14.sp,
                                color = Color(0xFF8E8E93),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        } else {
                            allInsights.take(3).forEach { insight ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "•",
                                        fontSize = 14.sp,
                                        color = Color(0xFFCD212A)
                                    )
                                    Text(
                                        text = insight,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1D1D1F),
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 시간대별 매출 막대 차트 (미니멀)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "시간대별 매출",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1D1F)
                    )
                    val hourlyOptions = listOf("지난 7일", "이번 달", "올해")
                    var hourlySelectedIndex by remember { mutableStateOf(0) }

                    // 로딩 및 데이터 갱신
                    LaunchedEffect(hourlySelectedIndex, userId) {
                        userId?.let { id ->
                            val (s, e) = when (hourlySelectedIndex) {
                                0 -> Pair(LocalDate.now().minusDays(6), LocalDate.now())
                                1 -> Pair(LocalDate.now().withDayOfMonth(1), LocalDate.now())
                                else -> Pair(LocalDate.now().withDayOfYear(1), LocalDate.now())
                            }
                            try {
                                val hourly = analyticsRepository.getHourlySales(id, s.toString(), e.toString())
                                hourlyLabels = hourly.map { h -> h.hour }
                                hourlyAmounts = hourly.map { h -> h.totalAmount ?: 0.0 }
                            } catch (e: Exception) {
                                println("시간대별 매출 로드 실패: ${e.message}")
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(161.dp)
                            .background(
                                color = Color(0xFFF2F2F7),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(2.dp)
                    ) {
                        Row {
                            hourlyOptions.forEachIndexed { index, label ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (hourlySelectedIndex == index) Color.White
                                            else Color.Transparent
                                        )
                                        .clickable { hourlySelectedIndex = index }
                                        .padding(horizontal = 8.dp, vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (hourlySelectedIndex == index) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (hourlySelectedIndex == index) Color(0xFF1D1D1F) else Color(0xFF8E8E93)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                HourlySalesBarChart(
                    hours = hourlyLabels,
                    amounts = hourlyAmounts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8F9FA))
                )
            }
        }
        
        // 하단 패딩
        Spacer(modifier = Modifier.height(24.dp))
    }

    // 기간별 매출 다이얼로그
    if (isSalesDialogOpen) {
        PeriodSelectionDialog(onDismiss = { isSalesDialogOpen = false }) { sel ->
            val range = when {
                sel.startsWith("연도: ") -> {
                    val (sy, ey) = sel
                        .removePrefix("연도: ")
                        .split("~")
                        .map { it.trim().removeSuffix("년") }
                        .map(String::toInt)
                    Pair(
                        LocalDate.of(sy, 1, 1),
                        LocalDate.of(ey, 12, 31)
                    )
                }
                sel.startsWith("월: ") -> {
                    val monthParts = sel
                        .removePrefix("월: ")
                        .split("~")
                        .map { it.trim() }
                    fun parseMonth(p: String): Pair<Int,Int> {
                        val (y, mText) = p.split(" ")
                        return y.toInt() to mText.removeSuffix("월").toInt()
                    }
                    val (sy, sm) = parseMonth(monthParts[0])
                    val (ey, em) = parseMonth(monthParts[1])
                    Pair(
                        LocalDate.of(sy, sm, 1),
                        YearMonth.of(ey, em).atEndOfMonth()
                    )
                }
                sel.startsWith("주: ") -> {
                    val weekParts = sel.removePrefix("주: ").split("~").map { it.trim() }
                    fun parseWeek(p: String): Triple<Int, Int, Int> {
                        val regex = """(\d{4})\D+(\d{1,2})월\s*(\d)주차""".toRegex()
                        val match = regex.find(p)
                        return match?.destructured
                            ?.let { (y, m, w) -> Triple(y.toInt(), m.toInt(), w.toInt()) }
                            ?: Triple(LocalDate.now().year, LocalDate.now().monthValue, 1)
                    }
                    val (sy, sm, sw) = parseWeek(weekParts[0])
                    val (ey, em, ew) = parseWeek(weekParts[1])
                    val wf = WeekFields.of(Locale.getDefault())
                    
                    fun convertToAbsoluteWeek(year: Int, month: Int, weekInMonth: Int): Int {
                        val firstDayOfMonth = LocalDate.of(year, month, 1)
                        val baseWeek = firstDayOfMonth.get(wf.weekOfYear())
                        return baseWeek + (weekInMonth - 1)
                    }

                    val start = try {
                        LocalDate.now()
                            .withYear(sy)
                            .with(wf.weekOfYear(), convertToAbsoluteWeek(sy, sm, sw).toLong())
                            .with(wf.dayOfWeek(), 1)
                    } catch (e: Exception) {
                        LocalDate.of(sy, sm, 1)
                    }

                    val end = try {
                        LocalDate.now()
                            .withYear(ey)
                            .with(wf.weekOfYear(), convertToAbsoluteWeek(ey, em, ew).toLong())
                            .with(wf.dayOfWeek(), 7)
                    } catch (e: Exception) {
                        LocalDate.of(ey, em, YearMonth.of(ey, em).lengthOfMonth())
                    }

                    Pair(start, end)
                }
                else -> Pair(salesStartDate, salesEndDate)
            }

            salesStartDate = range.first
            salesEndDate = range.second
            salesPeriodInfo = formatForDisplay(sel, "매출비교")
            isSalesDialogOpen = false
        }
    }

    // 메뉴별 매출 다이얼로그
    if (isMenuDialogOpen) {
        PeriodSelectionDialog(onDismiss = { isMenuDialogOpen = false }) { sel ->
            val range = when {
                sel.startsWith("연도: ") -> {
                    val raw = sel.removePrefix("연도: ").trim()
                    val (sy, ey) = raw
                        .split("~")
                        .map { it.trim().removeSuffix("년") }
                        .map(String::toInt)
                    Pair(
                        LocalDate.of(sy, 1, 1),
                        LocalDate.of(ey, 12, 31)
                    )
                }
                sel.startsWith("월: ") -> {
                    val raw = sel.removePrefix("월: ").trim()
                    val parts = raw.split("~").map { it.trim() }
                    fun parseMonthPart(p: String): Pair<Int, Int> {
                        val (y, m) = p.split(" ")
                        return y.toInt() to m.removeSuffix("월").toInt()
                    }
                    val (sy, sm) = parseMonthPart(parts[0])
                    val (ey, em) = parseMonthPart(parts[1])
                    Pair(
                        LocalDate.of(sy, sm, 1),
                        YearMonth.of(ey, em).atEndOfMonth()
                    )
                }
                sel.startsWith("주: ") -> {
                    val weekParts = sel.removePrefix("주: ").split("~").map { it.trim() }
                    fun parseWeek(p: String): Triple<Int, Int, Int> {
                        val regex = """(\d{4})\D+(\d{1,2})월\s*(\d)주차""".toRegex()
                        val match = regex.find(p)
                        return match?.destructured
                            ?.let { (y, m, w) -> Triple(y.toInt(), m.toInt(), w.toInt()) }
                            ?: Triple(LocalDate.now().year, LocalDate.now().monthValue, 1)
                    }
                    val (sy, sm, sw) = parseWeek(weekParts[0])
                    val (ey, em, ew) = parseWeek(weekParts[1])
                    val wf = WeekFields.of(Locale.getDefault())
                    
                    fun convertToAbsoluteWeek(year: Int, month: Int, weekInMonth: Int): Int {
                        val firstDayOfMonth = LocalDate.of(year, month, 1)
                        val baseWeek = firstDayOfMonth.get(wf.weekOfYear())
                        return baseWeek + (weekInMonth - 1)
                    }

                    val start = try {
                        LocalDate.now()
                            .withYear(sy)
                            .with(wf.weekOfYear(), convertToAbsoluteWeek(sy, sm, sw).toLong())
                            .with(wf.dayOfWeek(), 1)
                    } catch (e: Exception) {
                        LocalDate.of(sy, sm, 1)
                    }

                    val end = try {
                        LocalDate.now()
                            .withYear(ey)
                            .with(wf.weekOfYear(), convertToAbsoluteWeek(ey, em, ew).toLong())
                            .with(wf.dayOfWeek(), 7)
                    } catch (e: Exception) {
                        LocalDate.of(ey, em, YearMonth.of(ey, em).lengthOfMonth())
                    }

                    Pair(start, end)
                }
                else -> Pair(menuStartDate, menuEndDate)
            }

            menuStartDate = range.first
            menuEndDate = range.second
            menuPeriodInfo = formatForDisplay(sel, "매출비교")
            isMenuDialogOpen = false
        }
    }
}