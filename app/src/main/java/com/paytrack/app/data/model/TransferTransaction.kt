package com.paytrack.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "transfer_transactions")
data class TransferTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String,
    val amount: Double,
    val currency: String = "RM",
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp)),
    val timeString: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp)),
    val rawMessage: String,
    val transactionId: String? = null,
    val notes: String? = null,
    val isSimulated: Boolean = false
)
