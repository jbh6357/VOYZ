package com.voyz.presentation.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyz.datas.datastore.UserPreferencesManager
import com.voyz.datas.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class LoginUiState(
    val userId: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false,
    val showErrorDialog: Boolean = false,
    val loginAttempts: Int = 0,
    val isUserIdError: Boolean = false,
    val isPasswordError: Boolean = false,
    val errorType: LoginErrorType? = null,
    val isAutoLoginChecking: Boolean = true
)

enum class LoginErrorType {
    INVALID_CREDENTIALS, // 아이디/비밀번호 오류
    NETWORK_ERROR,       // 네트워크 오류
    SERVER_ERROR,        // 서버 오류
    EMPTY_FIELDS,        // 빈 필드
    TOO_MANY_ATTEMPTS    // 로그인 시도 초과
}

class LoginViewModel(
    private val userRepository: UserRepository,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    init {
        checkAutoLogin()
    }
    
    fun updateUserId(userId: String) {
        _uiState.value = _uiState.value.copy(userId = userId)
    }
    
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }
    
    fun login() {
        val current = _uiState.value
        
        // 로그인 시도 횟수 제한 (5회)
        if (current.loginAttempts >= 5) {
            _uiState.value = current.copy(
                errorMessage = "로그인 시도 횟수를 초과했습니다. 잠시 후 다시 시도해주세요.",
                errorType = LoginErrorType.TOO_MANY_ATTEMPTS,
                showErrorDialog = true
            )
            return
        }
        
        // 빈 필드 검사
        val isUserIdEmpty = current.userId.isBlank()
        val isPasswordEmpty = current.password.isBlank()
        
        if (isUserIdEmpty || isPasswordEmpty) {
            _uiState.value = current.copy(
                errorMessage = "아이디와 비밀번호를 모두 입력해주세요",
                errorType = LoginErrorType.EMPTY_FIELDS,
                isUserIdError = isUserIdEmpty,
                isPasswordError = isPasswordEmpty,
                showErrorDialog = false
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = current.copy(
                isLoading = true, 
                errorMessage = null,
                isUserIdError = false,
                isPasswordError = false,
                errorType = null
            )
            
            try {
                val response = userRepository.login(current.userId, current.password)
                
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    
                    // DataStore에 사용자 정보 저장 (실제 JWT 토큰 사용)
                    userPreferencesManager.saveLoginInfo(
                        accessToken = loginResponse.accessToken,
                        refreshToken = loginResponse.refreshToken,
                        userId = loginResponse.userId,
                        username = loginResponse.userName,
                        email = null,
                        name = loginResponse.userName
                    )
                    
                    _uiState.value = current.copy(
                        isLoading = false,
                        isLoginSuccess = true,
                        loginAttempts = 0 // 성공 시 시도 횟수 리셋
                    )
                } else {
                    val newAttempts = current.loginAttempts + 1
                    val (errorMsg, errorType) = when (response.code()) {
                        401 -> Pair(
                            "아이디 또는 비밀번호가 잘못되었습니다\n(${newAttempts}/5 시도)", 
                            LoginErrorType.INVALID_CREDENTIALS
                        )
                        500 -> Pair("서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요", LoginErrorType.SERVER_ERROR)
                        else -> Pair("로그인에 실패했습니다 (오류코드: ${response.code()})", LoginErrorType.SERVER_ERROR)
                    }
                    
                    _uiState.value = current.copy(
                        isLoading = false,
                        errorMessage = errorMsg,
                        errorType = errorType,
                        loginAttempts = newAttempts,
                        isUserIdError = errorType == LoginErrorType.INVALID_CREDENTIALS,
                        isPasswordError = errorType == LoginErrorType.INVALID_CREDENTIALS,
                        showErrorDialog = errorType != LoginErrorType.INVALID_CREDENTIALS
                    )
                }
            } catch (e: Exception) {
                _uiState.value = current.copy(
                    isLoading = false,
                    errorMessage = "네트워크 연결을 확인해주세요\n인터넷 연결 상태를 점검해보세요",
                    errorType = LoginErrorType.NETWORK_ERROR,
                    loginAttempts = current.loginAttempts + 1,
                    showErrorDialog = true
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            showErrorDialog = false,
            isUserIdError = false,
            isPasswordError = false,
            errorType = null
        )
    }
    
    fun resetLoginSuccess() {
        _uiState.value = _uiState.value.copy(isLoginSuccess = false)
    }
    
    fun dismissErrorDialog() {
        _uiState.value = _uiState.value.copy(showErrorDialog = false)
    }
    
    fun resetLoginAttempts() {
        _uiState.value = _uiState.value.copy(loginAttempts = 0)
    }
    
    /**
     * 자동 로그인 확인
     */
    private fun checkAutoLogin() {
        viewModelScope.launch {
            try {
                val accessToken = userPreferencesManager.accessToken.first()
                
                if (accessToken != null) {
                    // 저장된 토큰이 있으면 자동 로그인 시도
                    val response = userRepository.autoLogin(accessToken)
                    
                    if (response.isSuccessful && response.body() != null) {
                        _uiState.value = _uiState.value.copy(
                            isAutoLoginChecking = false,
                            isLoginSuccess = true
                        )
                    } else {
                        // 토큰이 유효하지 않으면 저장된 정보 삭제
                        userPreferencesManager.logout()
                        _uiState.value = _uiState.value.copy(isAutoLoginChecking = false)
                    }
                } else {
                    // 저장된 토큰이 없으면 일반 로그인 화면 표시
                    _uiState.value = _uiState.value.copy(isAutoLoginChecking = false)
                }
            } catch (e: Exception) {
                // 오류 발생 시 저장된 정보 삭제하고 로그인 화면 표시
                userPreferencesManager.logout()
                _uiState.value = _uiState.value.copy(isAutoLoginChecking = false)
            }
        }
    }
}