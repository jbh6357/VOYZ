package com.voyz.presentation.component.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SidebarComponent(
    isOpen: Boolean,
    onClose: () -> Unit,
    animatedOffset: Float = 0f,
    menuItems: List<SidebarMenuItem> = getDefaultMenuItems(),
    modifier: Modifier = Modifier
) {
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val sidebarWidth = with(density) { 280.dp.toPx() }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Gray.copy(alpha = 0.7f))
    ) {
        // 배경 터치로 사이드바 닫기
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClose() }
        )
        
        // 사이드바 콘텐츠
        Box(
            modifier = Modifier
                .width(280.dp)
                .fillMaxHeight()
                .offset(x = with(density) { (animatedOffset - sidebarWidth).toDp() })
                .background(MaterialTheme.colorScheme.surface)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (dragOffset < -sidebarWidth * 0.3f) {
                                onClose()
                            }
                            dragOffset = 0f
                        }
                    ) { _, dragAmount ->
                        val newOffset = (dragOffset + dragAmount).coerceAtMost(0f)
                        dragOffset = newOffset
                    }
                }
                .clickable { /* 사이드바 내부는 터치 이벤트 차단 */ }
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