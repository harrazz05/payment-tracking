package com.paytrack.app.data.repository

import com.paytrack.app.data.local.TransferDao
import com.paytrack.app.data.model.DailySummary
import com.paytrack.app.data.model.TransferTransaction
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransferRepository(private val dao: TransferDao) {

    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    val allTransactions: Flow<List<TransferTransaction>> = dao.getAllTransactions()
    val dailySummaries: Flow<List<DailySummary>> = dao.getDailySummaries()

    fun getTodayTransactions(): Flow<List<TransferTransaction>> {
        return dao.getTransactionsByDate(getTodayDateString())
    }

    fun getTransactionsByDate(dateString: String): Flow<List<TransferTransaction>> {
        return dao.getTransactionsByDate(dateString)
    }

    suspend fun getTransactionsByDateSync(dateString: String): List<TransferTransaction> {
        return dao.getTransactionsByDateSync(dateString)
    }

    fun getTransactionsByDateRange(startDate: String, endDate: String): Flow<List<TransferTransaction>> {
        return dao.getTransactionsByDateRange(startDate, endDate)
    }

    suspend fun getTransactionsByDateRangeSync(startDate: String, endDate: String): List<TransferTransaction> {
        return dao.getTransactionsByDateRangeSync(startDate, endDate)
    }

    fun getTodayTotal(): Flow<Double> {
        return dao.getDailyTotal(getTodayDateString())
    }

    fun getTodayCount(): Flow<Int> {
        return dao.getDailyCount(getTodayDateString())
    }

    suspend fun insert(transaction: TransferTransaction): Long {
        return dao.insert(transaction)
    }

    suspend fun insertBatch(transactions: List<TransferTransaction>): List<Long> {
        return dao.insertAll(transactions)
    }

    suspend fun delete(transaction: TransferTransaction) {
        dao.delete(transaction)
    }

    suspend fun existsMessage(rawMessage: String, timestamp: Long): Boolean {
        return dao.existsByMessageAndTimestamp(rawMessage, timestamp)
    }

    suspend fun existsTransactionId(transactionId: String): Boolean {
        return dao.existsByTransactionId(transactionId)
    }

    suspend fun isDuplicateTransaction(rawMessage: String, timestamp: Long, transactionId: String?): Boolean {
        return dao.isDuplicateTransaction(rawMessage, timestamp, transactionId)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
