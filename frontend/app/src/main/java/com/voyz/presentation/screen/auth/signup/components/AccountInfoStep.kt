package com.voyz.presentation.screen.reminder.signup.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountInfoStep(
    userId: String,
    password: String,
    confirmPassword: String,
    isUserIdValid: Boolean,
    isPasswordValid: Boolean,
    isPasswordMatch: Boolean,
    onUserIdChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 제목
        Column {
            Text(
                text = "계정 정보를 입력해주세요",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "로그인에 사용할 아이디와 비밀번호를 설정하세요",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
        
        // 사용자 ID
        Column {
            OutlinedTextField(
                value = userId,
                onValueChange = onUserIdChange,
                label = { Text("아이디") },
                placeholder = { Text("영문, 숫자 4자 이상") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (userId.isNotEmpty() && isUserIdValid) Color.Green else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (userId.isNotEmpty() && !isUserIdValid) Color.Red else Color.Gray.copy(alpha = 0.5f)
                ),
                isError = userId.isNotEmpty() && !isUserIdValid
            )
            
            if (userId.isNotEmpty() && !isUserIdValid) {
                Text(
                    text = "영문, 숫자 조합으로 4자 이상 입력해주세요",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
        
        // 비밀번호
        Column {
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("비밀번호") },
                placeholder = { Text("6자 이상") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible) "비밀번호 숨기기" else "비밀번호 보기"
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (password.isNotEmpty() && isPasswordValid) Color.Green else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (password.isNotEmpty() && !isPasswordValid) Color.Red else Color.Gray.copy(alpha = 0.5f)
                ),
                isError = password.isNotEmpty() && !isPasswordValid
            )
            
            if (password.isNotEmpty() && !isPasswordValid) {
                Text(
                    text = "비밀번호는 6자 이상이어야 합니다",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
        
        // 비밀번호 확인
        Column {
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text("비밀번호 확인") },
                placeholder = { Text("비밀번호를 다시 입력해주세요") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                        Icon(
                            imageVector = if (isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isConfirmPasswordVisible) "비밀번호 숨기기" else "비밀번호 보기"
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (confirmPassword.isNotEmpty() && isPasswordMatch) Color.Green else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (confirmPassword.isNotEmpty() && !isPasswordMatch) Color.Red else Color.Gray.copy(alpha = 0.5f)
                ),
                isError = confirmPassword.isNotEmpty() && !isPasswordMatch
            )
            
            if (confirmPassword.isNotEmpty() && !isPasswordMatch) {
                Text(
                    text = "비밀번호가 일치하지 않습니다",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
    }
}