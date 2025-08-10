package com.voyz.datas.model.dto

import com.google.gson.annotations.SerializedName

data class MenuSalesDto(
    @SerializedName("menuName") val name: String,  // menuName을 name으로 매핑
    @SerializedName("salesPercentage") val count: Float  // salesPercentage를 count로 매핑
)