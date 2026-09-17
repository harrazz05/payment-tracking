package com.paytrack.app.sms.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.paytrack.app.PayTrackApp
import com.paytrack.app.data.local.AppDatabase
import com.paytrack.app.data.model.TransferTransaction
import com.paytrack.app.sms.parser.SmsTransferParser
import com.paytrack.app.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        // Group messages by originating address
        val sender = messages[0].originatingAddress ?: "Unknown"
        val bodyBuilder = StringBuilder()
        var timestamp = System.currentTimeMillis()

        for (msg in messages) {
            bodyBuilder.append(msg.messageBody)
            timestamp = msg.timestampMillis
        }

        val fullBody = bodyBuilder.toString()
        val parsed = SmsTransferParser.parse(sender, fullBody)

        if (parsed.isValidTransfer && parsed.isCredit) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val dao = db.transferDao()

                    val now = Date(timestamp)
                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
                    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now)

                    // Avoid duplicate insertion
                    if (!dao.existsByMessageAndTimestamp(fullBody, timestamp)) {
                        val transaction = TransferTransaction(
                            sender = parsed.senderOrBank,
                            amount = parsed.amount,
                            currency = parsed.currency,
                            timestamp = timestamp,
                            dateString = dateStr,
                            timeString = timeStr,
                            rawMessage = fullBody,
                            notes = parsed.payerName ?: parsed.reference,
                        )
                        dao.insert(transaction)

                        // Calculate updated total for today
                        val todayTotal = dao.getDailyTotal(dateStr).first()
                        showTransferNotification(
                            context = context,
                            amount = parsed.amount,
                            currency = parsed.currency,
                            sender = parsed.senderOrBank,
                            payer = parsed.payerName,
                            todayTotal = todayTotal
                        )
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private fun showTransferNotification(
        context: Context,
        amount: Double,
        currency: String,
        sender: String,
        payer: String?,
        todayTotal: Double
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fromText = if (!payer.isNullOrBlank()) "from $payer ($sender)" else "from $sender"
        val formattedAmount = String.format(Locale.getDefault(), "%.2f", amount)
        val formattedTotal = String.format(Locale.getDefault(), "%.2f", todayTotal)

        val notification = NotificationCompat.Builder(context, PayTrackApp.CHANNEL_TRANSFER_ID)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle("Money Received: $currency $formattedAmount")
            .setContentText("Received $fromText. Today's total: $currency $formattedTotal")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Received $currency $formattedAmount $fromText.\nToday's Total: $currency $formattedTotal"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
