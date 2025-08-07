package com.voyz.presentation.screen.management.operation

import android.net.Uri
import com.voyz.datas.model.dto.MenuItemDto
import com.voyz.datas.network.ApiClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class MenuRepository {
    
    private val menuApiService = ApiClient.menuApiService
    
    suspend fun processOcrImage(imageUri: Uri, context: android.content.Context): Result<List<MenuItemDto>> {
        return try {
            // URI를 파일로 변환
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(imageUri)
            val file = File(context.cacheDir, "temp_image.jpg")
            
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            // Multipart 요청 생성
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)
            
            // API 호출
            val response = menuApiService.processOcrImage(multipartBody)
            
            // 임시 파일 삭제
            file.delete()
            
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("OCR 처리 실패: ${response.code()}"))
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createMenu(
        userId: String,
        menuName: String,
        menuPrice: Int,
        menuDescription: String,
        category: String
    ): Result<String> {
        return try {
            val response = menuApiService.createMenu(userId, menuName, menuPrice, menuDescription, category)
            
            if (response.isSuccessful) {
                Result.success("메뉴 등록 완료")
            } else {
                // 상태 코드에 따른 구체적 에러 메시지 제공
                val errorMessage = when (response.code()) {
                    409 -> {
                        // 서버에서 보낸 에러 메시지 파싱 시도
                        try {
                            response.errorBody()?.string() ?: "이미 등록된 메뉴입니다."
                        } catch (e: Exception) {
                            "이미 등록된 메뉴입니다."
                        }
                    }
                    400 -> "잘못된 요청입니다."
                    500 -> "서버 오류가 발생했습니다."
                    else -> "메뉴 등록 실패: ${response.code()}"
                }
                android.util.Log.e("MenuRepository", "메뉴 등록 실패: $errorMessage (HTTP ${response.code()})")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMenusByUserId(userId: String): Result<List<MenuItemDto>> {
        return try {
            val response = menuApiService.getMenusByUserId(userId)
            
            if (response.isSuccessful) {
                val menus = response.body() ?: emptyList()
                android.util.Log.d("MenuRepository", "파싱된 메뉴 개수: ${menus.size}")
                menus.forEachIndexed { index, menu ->
                    android.util.Log.d("MenuRepository", "파싱된 메뉴 $index: menuName=${menu.menuName}, menuPrice=${menu.menuPrice}")
                }
                Result.success(menus)
            } else {
                Result.failure(Exception("메뉴 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("MenuRepository", "파싱 오류", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteMenu(menuIdx: Int): Result<String> {
        return try {
            val response = menuApiService.deleteMenu(menuIdx)
            
            if (response.isSuccessful) {
                android.util.Log.d("MenuRepository", "메뉴 삭제 성공: menuIdx=$menuIdx")
                Result.success("메뉴 삭제 완료")
            } else {
                android.util.Log.e("MenuRepository", "메뉴 삭제 실패: ${response.code()}")
                Result.failure(Exception("메뉴 삭제 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("MenuRepository", "메뉴 삭제 오류", e)
            Result.failure(e)
        }
    }
}