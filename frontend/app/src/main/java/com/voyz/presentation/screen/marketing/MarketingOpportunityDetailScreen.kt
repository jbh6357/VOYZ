package com.voyz.presentation.screen.marketing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.voyz.datas.model.MarketingOpportunity
import com.voyz.datas.model.Priority
import com.voyz.datas.repository.MarketingOpportunityRepository
import com.voyz.ui.theme.MarketingColors
import com.voyz.ui.theme.getMarketingCategoryColors
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketingOpportunityDetailScreen(
    navController: NavController,
    opportunityId: String,
    modifier: Modifier = Modifier
) {
    val opportunity = remember { MarketingOpportunityRepository.getOpportunityById(opportunityId) }
    
    if (opportunity == null) {
        // 기회를 찾을 수 없는 경우
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("마케팅 기회를 찾을 수 없습니다.")
        }
        return
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
                actions = {
                    IconButton(onClick = { /* TODO: 북마크 기능 */ }) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "북마크"
                        )
                    }
                    IconButton(onClick = { /* TODO: 공유 기능 */ }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "공유"
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
            MarketingOpportunityBottomBar(
                onCreateCampaignClick = {
                    // TODO: 캠페인 생성 화면으로 이동
                    navController.navigate("marketing_create")
                },
                onSetReminderClick = {
                    // TODO: 리마인더 생성 화면으로 이동  
                    navController.navigate("reminder_create")
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 헤더 카드
            MarketingOpportunityHeader(opportunity = opportunity)
            
            // 기회 설명
            MarketingOpportunityDescription(opportunity = opportunity)
            
            // 제안된 액션
            MarketingOpportunitySuggestions(opportunity = opportunity)
            
            // 예상 효과
            MarketingOpportunityEffects(opportunity = opportunity)
            
            // 데이터 소스 정보
            MarketingOpportunityDataSource(opportunity = opportunity)
        }
    }
}

@Composable
private fun MarketingOpportunityHeader(
    opportunity: MarketingOpportunity
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = getMarketingCategoryColors(opportunity.category).second
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 날짜와 카테고리
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = opportunity.date.format(DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = opportunity.category.emoji,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = opportunity.category.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 우선순위
                    Box(
                        modifier = Modifier
                            .background(
                                color = when (opportunity.priority) {
                                    Priority.HIGH -> MarketingColors.HighPriority
                                    Priority.MEDIUM -> MarketingColors.MediumPriority 
                                    Priority.LOW -> MarketingColors.LowPriority
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = opportunity.priority.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // 신뢰도
                    Text(
                        text = "성공률 ${(opportunity.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 제목
            Text(
                text = opportunity.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 타겟 고객
            Text(
                text = "👥 ${opportunity.targetCustomer}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MarketingOpportunityDescription(
    opportunity: MarketingOpportunity
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MarketingColors.SurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "📊 상황 분석",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = opportunity.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
        }
    }
}

@Composable
private fun MarketingOpportunitySuggestions(
    opportunity: MarketingOpportunity
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MarketingColors.PrimaryLight
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "💡 제안된 마케팅 액션",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MarketingColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = opportunity.suggestedAction,
                style = MaterialTheme.typography.bodyLarge,
                color = MarketingColors.TextPrimary,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
        }
    }
}

@Composable
private fun MarketingOpportunityEffects(
    opportunity: MarketingOpportunity
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MarketingColors.CategorySecondary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "📈 예상 효과",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MarketingColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = opportunity.expectedEffect,
                style = MaterialTheme.typography.bodyLarge,
                color = MarketingColors.TextPrimary,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
        }
    }
}

@Composable
private fun MarketingOpportunityDataSource(
    opportunity: MarketingOpportunity
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MarketingColors.Surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "📋 데이터 출처",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = opportunity.dataSource.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MarketingOpportunityBottomBar(
    onCreateCampaignClick: () -> Unit,
    onSetReminderClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
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

@Preview(showBackground = true)
@Composable
fun MarketingOpportunityDetailScreenPreview() {
    val navController = rememberNavController()
    MarketingOpportunityDetailScreen(
        navController = navController,
        opportunityId = "1"
    )
}