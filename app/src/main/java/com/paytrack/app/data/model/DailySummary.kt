package com.paytrack.app.data.model

data class DailySummary(
    val dateString: String,
    val totalAmount: Double,
    val currency: String = "RM",
    val transactionCount: Int
)
