package com.voyz.datas.model.dto

import java.time.LocalDate

/**
 * 리마인더 생성을 위한 DTO
 */
data class ReminderDto(
    val title: String,
    val content: String,
    val startDate: LocalDate,
    val endDate: LocalDate
) 