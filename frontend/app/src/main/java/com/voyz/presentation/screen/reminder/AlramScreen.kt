package com.voyz.presentation.screen.reminder


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(onBackClick: () -> Unit) {
    var isChecked by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth(),
                title = {
                    Text(text = "알림", fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0)
            )
        }
    )  { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            SuggestionCard(
                date = "7월 18일",
                title = "OO중학교 방학식",
                description = "OO중학교 방학식이 있는 날입니다.\n이벤트를 진행하기 좋은 시기입니다.",
                onAcceptClick = { /* 수락 로직 */ },
                onRejectCheckChange = { checked -> isChecked = checked },
                isRejected = isChecked
            )
        }
    }
}
@Composable
fun SuggestionCard(
    date: String,
    title: String,
    description: String,
    onAcceptClick: () -> Unit,
    onRejectCheckChange: (Boolean) -> Unit,
    isRejected: Boolean
){Card(
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(4.dp),
    modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = date,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // 설명 텍스트
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 체크박스 + 버튼
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(modifier = Modifier.size(20.dp)) {
                    Checkbox(
                        checked = isRejected,
                        onCheckedChange = onRejectCheckChange,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(0.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(text = "다음부터 제안받지 않기", fontSize = 14.sp)
            }

            Button(onClick = onAcceptClick) {
                Text("수락")
            }
        }
    }

}}


@Preview(showBackground = true)
@Composable
fun AlarmScreenPreview() {
    AlarmScreen(onBackClick = {})
}