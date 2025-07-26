package com.voyz.presentation.fragment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.voyz.presentation.component.topbar.CommonTopBar
import com.voyz.presentation.component.calendar.CalendarComponent
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReminderScreen(
    onSearchClick: () -> Unit = {},
    onAlarmClick: () -> Unit = {},
    onTodayClick: () -> Unit = {},
    today: LocalDate = LocalDate.now()
) {
    Scaffold(
        topBar = {
            CommonTopBar(
                onSearchClick = onSearchClick,
                onAlarmClick = onAlarmClick,
                onTodayClick = onTodayClick,
                today = today
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CalendarComponent(
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
fun ReminderScreenPreview() {
    ReminderScreen()
}