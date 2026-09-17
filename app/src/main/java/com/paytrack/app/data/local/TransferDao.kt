package com.paytrack.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.paytrack.app.data.model.DailySummary
import com.paytrack.app.data.model.TransferTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransferTransaction): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<TransferTransaction>): List<Long>

    @Delete
    suspend fun delete(transaction: TransferTransaction)

    @Query("SELECT * FROM transfer_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransferTransaction>>

    @Query("SELECT * FROM transfer_transactions WHERE dateString = :date ORDER BY timestamp DESC")
    fun getTransactionsByDate(date: String): Flow<List<TransferTransaction>>

    @Query("SELECT * FROM transfer_transactions WHERE dateString = :date ORDER BY timestamp DESC")
    suspend fun getTransactionsByDateSync(date: String): List<TransferTransaction>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transfer_transactions WHERE dateString = :date")
    fun getDailyTotal(date: String): Flow<Double>

    @Query("SELECT COUNT(*) FROM transfer_transactions WHERE dateString = :date")
    fun getDailyCount(date: String): Flow<Int>

    @Query("""
        SELECT dateString, 
               COALESCE(SUM(amount), 0.0) AS totalAmount, 
               COALESCE(currency, 'RM') AS currency, 
               COUNT(*) AS transactionCount
        FROM transfer_transactions
        GROUP BY dateString
        ORDER BY dateString DESC
    """)
    fun getDailySummaries(): Flow<List<DailySummary>>

    @Query("SELECT EXISTS(SELECT 1 FROM transfer_transactions WHERE rawMessage = :rawMessage AND timestamp = :timestamp LIMIT 1)")
    suspend fun existsByMessageAndTimestamp(rawMessage: String, timestamp: Long): Boolean

    @Query("DELETE FROM transfer_transactions")
    suspend fun clearAll()
}
