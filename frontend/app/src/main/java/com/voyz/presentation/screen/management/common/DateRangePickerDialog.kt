package com.voyz.presentation.screen.management.common

import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.intl.Locale
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Pair<LocalDate, LocalDate>) -> Unit,
) {
    if (!visible) return

    val state = rememberDateRangePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = {
                val startMillis = state.selectedStartDateMillis
                val endMillis = state.selectedEndDateMillis
                if (startMillis != null && endMillis != null) {
                    val start = Instant.ofEpochMilli(startMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                    val end = Instant.ofEpochMilli(endMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                    onConfirm(start to end)
                } else {
                    onDismiss()
                }
            }) { Text("확인") }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text("취소") }
        }
    ) {
        DateRangePicker(
            state = state,
            title = { Text("기간 선택") },
            headline = { Text("시작일 ~ 종료일") },
            showModeToggle = false,
            colors = DatePickerDefaults.colors()
        )
    }
}


