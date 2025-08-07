package com.voyz.presentation.screen.management.review.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NationalityStatBox(
    totalNations: Int,
    koreanCount: Int,
    foreignCount: Int,
    nationalityBreakdown: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("국적 수: ${totalNations}개국", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("내국인: ${koreanCount}명 / 외국인: ${foreignCount}명", fontSize = 16.sp)

            Spacer(modifier = Modifier.height(12.dp))
            Text("외국인 국적별 분포", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(8.dp))

            nationalityBreakdown.forEach { (nation, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFFF1F1F1), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(nation.split(" ")[0], fontSize = 14.sp) // 🇺🇸
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(nation.drop(2), fontSize = 14.sp, modifier = Modifier.weight(1f)) // 미국
                    Text("${count}명", fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}
