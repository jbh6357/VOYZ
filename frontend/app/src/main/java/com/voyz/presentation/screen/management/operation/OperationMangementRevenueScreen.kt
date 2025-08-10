package com.voyz.presentation.screen.management.operation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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





enum class PeriodTab { YEAR, MONTH, WEEK }

fun formatForDisplay(selected: String, context: String? = null): String = when {
    selected.startsWith("주: ") -> {
        // "주: 2025 1주차 ~ 2025 4주차" 같은 입력을 "2025년 1주차 ~ 4주차 매출비교"로 변환
        val raw = selected.removePrefix("주: ").trim()
        val parts = raw.split("~").map { it.trim() }
        val start = parts[0].replaceFirst(Regex("""^(\d{4})\s"""), "$1년 ")
        val end   = parts[1]
        val range = "$start ~ $end"
        if (context != null) "$range $context" else range
    }
    selected.startsWith("월: ") -> {
        // "월: 2025 3월 ~ 2025 8월" → "2025년 3월 ~ 2025년 8월 매출비교"
        val raw = selected.removePrefix("월: ").trim()
        val parts = raw.split("~").map { it.trim() }  // ["2025 3월", "2025 8월"]
        val formatted = parts.map { part ->
            val (y, m) = part.split(" ")
            "${y}년 ${m}"
        }.joinToString(" ~ ")
        if (context != null) "$formatted $context" else formatted
    }
    selected.startsWith("연도: ") -> {
        // "연도: 2023 ~ 2025" → "2023년 ~ 2025년 매출비교"
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

    val userId = "nnnnnn"
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
        mutableStateOf(
            formatForDisplay(
                "월: ${lastMonth.year} ${lastMonth.monthValue}월 ~ ${today.year} ${today.monthValue}월",
                "매출비교"
            )
        )
    }
    var menuPeriodInfo by remember {
        mutableStateOf(
            formatForDisplay(
                "월: ${lastMonth.year} ${lastMonth.monthValue}월 ~ ${today.year} ${today.monthValue}월",
                "매출비교"
            )
        )
    }

    val salesData by viewModel.salesData.collectAsState()
    val menuData by viewModel.menuData.collectAsState()

    LaunchedEffect(salesStartDate, salesEndDate) {
        viewModel.loadSales(userId, salesStartDate.toString(), salesEndDate.toString())
    }
    LaunchedEffect(menuStartDate, menuEndDate) {
        viewModel.loadMenus(userId, menuStartDate.toString(), menuEndDate.toString())
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        SectionBox("메뉴별 매출 TOP 5", menuPeriodInfo, { isMenuDialogOpen = true }) {
            TopMenuDonutChartAnimated(
                menuSales = menuItems,
                periodInfo = menuPeriodInfo,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.height(16.dp))
        SectionBox("기간별 매출", salesPeriodInfo, { isSalesDialogOpen = true }) {
            MonthlyRevenueBarChartAnimated(
                data = salesValues,
                periodInfo = salesPeriodInfo,
                modifier = Modifier.fillMaxWidth().height(screenHeight * 0.4f)
            )
        }
    }

    if (isSalesDialogOpen) {
        PeriodSelectionDialog(onDismiss = { isSalesDialogOpen = false }) { sel ->
            // sel 예: "연도: 2025 ~ 2025", "월: 2025 3월 ~ 2025 8월", "주: 2025 1주차 ~ 2025 4주차"
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
                    // 월 분기: monthParts 로 이름 변경
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

                    fun convertToAbsoluteWeek(year: Int, month: Int, weekInMonth: Int): Int {
                        val firstDayOfMonth = LocalDate.of(year, month, 1)
                        val wf = WeekFields.of(Locale.getDefault())
                        val baseWeek = firstDayOfMonth.get(wf.weekOfYear())
                        return baseWeek + (weekInMonth - 1)
                    }

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

                    val start = try {
                        LocalDate.now()
                            .withYear(sy)
                            .with(wf.weekOfYear(), convertToAbsoluteWeek(sy, sm, sw).toLong())
                            .with(wf.dayOfWeek(), 1)  // 월요일
                    } catch (e: Exception) {
                        LocalDate.of(sy, sm, 1)
                    }

                    val end = try {
                        LocalDate.now()
                            .withYear(ey)
                            .with(wf.weekOfYear(), convertToAbsoluteWeek(ey, em, ew).toLong())
                            .with(wf.dayOfWeek(), 7)  // 일요일
                    } catch (e: Exception) {
                        LocalDate.of(ey, em, YearMonth.of(ey, em).lengthOfMonth())
                    }

                    Pair(start, end)
                }
                else -> Pair(salesStartDate, salesEndDate)
            }

            salesStartDate   = range.first
            salesEndDate     = range.second
            salesPeriodInfo  = formatForDisplay(sel, "매출비교")
            isSalesDialogOpen = false
        }
    }

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

                    fun convertToAbsoluteWeek(year: Int, month: Int, weekInMonth: Int): Int {
                        val firstDayOfMonth = LocalDate.of(year, month, 1)
                        val wf = WeekFields.of(Locale.getDefault())
                        val baseWeek = firstDayOfMonth.get(wf.weekOfYear())
                        return baseWeek + (weekInMonth - 1)
                    }

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

                    val start = try {
                        LocalDate.now()
                            .withYear(sy)
                            .with(wf.weekOfYear(), convertToAbsoluteWeek(sy, sm, sw).toLong())
                            .with(wf.dayOfWeek(), 1)  // 월요일
                    } catch (e: Exception) {
                        LocalDate.of(sy, sm, 1)
                    }

                    val end = try {
                        LocalDate.now()
                            .withYear(ey)
                            .with(wf.weekOfYear(), convertToAbsoluteWeek(ey, em, ew).toLong())
                            .with(wf.dayOfWeek(), 7)  // 일요일
                    } catch (e: Exception) {
                        LocalDate.of(ey, em, YearMonth.of(ey, em).lengthOfMonth())
                    }

                    Pair(start, end)
                }

                else -> Pair(menuStartDate, menuEndDate)
            }

            menuStartDate   = range.first
            menuEndDate     = range.second
            menuPeriodInfo  = formatForDisplay(sel, "매출비교")
            isMenuDialogOpen = false
        }
    }
}

@Composable
fun ColumnScope.SectionBox(
    title: String,
    periodInfo: String,
    onClickPeriod: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1) 제목 + 기간 선택 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
                IconButton(onClick = onClickPeriod) {
                    Icon(Icons.Default.DateRange, contentDescription = "기간 선택")
                }
            }

            // 3) 차트 영역
            Box(modifier = Modifier.fillMaxSize(), content = content)
        }
    }
}