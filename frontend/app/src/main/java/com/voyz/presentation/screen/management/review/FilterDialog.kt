package com.voyz.presentation.screen.management.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FilterDialog(
    selectedType: String,
    onTypeChange: (String) -> Unit,
    selectedNationalities: Set<String>,
    onNationalityToggle: (String) -> Unit,
    selectedRatingRanges: Set<String>,
    onRatingToggle: (String) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    val nationalityOptions = listOf("한국", "미국", "일본", "중국")
    val ratingOptions = listOf("1.0~1.5", "2.0~2.5", "3.0~3.5", "4.0~4.5", "5.0~5.0")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onApply) { Text("적용") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        },
        title = { Text("리뷰 필터") },
        text = {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {

                Text("리뷰 종류", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("전체", "긍정", "부정").forEach { type ->
                        FilterToggleButton(
                            text = type,
                            selected = selectedType == type,
                            onClick = { onTypeChange(type) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("국적", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    nationalityOptions.forEach { nation ->
                        FilterToggleButton(
                            text = nation,
                            selected = selectedNationalities.contains(nation),
                            onClick = { onNationalityToggle(nation) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("평점", style = MaterialTheme.typography.titleMedium)

                val allRatingOptions = listOf("1.0~1.5", "2.0~2.5", "3.0~3.5", "4.0~4.5", "5.0~5.0")
                val isAllSelected = selectedRatingRanges.size == 5

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Checkbox(
                        checked = isAllSelected,
                        onCheckedChange = {
                            if (isAllSelected) {
                                ratingOptions.forEach { onRatingToggle(it) }
                            } else {
                                allRatingOptions.forEach { if (!selectedRatingRanges.contains(it)) onRatingToggle(it) }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("전체")
                }

                Spacer(modifier = Modifier.height(8.dp))

                ratingOptions.forEach { range ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Checkbox(
                            checked = selectedRatingRanges.contains(range),
                            onCheckedChange = { onRatingToggle(range) }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(range)
                    }
                }
            }
        }
    )
}

@Composable
fun FilterToggleButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) Color.White else Color.Black // ✅ 글자색만 조건부 변경
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(text)
    }
}
