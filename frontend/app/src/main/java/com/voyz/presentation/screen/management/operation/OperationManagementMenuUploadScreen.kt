package com.voyz.presentation.screen.management.operation

import StepProgressIndicator
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.BottomAppBarDefaults.windowInsets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import coil.compose.rememberAsyncImagePainter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationManagementMenuUploadScreen(
    navController: NavController,
    onImageSelected: (Uri) -> Unit,
    imageUri: Uri?,
    onDeleteImage: () -> Unit,
    onNextStep: () -> Unit
) {
    // 디버깅용 로그
    android.util.Log.d("UploadScreen", "imageUri: $imageUri")
    val shape = RoundedCornerShape(16.dp)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = {
                    Text(
                        text = "메뉴판 업로드",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            StepProgressIndicator(
                steps = listOf("사진등록", "AI분석중", "결과"),
                currentStep = 0
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (imageUri == null) {
                // 아직 이미지 없을 때
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .drawBehind {
                            val stroke = Stroke(
                                width = 3f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f))
                            )
                            drawRoundRect(
                                color = Color.Gray,
                                topLeft = Offset(0f, 0f),
                                size = size,
                                style = stroke,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(32f, 32f)
                            )
                        }
                        .clickable {
                            // ✅ 이 안에 있어야 작동해요!
                            launcher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "이미지 추가",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                        Text("메뉴판 사진을 업로드 해주세요", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                // 이미지 선택된 경우
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "선택된 이미지",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(onClick = onDeleteImage) {
                        Text("삭제")
                    }
                    OutlinedButton(onClick = {
                        launcher.launch("image/*") // ✅ 이미지 변경
                    }) {
                        Text("변경")
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("취소")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        android.util.Log.d("UploadScreen", "Next button clicked, imageUri: $imageUri")
                        onNextStep()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = imageUri != null
                ) {
                    Text("다음 단계")
                }
            }
        }
    }
}