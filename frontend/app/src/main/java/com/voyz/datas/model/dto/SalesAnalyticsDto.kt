package com.voyz.datas.model.dto

import com.google.gson.annotations.SerializedName

data class SalesAnalyticsDto(
    @SerializedName("granurality") val label: String?, // 백엔드에서 granurality로 보내고 있음
    @SerializedName("totalSales") val totalSales: Float
)
