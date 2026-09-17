package com.paytrack.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.paytrack.app.data.local.AppDatabase
import com.paytrack.app.data.repository.TransferRepository
import com.paytrack.app.sms.worker.DailyReportWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class PayTrackApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { TransferRepository(database.transferDao()) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleDailyReportWork()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val transferChannel = NotificationChannel(
                CHANNEL_TRANSFER_ID,
                getString(R.string.channel_transfer_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_transfer_desc)
                enableVibration(true)
            }

            val reportChannel = NotificationChannel(
                CHANNEL_REPORT_ID,
                getString(R.string.channel_report_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.channel_report_desc)
            }

            notificationManager.createNotificationChannel(transferChannel)
            notificationManager.createNotificationChannel(reportChannel)
        }
    }

    private fun scheduleDailyReportWork() {
        // Calculate delay until next 23:59 (End of Day)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 0)
        }
        val now = System.currentTimeMillis()
        var initialDelay = calendar.timeInMillis - now
        if (initialDelay <= 0) {
            // Already past 23:59 today, schedule for tomorrow
            initialDelay += TimeUnit.DAYS.toMillis(1)
        }

        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReportWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.NONE)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyReportWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }

    companion object {
        const val CHANNEL_TRANSFER_ID = "channel_transfers"
        const val CHANNEL_REPORT_ID = "channel_daily_reports"
    }
}
