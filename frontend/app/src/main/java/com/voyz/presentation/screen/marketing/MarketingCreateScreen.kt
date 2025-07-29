package com.voyz.presentation.screen.marketing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.voyz.presentation.component.topbar.CommonTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketingCreateScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetAudience by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "마케팅 생성",
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 헤더 섹션
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = "새 마케팅 캠페인",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "효과적인 마케팅 전략을 수립하세요",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // 캠페인 제목
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("캠페인 제목") },
                placeholder = { Text("예: 신제품 출시 캠페인") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 캠페인 설명
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("캠페인 설명") },
                placeholder = { Text("캠페인의 목적과 내용을 자세히 설명해주세요") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )
            
            // 타겟 고객
            OutlinedTextField(
                value = targetAudience,
                onValueChange = { targetAudience = it },
                label = { Text("타겟 고객") },
                placeholder = { Text("예: 20-30대 직장인, 주부 등") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 예산
            OutlinedTextField(
                value = budget,
                onValueChange = { budget = it },
                label = { Text("예산 (원)") },
                placeholder = { Text("예: 1,000,000") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 캠페인 기간
            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("캠페인 기간") },
                placeholder = { Text("예: 2주, 1개월 등") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 액션 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("취소")
                }
                
                Button(
                    onClick = { 
                        // TODO: 마케팅 캠페인 생성 로직
                        navController.navigateUp()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = title.isNotBlank() && description.isNotBlank()
                ) {
                    Text("생성하기")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MarketingCreateScreenPreview() {
    val navController = rememberNavController()
    MarketingCreateScreen(navController = navController)
}