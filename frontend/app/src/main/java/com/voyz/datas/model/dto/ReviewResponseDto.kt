package com.voyz.datas.model.dto

data class ReviewResponseDto(
    val reviewIdx: Int,
    val menuIdx: Int,
    val orderIdx: Int,
    val userId: String,
    val comment: String,
    val rating: Int,
    val nationality: String,
    val language: String?,
    val createdAt: String,
    val menuName: String? = null,
)


