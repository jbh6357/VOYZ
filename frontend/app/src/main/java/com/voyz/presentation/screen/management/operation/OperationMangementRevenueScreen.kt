package com.voyz.presentation.screen.management.operation


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.voyz.presentation.screen.management.operation.graph.MonthlyRevenueLineChartAnimated
import com.voyz.presentation.screen.management.operation.graph.TopMenuDonutChartAnimated
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.floor
import kotlin.math.max



fun formatMonthWeek(date: LocalDate): String {
    val month = date.monthValue
    val week = ((date.dayOfMonth - 1) / 7) + 1
    return "${month}월 ${week}주차"
}
fun generateXAxisLabels(
    start: LocalDate,
    end:   LocalDate,
    granularity: PeriodTab,
    slots: Int
): List<String> = when (granularity) {
    PeriodTab.YEAR -> {
        // 분기마다 보여줄 때는 slots=4, stepMonths=12/slots
        val step = 12 / slots
        (1..slots).map { "${it*step}월" }
    }
    PeriodTab.MONTH -> {
        // 한 달 동안 주차를 slots만큼 뽑을 땐
        (1..slots).map { "${it}주차" }
    }
    PeriodTab.WEEK -> {
        // 일주일을 요일별로 slots=7로 두면
        listOf("월","화","수","목","금","토","일")
            .take(slots)
    }
}
@Composable
fun OperationManagementRevenueScreen() {
    // ── 1) 날짜 기본값 계산 (지난달~이번달)
    val today     = LocalDate.now()
    // 기본: 지난달~오늘
    val defaultStart = today.minusMonths(1).withDayOfMonth(1)
    val defaultEnd   = today

    val lastMonth = today.minusMonths(1)
    val defaultPeriod = "${lastMonth.year} ${lastMonth.monthValue}월 ~ ${today.year} ${today.monthValue}월 매출비교"

    // ① 뷰모드(그랜ularity)
    var topGranularity by remember { mutableStateOf(PeriodTab.MONTH) }
    // ② 기간 범위
    var topStartDate   by remember { mutableStateOf(defaultStart) }
    var topEndDate     by remember { mutableStateOf(defaultEnd) }
    // ③ 다이얼로그 오픈 플래그

    // ── 2) 상단/하단 고정 타이틀
    val staticTitle = "기간별 매출"
    val staticBottomTitle = "메뉴별 매출 TOP 5"

    // ── 3) 상단/하단 기간 정보
    var topPeriodInfo    by remember { mutableStateOf(defaultPeriod) }
    var bottomPeriodInfo by remember { mutableStateOf(defaultPeriod) }

    // ── 4) 상단/하단 다이얼로그 열림 플래그
    var isTopDialogOpen    by remember { mutableStateOf(false) }
    var isBottomDialogOpen by remember { mutableStateOf(false) }

    // ── 5) 상단 다이얼로그
    if (isTopDialogOpen) {
        PeriodSelectionDialog(
            onDismiss = { isTopDialogOpen = false },
            onPeriodSelected = { sel ->
                // sel: "연도: 2022 ~ 2025" or "월: 2025 5월 ~ 2025 8월" or "주: 2025 8월 2주차 ~ 2025 8월 3주차"
                when {
                    sel.startsWith("연도: ") -> {
                        topGranularity = PeriodTab.YEAR
                        val (y1,y2) = sel.removePrefix("연도: ").split("~").map { it.trim().toInt() }
                        topStartDate  = LocalDate.of(y1,1,1)
                        topEndDate    = LocalDate.of(y2,12,31)
                    }
                    sel.startsWith("월: ") -> {
                        topGranularity = PeriodTab.MONTH
                        val parts = sel.removePrefix("월: ").split("~").map { it.trim().split(" ") }
                        // parts = [["2025","5월"],["2025","8월"]]
                        val (sy,sm) = parts[0].let{ it[0].toInt() to it[1].dropLast(1).toInt() }
                        val (ey,em) = parts[1].let{ it[0].toInt() to it[1].dropLast(1).toInt() }
                        topStartDate = LocalDate.of(sy,sm,1)
                        topEndDate   = LocalDate.of(ey,em,1).withDayOfMonth(
                            LocalDate.of(ey,em,1).lengthOfMonth()
                        )
                    }
                    sel.startsWith("주: ") -> {
                        topGranularity = PeriodTab.WEEK
                        val parts = sel.removePrefix("주: ").split("~").map { it.trim().split(" ") }
                        // parts = [["2025","8월","2주차"],["2025","8월","3주차"]]
                        fun parseWeek(list: List<String>): LocalDate {
                            val y = list[0].toInt()
                            val m = list[1].dropLast(1).toInt()
                            val w = list[2].dropLast(2).toInt()
                            // 한 달 1일 기준 + (w-1)*7 일
                            return LocalDate.of(y,m,1).plusWeeks((w-1).toLong())
                        }
                        topStartDate = parseWeek(parts[0])
                        topEndDate   = parseWeek(parts[1])
                    }
                }
                isTopDialogOpen = false
            }
        )
    }

    // ── 6) 하단 다이얼로그
    if (isBottomDialogOpen) {
        PeriodSelectionDialog(
            onDismiss = { isBottomDialogOpen = false },
            onPeriodSelected = { sel ->
                bottomPeriodInfo = formatForDisplay(sel)
                isBottomDialogOpen = false
            }
        )
    }
    val topMenuItems = listOf(
        MenuSales("파스타",      250, Color(0xFFE57373)),
        MenuSales("돈까스",      220, Color(0xFF64B5F6)),
        MenuSales("냉모밀",      200, Color(0xFF81C784)),
        MenuSales("치킨샐러드",  180, Color(0xFFFFB74D)),
        MenuSales("햄버거",      150, Color(0xFFBA68C8))
    )


    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // 👉 상단 영역
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(8.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(staticTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize   = 18.sp
                        )
                    )
                    Row(
                        modifier = Modifier
                            .clickable { isTopDialogOpen = true }
                            .padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = "기간 선택", tint = Color.Gray)
                        Spacer(Modifier.width(4.dp))
                        Text("기간 선택", fontSize = 14.sp, color = Color.Black)
                    }
                }
                Spacer(Modifier.height(12.dp))

                MonthlyRevenueLineChartAnimated(
                    startDate   = topStartDate,
                    endDate     = topEndDate,
                    granularity = topGranularity,
                    periodInfo  = topPeriodInfo,
                    modifier    = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 👉 하단 영역
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(8.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, bottom = 8.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(staticBottomTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize   = 18.sp
                        )
                    )
                    Row(
                        modifier = Modifier
                            .clickable { isBottomDialogOpen = true }
                            .padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = "기간 선택", tint = Color.Gray)
                        Spacer(Modifier.width(4.dp))
                        Text("기간 선택", fontSize = 14.sp, color = Color.Black)
                    }
                }
                Spacer(Modifier.height(12.dp))

                TopMenuDonutChartAnimated(
                    menuSales  = topMenuItems,
                    periodInfo = bottomPeriodInfo,
                    modifier   = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

// ── helper: 선택된 문자열을 화면용으로 포맷해주는 함수
fun formatForDisplay(selected: String): String = when {
    selected.startsWith("주: ") -> {
        val raw = selected.removePrefix("주: ").trim()
        raw.split("~")
            .map { it.trim().replaceFirst(Regex("""^(\d{4})\s"""), "$1년 ") }
            .let { "${it[0]} ~ ${it[1]} 매출비교" }
    }
    selected.startsWith("월: ") -> {
        selected.removePrefix("월: ").trim()
            .split("~").joinToString(" ~ ") + " 매출비교"
    }
    selected.startsWith("연도: ") -> {
        selected.removePrefix("연도: ").trim()
            .split("~")
            .map { it.trim() }
            .joinToString(" ~ ") { year -> "${year}년" }
            .plus(" 매출비교")
    }
    else -> selected
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthSelector(
    selectedMonth: String,
    onMonthSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val months = listOf(
        "2025년 8월", "2025년 7월", "2025년 6월",
        "2025년 5월", "2025년 4월"
    )
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedMonth,
            onValueChange = {},
            label = { Text("월 선택") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .width(160.dp) // 너비 조정
                .height(54.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            months.forEach { month ->
                DropdownMenuItem(
                    text = { Text(month) },
                    onClick = {
                        onMonthSelected(month)
                        expanded = false
                    }
                )
            }
        }
    }
}

// RevenueGraphPager.kt 대체

// 더미 차트들

data class MenuSales( // ✅ 1번 위치
    val name: String,
    val count: Int,
    val color: Color
)


enum class PeriodTab { YEAR, MONTH, WEEK }


