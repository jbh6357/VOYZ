package com.voyz.presentation.component.sidebar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun SidebarComponent(
    isOpen: Boolean,
    onClose: () -> Unit,
    menuItems: List<SidebarMenuItem> = getDefaultMenuItems(),
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(300)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(300)
        ),
        modifier = modifier.zIndex(10f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 배경 클릭 시 사이드바 닫기
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onClose() }
            )
            
            // 사이드바 콘텐츠
            Card(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterStart),
                shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // 사이드바 헤더
                    Text(
                        text = "VOYZ",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    // 메뉴 아이템들
                    menuItems.forEach { item ->
                        SidebarMenuItem(
                            item = item,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarMenuItem(
    item: SidebarMenuItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable { item.onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 아이콘이 있으면 표시 (현재는 텍스트로만)
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun getDefaultMenuItems(): List<SidebarMenuItem> {
    return listOf(
        SidebarMenuItem(
            id = "home",
            title = "홈",
            onClick = { /* 홈 이동 로직 */ }
        ),
        SidebarMenuItem(
            id = "calendar",
            title = "캘린더",
            onClick = { /* 캘린더 이동 로직 */ }
        ),
        SidebarMenuItem(
            id = "posts",
            title = "게시글",
            onClick = { /* 게시글 목록 이동 로직 */ }
        ),
        SidebarMenuItem(
            id = "profile",
            title = "프로필",
            onClick = { /* 프로필 이동 로직 */ }
        ),
        SidebarMenuItem(
            id = "settings",
            title = "설정",
            onClick = { /* 설정 이동 로직 */ }
        )
    )
}

@Preview(showBackground = true)
@Composable
fun SidebarComponentPreview() {
    SidebarComponent(
        isOpen = true,
        onClose = {}
    )
}