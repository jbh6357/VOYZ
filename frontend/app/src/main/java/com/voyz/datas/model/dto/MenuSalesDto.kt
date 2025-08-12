package com.voyz.datas.model.dto

import com.google.gson.annotations.SerializedName

data class MenuSalesDto(
    @SerializedName("menuName") val name: String,         // 메뉴 이름
    @SerializedName("salesAmount") val salesAmount: Double,  // 실제 매출액
    @SerializedName("salesPercentage") val percentage: Double? = null  // 비율 (선택적)
) {
    // 기존 코드와의 호환성을 위한 계산 속성
    val count: Float get() = salesAmount.toFloat()
}