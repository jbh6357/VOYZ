package com.voyz.presentation.screen.management.operation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodSelectionDialog(
    onDismiss: () -> Unit,
    onPeriodSelected: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(PeriodTab.YEAR) }

    // 공통 옵션
    val yearOptions  = (2025 downTo 2021).map { it.toString() }
    val monthOptions = (1..12).map { "${it}월" }
    val weekOptions  = listOf("1주차", "2주차", "3주차", "4주차", "5주차")

    // 연도별 상태
    var startYear by remember { mutableStateOf(yearOptions.first()) }
    var endYear   by remember { mutableStateOf(yearOptions.last()) }

    // 월별 상태
    var startMonth by remember { mutableStateOf(monthOptions.first()) }
    var endMonth   by remember { mutableStateOf(monthOptions.first()) }

    // 주별 상태
    var startWeekYear  by remember { mutableStateOf(yearOptions.first()) }
    var startWeekMonth by remember { mutableStateOf(monthOptions.first()) }
    var startWeek      by remember { mutableStateOf(weekOptions.first()) }

    var endWeekYear  by remember { mutableStateOf(yearOptions.first()) }
    var endWeekMonth by remember { mutableStateOf(monthOptions.first()) }
    var endWeek      by remember { mutableStateOf(weekOptions.first()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)  // 플랫폼 기본 폭 사용 안 함
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth(0.85f)   // 화면의 95% 너비로 설정
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // ─── 탭 헤더 ───
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PeriodTab.values().forEach { tab ->
                        Text(
                            text = when (tab) {
                                PeriodTab.YEAR  -> "연도별"
                                PeriodTab.MONTH -> "월별"
                                PeriodTab.WEEK  -> "주별"
                            },
                            color = if (selectedTab == tab)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clickable { selectedTab = tab }
                                .padding(8.dp)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                // ─── YEAR 탭 ───
                if (selectedTab == PeriodTab.YEAR) {
                    Row(Modifier.fillMaxWidth()) {
                        DropdownSelectorString(
                            label     = "시작 연도",
                            options   = yearOptions,
                            selected  = startYear,
                            onSelected= { startYear = it },
                            modifier  = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        DropdownSelectorString(
                            label     = "종료 연도",
                            options   = yearOptions,
                            selected  = endYear,
                            onSelected= { endYear = it },
                            modifier  = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (startYear.toInt() <= endYear.toInt()
                                && endYear.toInt() - startYear.toInt() + 1 <= 5
                            ) {
                                onPeriodSelected("연도: $startYear ~ $endYear")
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("확인")
                    }
                }

                // ─── MONTH 탭 ───
                if (selectedTab == PeriodTab.MONTH) {
                    Row(Modifier.fillMaxWidth()) {
                        DropdownSelectorString(
                            label     = "시작 연도",
                            options   = yearOptions,
                            selected  = startYear,
                            onSelected= { startYear = it },
                            modifier  = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        DropdownSelectorString(
                            label     = "시작 월",
                            options   = monthOptions,
                            selected  = startMonth,
                            onSelected= { startMonth = it },
                            modifier  = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth()) {
                        DropdownSelectorString(
                            label     = "종료 연도",
                            options   = yearOptions,
                            selected  = endYear,
                            onSelected= { endYear = it },
                            modifier  = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        DropdownSelectorString(
                            label     = "종료 월",
                            options   = monthOptions,
                            selected  = endMonth,
                            onSelected= { endMonth = it },
                            modifier  = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val start = LocalDate.of(
                                startYear.toInt(),
                                startMonth.dropLast(1).toInt(),
                                1
                            )
                            val end = LocalDate.of(
                                endYear.toInt(),
                                endMonth.dropLast(1).toInt(),
                                1
                            )
                            val monthsBetween = ChronoUnit.MONTHS.between(start, end) + 1
                            if (!start.isAfter(end) && monthsBetween <= 6) {
                                onPeriodSelected("월: $startYear $startMonth ~ $endYear $endMonth")
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("확인")
                    }
                }

                // ─── WEEK 탭 (연도 / 월 / 1~5주차) ───
                if (selectedTab == PeriodTab.WEEK) {
                    // 시작
                    Row(Modifier.fillMaxWidth()) {
                        DropdownSelectorString(
                            label     = "시작 연도",
                            options   = yearOptions,
                            selected  = startWeekYear,
                            onSelected= { startWeekYear = it },
                            modifier  = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        DropdownSelectorString(
                            label     = "시작 월",
                            options   = monthOptions,
                            selected  = startWeekMonth,
                            onSelected= { startWeekMonth = it },
                            modifier  = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        DropdownSelectorString(
                            label     = "시작 주차",
                            options   = weekOptions,
                            selected  = startWeek,
                            onSelected= { startWeek = it },
                            modifier  = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // 종료
                    Row(Modifier.fillMaxWidth()) {
                        DropdownSelectorString(
                            label     = "종료 연도",
                            options   = yearOptions,
                            selected  = endWeekYear,
                            onSelected= { endWeekYear = it },
                            modifier  = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        DropdownSelectorString(
                            label     = "종료 월",
                            options   = monthOptions,
                            selected  = endWeekMonth,
                            onSelected= { endWeekMonth = it },
                            modifier  = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        DropdownSelectorString(
                            label     = "종료 주차",
                            options   = weekOptions,
                            selected  = endWeek,
                            onSelected= { endWeek = it },
                            modifier  = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            onPeriodSelected(
                                "주: $startWeekYear $startWeekMonth $startWeek ~ " +
                                        "$endWeekYear $endWeekMonth $endWeek"
                            )
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("확인")
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelectorString(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier   // 전달된 modifier 사용
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selected,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}