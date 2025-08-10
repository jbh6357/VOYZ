package com.voyz.datas.model.dto

import com.google.gson.annotations.SerializedName

data class SalesAnalyticsDto(
    @SerializedName("label") val label: String,
    @SerializedName("totalSales") val totalSales: Float
)
