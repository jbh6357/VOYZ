package com.voyz.presentation.screen.auth.signup.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.unit.DpOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager

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
    var selectedDomain by remember { mutableStateOf("gmail.com") }
    var emailPrefix by remember { mutableStateOf("") }
    var showDomainDropdown by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    val emailDomains = listOf(
        "gmail.com",
        "naver.com",
        "daum.net",
        "kakao.com",
        "outlook.com",
        "직접 입력"
    )
    
    // 이메일 조합
    LaunchedEffect(emailPrefix, selectedDomain) {
        if (emailPrefix.isNotEmpty() && selectedDomain.isNotEmpty() && selectedDomain != "직접 입력") {
            onUserIdChange("$emailPrefix@$selectedDomain")
        } else if (selectedDomain == "직접 입력") {
            onUserIdChange(emailPrefix)
        }
    }
    
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
        
        // 이메일 입력
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 이메일 아이디 입력
                OutlinedTextField(
                    value = if (selectedDomain == "직접 입력") userId else emailPrefix,
                    onValueChange = { value ->
                        if (selectedDomain == "직접 입력") {
                            onUserIdChange(value)
                        } else {
                            emailPrefix = value
                        }
                    },
                    label = { Text("이메일") },
                    placeholder = { Text(if (selectedDomain == "직접 입력") "example@email.com" else "이메일 아이디") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (selectedDomain == "직접 입력") KeyboardType.Email else KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (userId.isNotEmpty() && isUserIdValid) Color.Green else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (userId.isNotEmpty() && !isUserIdValid) Color.Red else Color.Gray.copy(alpha = 0.5f)
                    ),
                    isError = userId.isNotEmpty() && !isUserIdValid
                )
                
                if (selectedDomain != "직접 입력") {
                    Text(
                        text = "@",
                        fontSize = 16.sp,
                        modifier = Modifier.wrapContentHeight()
                    )
                }
                
                // 도메인 선택 드롭다운
                if (selectedDomain != "직접 입력") {
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedDomain,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("도메인") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            interactionSource = remember { MutableInteractionSource() }
                                .also { interactionSource ->
                                    LaunchedEffect(interactionSource) {
                                        interactionSource.interactions.collect {
                                            if (it is PressInteraction.Release) {
                                                showDomainDropdown = !showDomainDropdown
                                            }
                                        }
                                    }
                                },
                            trailingIcon = {
                                IconButton(onClick = { 
                                    focusManager.clearFocus()
                                    showDomainDropdown = !showDomainDropdown 
                                }) {
                                    Icon(
                                        imageVector = if (showDomainDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "도메인 선택"
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                        
                        DropdownMenu(
                            expanded = showDomainDropdown,
                            onDismissRequest = { showDomainDropdown = false },
                            offset = DpOffset(0.dp, 4.dp)
                        ) {
                            emailDomains.forEach { domain ->
                                DropdownMenuItem(
                                    text = { Text(domain) },
                                    onClick = {
                                        selectedDomain = domain
                                        showDomainDropdown = false
                                        if (domain == "직접 입력") {
                                            emailPrefix = ""
                                            onUserIdChange("")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            
            if (userId.isNotEmpty() && !isUserIdValid) {
                Text(
                    text = "올바른 이메일 형식으로 입력해주세요",
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
                placeholder = { Text("영문, 숫자, 특수문자 포함 8자 이상") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
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
                    text = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다",
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
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
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