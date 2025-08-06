package com.voyz.presentation.screen.management.operation

import StepProgressIndicator
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun OperationManagementMenuProcessingScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(1500L) // 1.5초 후 다음 단계로
        navController.navigate("operation_management_menu_confirm")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StepProgressIndicator(
            steps = listOf("사진등록", "AI분석중", "결과"),
            currentStep = 1
        )
        Spacer(modifier = Modifier.height(32.dp))

        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text("AI가 메뉴를 분석 중입니다...", style = MaterialTheme.typography.titleMedium)
    }
}