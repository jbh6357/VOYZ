package com.voyz.presentation.component.fab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun FloatingActionMenu(
    modifier: Modifier = Modifier,
    onTextPostClick: () -> Unit = {},
    onPhotoPostClick: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = tween(300),
        label = "fab_rotation"
    )

    Box(modifier = modifier) {
        // 배경 오버레이 (메뉴가 열렸을 때)
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { isExpanded = false }
                    .zIndex(8f)
            )
        }
        
        // 메뉴 아이템들
        Column(
            modifier = Modifier.zIndex(10f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            // 텍스트 게시글 작성 메뉴
            AnimatedVisibility(
                visible = isExpanded,
                enter = scaleIn(tween(200, delayMillis = 50)) + fadeIn(tween(200, delayMillis = 50)),
                exit = scaleOut(tween(150)) + fadeOut(tween(150))
            ) {
                FloatingActionMenuItem(
                    icon = Icons.Default.Edit,
                    label = "텍스트 게시글",
                    onClick = {
                        onTextPostClick()
                        isExpanded = false
                    }
                )
            }
            
            // 사진 게시글 작성 메뉴
            AnimatedVisibility(
                visible = isExpanded,
                enter = scaleIn(tween(200)) + fadeIn(tween(200)),
                exit = scaleOut(tween(150)) + fadeOut(tween(150))
            ) {
                FloatingActionMenuItem(
                    icon = Icons.Default.Create,
                    label = "사진 게시글",
                    onClick = {
                        onPhotoPostClick()
                        isExpanded = false
                    }
                )
            }
            
            // 메인 FAB
            FloatingActionButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.size(56.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (isExpanded) "메뉴 닫기" else "게시글 작성",
                    modifier = Modifier.rotate(rotationAngle),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun FloatingActionMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        // 레이블
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // 미니 FAB
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(40.dp),
            containerColor = MaterialTheme.colorScheme.secondary,
            shape = CircleShape
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSecondary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FloatingActionMenuPreview() {
    FloatingActionMenu()
}