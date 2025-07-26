package com.voyz.presentation.component

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CommonTopBar(
    onSearchClick: () -> Unit = {},
    onAlarmClick: () -> Unit ={},
    onTodayClick: () -> Unit ={},
    today: LocalDate = LocalDate.now()
){
    TopAppBar(  // 상단파 컴포저블

        //가운데 비워두기
        title ={},

        //오른쪽 아이콘 3개 정렬
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "검색"
                )
            }



            IconButton(onClick = onAlarmClick) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription ="알림"
                )
            }

            Spacer(modifier = Modifier.width(15.dp))

            OutlinedButton(
                onClick = onTodayClick, //클릭시 실행
                shape = RoundedCornerShape(8.dp), //버튼 모서리 둥글게
                border = BorderStroke(2.dp, Color.Gray), // 테두리
                contentPadding = PaddingValues(0.dp),//여백
                modifier = Modifier.width(32.dp).height(36.dp)
            ){
                Text(
                    text = "${today.dayOfMonth}", // 오늘의 '일(day)'만 추출
                    fontSize = 16.sp
                )
            }
        }
    )

}
