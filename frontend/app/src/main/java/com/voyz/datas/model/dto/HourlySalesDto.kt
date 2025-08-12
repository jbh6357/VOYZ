package com.voyz.datas.model.dto

import com.google.gson.annotations.SerializedName

data class HourlySalesDto(
    @SerializedName("hour") val hour: String,
    @SerializedName("totalAmount") val totalAmount: Double?,
    @SerializedName("orderCount") val orderCount: Long?
)


