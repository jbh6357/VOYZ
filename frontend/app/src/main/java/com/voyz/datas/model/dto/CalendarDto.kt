package com.voyz.datas.model.dto

import java.time.LocalDate

/**
 * 백엔드 Marketing 엔티티에 대응하는 DTO
 */
data class MarketingDto(
    val marketingIdx: Int,
    val title: String,
    val type: String,
    val content: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: String,
    val reminderIdx: Int
)

/**
 * 백엔드 SpecialDay 엔티티에 대응하는 DTO
 */
data class SpecialDayDto(
    val sdIdx: Int,
    val name: String,
    val type: String,
    val category: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val isHoliday: Int
)

/**
 * 백엔드 SpecialDaySuggest 엔티티에 대응하는 DTO
 */
data class SpecialDaySuggestDto(
    val ssuIdx: Int,
    val title: String,
    val content: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val sm_idx: Int, // 백엔드 필드명과 일치
    val calendarIdx: Int
)

/**
 * 백엔드 DaySuggestionDto에 대응하는 DTO
 */
data class DaySuggestionDto(
    val specialDay: SpecialDayDto,
    val specialDaySuggest: SpecialDaySuggestDto?,
    val hasSuggest: Boolean = false
)