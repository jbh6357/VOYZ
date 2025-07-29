package com.voyz.presentation.screen.marketing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.voyz.datas.model.dto.SpecialDaySuggestDto
import com.voyz.datas.network.ApiClient
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketingOpportunityDetailScreen(
    navController: NavController,
    ssuIdx: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var suggestion by remember { mutableStateOf<SpecialDaySuggestDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(ssuIdx) {
        coroutineScope.launch {
            try {
                isLoading = true
                val response = ApiClient.calendarApiService.getSuggestionDetail(ssuIdx)
                if (response.isSuccessful) {
                    suggestion = response.body()
                } else {
                    error = "제안을 불러올 수 없습니다. (${response.code()})"
                }
            } catch (e: Exception) {
                error = "오류가 발생했습니다: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "마케팅 제안",
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
        },
        bottomBar = {
            suggestion?.let {
                SuggestionDetailBottomBar(
                    onSetReminderClick = {
                        // 제안 내용을 리마인더 생성 폼으로 전달
                        val encodedTitle = java.net.URLEncoder.encode(it.title, "UTF-8")
                        val encodedContent = java.net.URLEncoder.encode(it.content, "UTF-8")
                        // LocalDate를 ISO 형식으로 명시적 변환 (YYYY-MM-DD)
                        val dateString = it.startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        android.util.Log.d("MarketingDetail", "Original startDate: ${it.startDate}")
                        android.util.Log.d("MarketingDetail", "Original year: ${it.startDate.year}, month: ${it.startDate.monthValue}, day: ${it.startDate.dayOfMonth}")
                        android.util.Log.d("MarketingDetail", "Converted dateString: $dateString")
                        val reminderData = "title=$encodedTitle&content=$encodedContent&date=$dateString"
                        android.util.Log.d("MarketingDetail", "Final reminderData: $reminderData")
                        navController.navigate("reminder_create?$reminderData")
                    },
                    onCreateCampaignClick = {
                        navController.navigate("marketing_create")
                    }
                )
            }
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = error!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { navController.navigateUp() }) {
                            Text("돌아가기")
                        }
                    }
                }
            }
            suggestion != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 제안 헤더
                    SuggestionHeader(suggestion = suggestion!!)
                    
                    // 제안 내용
                    SuggestionContent(suggestion = suggestion!!)
                    
                    // 제안 기간
                    SuggestionPeriod(suggestion = suggestion!!)
                }
            }
        }
    }
}

@Composable
private fun SuggestionHeader(suggestion: SpecialDaySuggestDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFC107).copy(alpha = 0.1f) // 노란색 (제안)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = suggestion.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = suggestion.startDate.format(DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // 제안 배지
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFFFFC107), // 노란색 (제안)
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "제안",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionContent(suggestion: SpecialDaySuggestDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "💡 제안 내용",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = suggestion.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SuggestionPeriod(suggestion: SpecialDaySuggestDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "📅 제안 기간",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "시작일:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = suggestion.startDate.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일")),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column {
                    Text(
                        text = "종료일:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = suggestion.endDate.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일")),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionDetailBottomBar(
    onSetReminderClick: () -> Unit,
    onCreateCampaignClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onSetReminderClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("리마인더 설정")
            }
            
            Button(
                onClick = onCreateCampaignClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("캠페인 생성")
            }
        }
    }
}