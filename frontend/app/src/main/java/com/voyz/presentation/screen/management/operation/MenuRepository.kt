package com.voyz.presentation.screen.management.operation

import android.net.Uri
import com.voyz.datas.model.dto.MenuItemDto
import com.voyz.datas.network.ApiClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

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
    
    suspend fun createMenuWithImage(
        userId: String,
        menuName: String,
        menuPrice: Int,
        menuDescription: String,
        category: String,
        imageUri: Uri?,
        context: android.content.Context
    ): Result<String> {
        return try {
            // 이미지가 없으면 기존 API 사용
            if (imageUri == null) {
                return createMenu(userId, menuName, menuPrice, menuDescription, category)
            }
            
            // RequestBody 생성
            val userIdBody = userId.toRequestBody("text/plain".toMediaTypeOrNull())
            val menuNameBody = menuName.toRequestBody("text/plain".toMediaTypeOrNull())
            val menuPriceBody = menuPrice.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val menuDescriptionBody = menuDescription.toRequestBody("text/plain".toMediaTypeOrNull())
            val categoryBody = category.toRequestBody("text/plain".toMediaTypeOrNull())
            
            // 이미지 파일 처리 및 압축
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(imageUri)
            val file = File(context.cacheDir, "menu_image_${System.currentTimeMillis()}.jpg")
            
            inputStream?.use { input ->
                // 비트맵으로 디코딩
                val originalBitmap = BitmapFactory.decodeStream(input)
                
                // 이미지 크기 계산 (최대 1280x1280)
                val maxSize = 1280
                val width = originalBitmap.width
                val height = originalBitmap.height
                
                val newWidth: Int
                val newHeight: Int
                
                if (width > height) {
                    if (width > maxSize) {
                        newWidth = maxSize
                        newHeight = (height * maxSize) / width
                    } else {
                        newWidth = width
                        newHeight = height
                    }
                } else {
                    if (height > maxSize) {
                        newHeight = maxSize
                        newWidth = (width * maxSize) / height
                    } else {
                        newWidth = width
                        newHeight = height
                    }
                }
                
                // 리사이즈
                val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
                
                // JPEG로 압축 (품질 85%)
                file.outputStream().use { output ->
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
                }
                
                // 메모리 해제
                if (originalBitmap != scaledBitmap) {
                    originalBitmap.recycle()
                }
                scaledBitmap.recycle()
            }
            
            // Multipart 생성
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
            
            // API 호출
            val response = menuApiService.createMenuWithImage(
                userIdBody, menuNameBody, menuPriceBody, 
                menuDescriptionBody, categoryBody, imagePart
            )
            
            // 임시 파일 삭제
            file.delete()
            
            if (response.isSuccessful) {
                Result.success("메뉴 등록 완료")
            } else {
                val errorMessage = when (response.code()) {
                    409 -> "이미 등록된 메뉴입니다."
                    400 -> "잘못된 요청입니다."
                    500 -> "서버 오류가 발생했습니다."
                    else -> "메뉴 등록 실패: ${response.code()}"
                }
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
    
    suspend fun updateMenu(
        menuIdx: Int,
        menuName: String,
        menuPrice: Int,
        menuDescription: String,
        category: String
    ): Result<String> {
        return try {
            val response = menuApiService.updateMenu(menuIdx, menuName, menuPrice, menuDescription, category)
            
            if (response.isSuccessful) {
                android.util.Log.d("MenuRepository", "메뉴 수정 성공: menuIdx=$menuIdx")
                Result.success("메뉴 수정 완료")
            } else {
                android.util.Log.e("MenuRepository", "메뉴 수정 실패: ${response.code()}")
                Result.failure(Exception("메뉴 수정 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("MenuRepository", "메뉴 수정 오류", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateMenuWithImage(
        menuIdx: Int,
        menuName: String,
        menuPrice: Int,
        menuDescription: String,
        category: String,
        imageUri: Uri?,
        context: android.content.Context
    ): Result<String> {
        return try {
            // RequestBody 생성
            val menuNameBody = menuName.toRequestBody("text/plain".toMediaTypeOrNull())
            val menuPriceBody = menuPrice.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val menuDescriptionBody = menuDescription.toRequestBody("text/plain".toMediaTypeOrNull())
            val categoryBody = category.toRequestBody("text/plain".toMediaTypeOrNull())
            
            // 이미지 파일 처리
            val imagePart = imageUri?.let { uri ->
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, "menu_image_${System.currentTimeMillis()}.jpg")
                
                inputStream?.use { input ->
                    val originalBitmap = BitmapFactory.decodeStream(input)
                    val maxSize = 1280
                    val width = originalBitmap.width
                    val height = originalBitmap.height
                    
                    val newWidth: Int
                    val newHeight: Int
                    
                    if (width > height) {
                        if (width > maxSize) {
                            newWidth = maxSize
                            newHeight = (height * maxSize) / width
                        } else {
                            newWidth = width
                            newHeight = height
                        }
                    } else {
                        if (height > maxSize) {
                            newHeight = maxSize
                            newWidth = (width * maxSize) / height
                        } else {
                            newWidth = width
                            newHeight = height
                        }
                    }
                    
                    val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
                    
                    file.outputStream().use { output ->
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
                    }
                    
                    if (originalBitmap != scaledBitmap) {
                        originalBitmap.recycle()
                    }
                    scaledBitmap.recycle()
                }
                
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("image", file.name, requestFile)
                
                // 임시 파일 삭제는 API 호출 후에
                file.deleteOnExit()
                part
            }
            
            val response = menuApiService.updateMenuWithImage(
                menuIdx, menuNameBody, menuPriceBody, 
                menuDescriptionBody, categoryBody, imagePart
            )
            
            if (response.isSuccessful) {
                android.util.Log.d("MenuRepository", "이미지와 함께 메뉴 수정 성공: menuIdx=$menuIdx")
                Result.success("메뉴 수정 완료")
            } else {
                android.util.Log.e("MenuRepository", "이미지와 함께 메뉴 수정 실패: ${response.code()}")
                Result.failure(Exception("메뉴 수정 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("MenuRepository", "이미지와 함께 메뉴 수정 오류", e)
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