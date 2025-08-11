package com.voyz.datas.model.dto

data class NationalityAnalyticsDto(
    val nationality: String,
    val count: Long,
)

data class NationalitySummaryDto(
    val localCount: Long,
    val foreignCount: Long,
    val breakdown: List<NationalityAnalyticsDto>,
)


