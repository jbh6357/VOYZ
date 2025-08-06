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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

@Composable
fun FloatingActionMenu(
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    isOperationMode: Boolean = false,
    onMarketingCreateClick: () -> Unit = {},
    onReminderCreateClick: () -> Unit = {},
    onMenuUploadClick: () -> Unit = {},
    onMenuDirectClick: () -> Unit = {}
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = tween(300),
        label = "fab_rotation"
    )

    // 메뉴 아이템들을 하단에서부터 올라가게 배치
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        if (isOperationMode) {
            AnimatedVisibility(
                visible = isExpanded,
                enter = scaleIn(tween(200, delayMillis = 100)) + fadeIn(
                    tween(
                        200,
                        delayMillis = 100
                    )
                ),
                exit = scaleOut(tween(150)) + fadeOut(tween(150))
            ) {
                FloatingActionMenuItem(
                    icon = Icons.Default.Campaign,
                    label = "메뉴판 업로드",
                    onClick = {
                        onMenuUploadClick()
                        onExpandedChange(false)
                    }
                )
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = scaleIn(tween(200, delayMillis = 50)) + fadeIn(
                    tween(
                        200,
                        delayMillis = 50
                    )
                ),
                exit = scaleOut(tween(150)) + fadeOut(tween(150))
            ) {
                FloatingActionMenuItem(
                    icon = Icons.Default.NotificationAdd,
                    label = "메뉴 직접 입력",
                    onClick = {
                        onMenuDirectClick()
                        onExpandedChange(false)
                    }
                )
            }
        } else {
            // 마케팅 생성 메뉴
            AnimatedVisibility(
                visible = isExpanded,
                enter = scaleIn(tween(200, delayMillis = 100)) + fadeIn(
                    tween(
                        200,
                        delayMillis = 100
                    )
                ),
                exit = scaleOut(tween(150)) + fadeOut(tween(150))
            ) {
                FloatingActionMenuItem(
                    icon = Icons.Default.Campaign,
                    label = "마케팅 생성",
                    onClick = {
                        onMarketingCreateClick()
                        onExpandedChange(false)
                    }
                )
            }
            // 리마인더 생성 메뉴
            AnimatedVisibility(
                visible = isExpanded,
                enter = scaleIn(tween(200, delayMillis = 50)) + fadeIn(
                    tween(
                        200,
                        delayMillis = 50
                    )
                ),
                exit = scaleOut(tween(150)) + fadeOut(tween(150))
            ) {
                FloatingActionMenuItem(
                    icon = Icons.Default.NotificationAdd,
                    label = "리마인더 생성",
                    onClick = {
                        onReminderCreateClick()
                        onExpandedChange(false)
                    }
                )
            }

        }

            // 메인 FAB
            FloatingActionButton(
                onClick = { onExpandedChange(!isExpanded) },
                modifier = Modifier.size(56.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (isExpanded) "메뉴 닫기" else "메뉴 열기",
                    modifier = Modifier.rotate(rotationAngle),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun FloatingActionMenuPreview() {
        FloatingActionMenu()
    }
