package com.voyz.presentation.screen.management.review.model

data class Review(
    val content: String,
    val translatedContent: String,
    val rating: Float,
    val nationality: String,
    val timestamp: String,
    val isPositive: Boolean
)