package com.voyz.presentation.fragment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voyz.datas.datastore.UserPreferencesManager
import com.voyz.datas.repository.UserRepository
import com.voyz.ui.theme.Primary

@Composable //로그인 화면 구성
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit,
    onSignupClick: () -> Unit,
    onFindClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember {
        LoginViewModel(
            userRepository = UserRepository(),
            userPreferencesManager = UserPreferencesManager(context)
        )
    }
    
    val uiState by viewModel.uiState.collectAsState()
    var isPasswordVisible by remember { mutableStateOf(false) }
    
    // 로그인 성공 처리
    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) {
            onLoginSuccess()
            viewModel.resetLoginSuccess()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("로그인", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.userId,
            onValueChange = viewModel::updateUserId,
            label = { Text("ID") },
            placeholder = { Text("아이디를 입력하세요") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (uiState.isUserIdError) Color.Red else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (uiState.isUserIdError) Color.Red else Color.Gray.copy(alpha = 0.5f)
            ),
            isError = uiState.isUserIdError
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::updatePassword,
            label = { Text("Password") },
            placeholder = { Text("비밀번호를 입력하세요") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (uiState.isPasswordError) Color.Red else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (uiState.isPasswordError) Color.Red else Color.Gray.copy(alpha = 0.5f)
            ),
            isError = uiState.isPasswordError
        )

        // 에러 메시지 (간단하게)
        uiState.errorMessage?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = viewModel::login,
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color.White
                )
            } else {
                Text("로그인")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = onSignupClick) {
                Text("회원가입")
            }
            TextButton(onClick = onFindClick) {
                Text("아이디/비밀번호 찾기")
            }
        }
        
        // 에러 다이얼로그
        if (uiState.showErrorDialog) {
            AlertDialog(
                onDismissRequest = viewModel::dismissErrorDialog,
                icon = {
                    Text(
                        text = when (uiState.errorType) {
                            LoginErrorType.NETWORK_ERROR -> "📶"
                            LoginErrorType.SERVER_ERROR -> "⚠️"
                            LoginErrorType.TOO_MANY_ATTEMPTS -> "🚫"
                            else -> "❌"
                        },
                        fontSize = 24.sp
                    )
                },
                title = {
                    Text(
                        text = when (uiState.errorType) {
                            LoginErrorType.NETWORK_ERROR -> "네트워크 오류"
                            LoginErrorType.SERVER_ERROR -> "서버 오류"
                            LoginErrorType.TOO_MANY_ATTEMPTS -> "로그인 시도 초과"
                            else -> "로그인 실패"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = uiState.errorMessage ?: "",
                            lineHeight = 20.sp
                        )
                        
                        when (uiState.errorType) {
                            LoginErrorType.NETWORK_ERROR -> {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "해결 방법:\n• Wi-Fi 연결 상태 확인\n• 모바일 데이터 확인\n• 서버 주소 확인",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    lineHeight = 16.sp
                                )
                            }
                            LoginErrorType.TOO_MANY_ATTEMPTS -> {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "5분 후 다시 시도하거나 비밀번호 찾기를 이용하세요.",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    lineHeight = 16.sp
                                )
                            }
                            else -> {}
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = viewModel::dismissErrorDialog
                    ) {
                        Text("확인", color = Primary)
                    }
                },
                dismissButton = if (uiState.errorType == LoginErrorType.TOO_MANY_ATTEMPTS) {
                    {
                        TextButton(
                            onClick = {
                                viewModel.resetLoginAttempts()
                                viewModel.dismissErrorDialog()
                            }
                        ) {
                            Text("재설정", color = Color.Gray)
                        }
                    }
                } else null
            )
        }
    }
}