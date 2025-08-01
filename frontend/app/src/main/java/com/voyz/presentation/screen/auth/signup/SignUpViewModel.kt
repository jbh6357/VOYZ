package com.voyz.presentation.screen.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voyz.datas.datastore.UserPreferencesManager
import com.voyz.datas.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SignUpUiState(
    val currentStep: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistrationComplete: Boolean = false,
    
    // Step 1: 기본 정보
    val userId: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val userName: String = "",
    val userPhone: String = "",
    
    // Step 2: 매장 정보
    val storeName: String = "",
    val storeCategory: String = "",
    val storeAddress: String = "",
    
    // 유효성 검사
    val isUserIdValid: Boolean = false,
    val isPasswordValid: Boolean = false,
    val isPasswordMatch: Boolean = false,
    val isUserNameValid: Boolean = false,
    val isUserPhoneValid: Boolean = false,
    val isStoreNameValid: Boolean = false,
    val isStoreCategoryValid: Boolean = false,
    val isStoreAddressValid: Boolean = false
)

class SignUpViewModel(
    private val userRepository: UserRepository,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()
    
    fun updateUserId(userId: String) {
        _uiState.value = _uiState.value.copy(
            userId = userId,
            isUserIdValid = userId.length >= 4 && userId.matches(Regex("^[a-zA-Z0-9]+$"))
        )
    }
    
    fun updatePassword(password: String) {
        val current = _uiState.value
        _uiState.value = current.copy(
            password = password,
            isPasswordValid = password.length >= 6,
            isPasswordMatch = password == current.confirmPassword && password.isNotEmpty()
        )
    }
    
    fun updateConfirmPassword(confirmPassword: String) {
        val current = _uiState.value
        _uiState.value = current.copy(
            confirmPassword = confirmPassword,
            isPasswordMatch = current.password == confirmPassword && confirmPassword.isNotEmpty()
        )
    }
    
    fun updateUserName(userName: String) {
        _uiState.value = _uiState.value.copy(
            userName = userName,
            isUserNameValid = userName.isNotEmpty()
        )
    }
    
    fun updateUserPhone(userPhone: String) {
        _uiState.value = _uiState.value.copy(
            userPhone = userPhone,
            isUserPhoneValid = userPhone.matches(Regex("^01[0-9]-[0-9]{4}-[0-9]{4}$"))
        )
    }
    
    fun updateStoreName(storeName: String) {
        _uiState.value = _uiState.value.copy(
            storeName = storeName,
            isStoreNameValid = storeName.isNotEmpty()
        )
    }
    
    fun updateStoreCategory(storeCategory: String) {
        _uiState.value = _uiState.value.copy(
            storeCategory = storeCategory,
            isStoreCategoryValid = storeCategory.isNotEmpty()
        )
    }
    
    fun updateStoreAddress(storeAddress: String) {
        _uiState.value = _uiState.value.copy(
            storeAddress = storeAddress,
            isStoreAddressValid = storeAddress.isNotEmpty()
        )
    }
    
    fun nextStep() {
        val current = _uiState.value
        if (current.currentStep < 2) {
            _uiState.value = current.copy(currentStep = current.currentStep + 1)
        }
    }
    
    fun previousStep() {
        val current = _uiState.value
        if (current.currentStep > 0) {
            _uiState.value = current.copy(currentStep = current.currentStep - 1)
        }
    }
    
    fun canProceedToNextStep(): Boolean {
        val current = _uiState.value
        return when (current.currentStep) {
            0 -> current.isUserIdValid && current.isPasswordValid && current.isPasswordMatch
            1 -> current.isUserNameValid && current.isUserPhoneValid
            2 -> current.isStoreNameValid && current.isStoreCategoryValid && current.isStoreAddressValid
            else -> false
        }
    }
    
    fun register() {
        val current = _uiState.value
        
        if (!canProceedToNextStep()) return
        
        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true, errorMessage = null)
            
            try {
                val response = userRepository.register(
                    userId = current.userId,
                    password = current.password,
                    userName = current.userName,
                    userPhone = current.userPhone,
                    storeName = current.storeName,
                    storeCategory = current.storeCategory,
                    storeAddress = current.storeAddress
                )
                
                if (response.isSuccessful) {
                    _uiState.value = current.copy(
                        isLoading = false,
                        isRegistrationComplete = true
                    )
                } else {
                    _uiState.value = current.copy(
                        isLoading = false,
                        errorMessage = "회원가입에 실패했습니다. 다시 시도해주세요."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = current.copy(
                    isLoading = false,
                    errorMessage = "네트워크 오류가 발생했습니다. ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    companion object {
        const val TOTAL_STEPS = 3
        
        val STORE_CATEGORIES = listOf(
            "한식", "중식", "일식", "양식", "카페", 
            "치킨", "피자", "버거", "분식", "기타"
        )
    }
}