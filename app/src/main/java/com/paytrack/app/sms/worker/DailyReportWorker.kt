package com.paytrack.app.sms.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.paytrack.app.PayTrackApp
import com.paytrack.app.data.local.AppDatabase
import com.paytrack.app.ui.MainActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyReportWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val db = AppDatabase.getDatabase(context)
        val dao = db.transferDao()

        val transactions = dao.getTransactionsByDateSync(todayStr)
        val totalAmount = transactions.sumOf { it.amount }
        val count = transactions.size

        if (count > 0) {
            showDailyReportNotification(todayStr, totalAmount, count)
        }

        return Result.success()
    }

    private fun showDailyReportNotification(date: String, total: Double, count: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("OPEN_TAB", "REPORT")
            putExtra("REPORT_DATE", date)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedTotal = String.format(Locale.getDefault(), "%.2f", total)

        val notification = NotificationCompat.Builder(context, PayTrackApp.CHANNEL_REPORT_ID)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("Daily Transfer Report - $date")
            .setContentText("Total Received: RM $formattedTotal ($count transfers)")
            .setStyle(NotificationCompat.BigTextStyle().bigText("End-of-Day Summary for $date:\nTotal Money Received: RM $formattedTotal across $count transfer transactions.\nTap to review and publish the report."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
