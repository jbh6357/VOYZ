package com.voyz.presentation.component.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ripple
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun SidebarComponent(
    isOpen: Boolean,
    onClose: () -> Unit,
    navController: NavController,
    animatedOffset: Float = 0f,
    currentRoute: String? = null,
    userName: String = "관리자",
    userRole: String = "Admin",
    menuItems: List<SidebarMenuItem> = getDefaultMenuItems(navController),
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
                // 사용자 프로필 섹션
                UserProfileSection(
                    userName = userName,
                    userRole = userRole,
                    onProfileClick = {
                        navController.navigate("user_profile")
                        onClose()
                    },
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // 메뉴 아이템들
                menuItems.forEachIndexed { index, item ->
                    SidebarMenuItem(
                        item = item,
                        isSelected = currentRoute == item.route,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    // 고객 관리와 설정 사이에 구분선 추가
                    if (item.id == "customer_management") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            thickness = 1.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarMenuItem(
    item: SidebarMenuItem,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
    } else {
        Color.Transparent
    }
    
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    bounded = true,
                    color = MaterialTheme.colorScheme.primary
                )
            ) { item.onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )
    }
}

@Composable
private fun UserProfileSection(
    userName: String,
    userRole: String,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    bounded = true,
                    color = MaterialTheme.colorScheme.primary
                )
            ) { onProfileClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 프로필 아바타
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
        }
        
        // 사용자 정보
        Column {
            Text(
                text = userName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = userRole,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getDefaultMenuItems(navController: NavController): List<SidebarMenuItem> {
    return listOf(
        SidebarMenuItem(
            id = "dashboard",
            title = "대시보드",
            route = "main",
            onClick = {
                navController.navigate("main")
            }
        ),
        SidebarMenuItem(
            id = "reminder",
            title = "리마인더",
            route = "reminder",
            onClick = {
                navController.navigate("reminder")
            }
        ),
        SidebarMenuItem(
            id = "operation_management",
            title = "운영 관리",
            route = "operation_management",
            onClick = {
                navController.navigate("operation_management")
            }
        ),
        SidebarMenuItem(
            id = "customer_management",
            title = "고객 관리",
            route = "customer_management",
            onClick = {
                navController.navigate("customer_management")
            }
        ),
        SidebarMenuItem(
            id = "settings",
            title = "설정",
            route = "settings",
            onClick = {
                navController.navigate("settings")
            }
        )
    )
}

@Preview(showBackground = true)
@Composable
fun SidebarComponentPreview() {
    val navController = rememberNavController()

    SidebarComponent(
        isOpen = true,
        onClose = {},
        navController = navController
    )
}