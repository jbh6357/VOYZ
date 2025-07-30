package com.voyz.presentation.screen.auth.signup.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoStep(
    userName: String,
    userPhone: String,
    isUserNameValid: Boolean,
    isUserPhoneValid: Boolean,
    onUserNameChange: (String) -> Unit,
    onUserPhoneChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 제목
        Column {
            Text(
                text = "개인 정보를 입력해주세요",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "서비스 이용을 위한 기본 정보를 입력하세요",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
        
        // 사용자 이름
        Column {
            OutlinedTextField(
                value = userName,
                onValueChange = onUserNameChange,
                label = { Text("이름") },
                placeholder = { Text("실명을 입력해주세요") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (userName.isNotEmpty() && isUserNameValid) Color.Green else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (userName.isNotEmpty() && !isUserNameValid) Color.Red else Color.Gray.copy(alpha = 0.5f)
                ),
                isError = userName.isNotEmpty() && !isUserNameValid
            )
            
            if (userName.isNotEmpty() && !isUserNameValid) {
                Text(
                    text = "이름을 입력해주세요",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
        
        // 전화번호
        Column {
            OutlinedTextField(
                value = userPhone,
                onValueChange = { input ->
                    // 자동으로 하이픈 추가
                    val formatted = formatPhoneNumber(input)
                    onUserPhoneChange(formatted)
                },
                label = { Text("전화번호") },
                placeholder = { Text("010-0000-0000") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (userPhone.isNotEmpty() && isUserPhoneValid) Color.Green else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (userPhone.isNotEmpty() && !isUserPhoneValid) Color.Red else Color.Gray.copy(alpha = 0.5f)
                ),
                isError = userPhone.isNotEmpty() && !isUserPhoneValid
            )
            
            if (userPhone.isNotEmpty() && !isUserPhoneValid) {
                Text(
                    text = "올바른 전화번호 형식으로 입력해주세요 (010-0000-0000)",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
        
        // 추가 안내 정보
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "개인정보 수집 및 이용 안내",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• 입력하신 정보는 서비스 제공을 위해서만 사용됩니다\n• 개인정보는 안전하게 암호화되어 저장됩니다\n• 언제든지 개인정보 수정 및 삭제가 가능합니다",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/**
 * 전화번호 자동 포맷팅
 */
private fun formatPhoneNumber(input: String): String {
    val digitsOnly = input.filter { it.isDigit() }
    
    return when {
        digitsOnly.length <= 3 -> digitsOnly
        digitsOnly.length <= 7 -> "${digitsOnly.substring(0, 3)}-${digitsOnly.substring(3)}"
        digitsOnly.length <= 11 -> "${digitsOnly.substring(0, 3)}-${digitsOnly.substring(3, 7)}-${digitsOnly.substring(7)}"
        else -> "${digitsOnly.substring(0, 3)}-${digitsOnly.substring(3, 7)}-${digitsOnly.substring(7, 11)}"
    }
}