package com.voyz.presentation.fragment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voyz.presentation.component.CommonTopBar
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
                .padding(16.dp)
        ) {

        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
fun ReminderScreenPreview() {
    ReminderScreen()
}