package com.paytrack.app.sms.scanner

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.paytrack.app.data.local.AppDatabase
import com.paytrack.app.data.model.TransferTransaction
import com.paytrack.app.sms.parser.SmsTransferParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ScanResult(
    val totalScanned: Int,
    val transfersFound: Int,
    val newTransfersAdded: Int,
    val totalAmount: Double
)

object SmsInboxScanner {

    suspend fun scanInbox(context: Context): ScanResult = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        // Only scan messages from the past 24 hours
        val twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
        val selection = "${Telephony.Sms.DATE} >= ?"
        val selectionArgs = arrayOf(twentyFourHoursAgo.toString())

        val cursor = contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            Telephony.Sms.DATE + " DESC"
        ) ?: return@withContext ScanResult(0, 0, 0, 0.0)

        val db = AppDatabase.getDatabase(context)
        val dao = db.transferDao()

        var totalScanned = 0
        var transfersFound = 0
        var newTransfersAdded = 0
        var totalAmount = 0.0

        val newTransactions = mutableListOf<TransferTransaction>()

        cursor.use {
            val addressIndex = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIndex = it.getColumnIndexOrThrow(Telephony.Sms.DATE)

            while (it.moveToNext()) {
                totalScanned++
                val address = it.getString(addressIndex) ?: "Unknown"
                val body = it.getString(bodyIndex) ?: ""
                val dateMillis = it.getLong(dateIndex)

                val parsed = SmsTransferParser.parse(address, body)
                if (parsed.isValidTransfer && parsed.isCredit) {
                    transfersFound++
                    totalAmount += parsed.amount

                    // Check for duplication by Transaction ID or message & timestamp
                    val txnId = parsed.reference
                    val isDuplicate = dao.isDuplicateTransaction(body, dateMillis, txnId)

                    if (!isDuplicate) {
                        val dateObj = Date(dateMillis)
                        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dateObj)
                        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(dateObj)

                        newTransactions.add(
                            TransferTransaction(
                                sender = parsed.senderOrBank,
                                amount = parsed.amount,
                                currency = parsed.currency,
                                timestamp = dateMillis,
                                dateString = dateStr,
                                timeString = timeStr,
                                rawMessage = body,
                                transactionId = txnId,
                                notes = parsed.payerName ?: parsed.reference
                            )
                        )
                    }
                }
            }
        }

        if (newTransactions.isNotEmpty()) {
            dao.insertAll(newTransactions)
            newTransfersAdded = newTransactions.size
        }

        ScanResult(
            totalScanned = totalScanned,
            transfersFound = transfersFound,
            newTransfersAdded = newTransfersAdded,
            totalAmount = totalAmount
        )
    }
}
