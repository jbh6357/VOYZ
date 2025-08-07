package com.voyz.datas.model.dto

import com.google.gson.annotations.SerializedName

data class MenuItemDto(
    val menuIdx: Int? = null,
    val menuName: String,
    val menuPrice: Int,
    val menuDescription: String? = null,
    val imageUrl: String? = null,
    val category: String? = "기타"
)