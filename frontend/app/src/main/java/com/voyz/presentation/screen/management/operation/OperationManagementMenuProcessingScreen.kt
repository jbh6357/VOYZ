package com.voyz.presentation.screen.management.operation

import StepProgressIndicator
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.voyz.datas.model.dto.MenuItemDto
import kotlinx.coroutines.launch

@Composable
fun OperationManagementMenuProcessingScreen(
    navController: NavController,
    imageUri: Uri?,
    onOcrComplete: (List<MenuItemDto>) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val menuRepository = remember { MenuRepository() }
    var isProcessing by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(imageUri) {
        android.util.Log.d("ProcessingScreen", "LaunchedEffect triggered, imageUri: $imageUri")
        if (imageUri != null) {
            android.util.Log.d("ProcessingScreen", "Starting OCR processing for imageUri: $imageUri")
            scope.launch {
                menuRepository.processOcrImage(imageUri, context)
                    .onSuccess { menuItems ->
                        android.util.Log.d("ProcessingScreen", "OCR success, received ${menuItems.size} items")
                        
                        // 안전한 데이터 검증
                        val validMenuItems = menuItems.filter { item ->
                            !item.menuName.isNullOrBlank() && item.menuPrice > 0
                        }
                        
                        android.util.Log.d("ProcessingScreen", "Valid menu items: ${validMenuItems.size}")
                        validMenuItems.forEach { item ->
                            android.util.Log.d("ProcessingScreen", "Item: name=${item.menuName}, price=${item.menuPrice}")
                        }
                        
                        onOcrComplete(validMenuItems)
                        navController.navigate("operation_management_menu_confirm")
                    }
                    .onFailure { error ->
                        android.util.Log.e("ProcessingScreen", "OCR failed", error)
                        errorMessage = error.message ?: "알 수 없는 오류가 발생했습니다."
                        isProcessing = false
                    }
            }
        } else {
            android.util.Log.w("ProcessingScreen", "imageUri is null!")
            errorMessage = "이미지가 선택되지 않았습니다."
            isProcessing = false
        }
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

        if (isProcessing) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("AI가 메뉴를 분석 중입니다...", style = MaterialTheme.typography.titleMedium)
        } else if (errorMessage != null) {
            Text(
                text = "오류: $errorMessage", 
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.popBackStack() }
            ) {
                Text("다시 시도")
            }
        }
    }
}