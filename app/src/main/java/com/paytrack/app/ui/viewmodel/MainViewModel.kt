package com.paytrack.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.paytrack.app.PayTrackApp
import com.paytrack.app.data.model.DailySummary
import com.paytrack.app.data.model.TransferTransaction
import com.paytrack.app.sms.parser.SmsTransferParser
import com.paytrack.app.sms.scanner.ScanResult
import com.paytrack.app.sms.scanner.SmsInboxScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as PayTrackApp).repository

    val todayDateString: String = repository.getTodayDateString()

    val todayTotal: StateFlow<Double> = repository.getTodayTotal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayCount: StateFlow<Int> = repository.getTodayCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayTransactions: StateFlow<List<TransferTransaction>> = repository.getTodayTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailySummaries: StateFlow<List<DailySummary>> = repository.dailySummaries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDate = MutableStateFlow(todayDateString)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val selectedDateTransactions: StateFlow<List<TransferTransaction>> = _selectedDate
        .flatMapLatest { date -> repository.getTransactionsByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    enum class ReportType { DAILY, WEEKLY, MONTHLY, CUSTOM }

    private val _reportType = MutableStateFlow(ReportType.DAILY)
    val reportType: StateFlow<ReportType> = _reportType.asStateFlow()

    private val _customStartDate = MutableStateFlow(getDateStringOffset(-7))
    val customStartDate: StateFlow<String> = _customStartDate.asStateFlow()

    private val _customEndDate = MutableStateFlow(todayDateString)
    val customEndDate: StateFlow<String> = _customEndDate.asStateFlow()

    val reportTransactions: StateFlow<List<TransferTransaction>> = 
        combine(_reportType, _selectedDate, _customStartDate, _customEndDate) { type, selDate, cStart, cEnd ->
            Triple(type, selDate, Pair(cStart, cEnd))
        }.flatMapLatest { (type, selDate, range) ->
            when (type) {
                ReportType.DAILY -> repository.getTransactionsByDate(selDate)
                ReportType.WEEKLY -> {
                    val end = todayDateString
                    val start = getDateStringOffsetFrom(end, -6)
                    repository.getTransactionsByDateRange(start, end)
                }
                ReportType.MONTHLY -> {
                    val end = todayDateString
                    val start = getDateStringOffsetFrom(end, -29)
                    repository.getTransactionsByDateRange(start, end)
                }
                ReportType.CUSTOM -> {
                    repository.getTransactionsByDateRange(range.first, range.second)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setReportType(type: ReportType) {
        _reportType.value = type
    }

    fun setCustomDateRange(startDate: String, endDate: String) {
        val minDateStr = getDateStringOffset(-90)
        val finalStart = if (startDate < minDateStr) minDateStr else startDate
        _customStartDate.value = finalStart
        _customEndDate.value = endDate
        _reportType.value = ReportType.CUSTOM
    }

    private fun getDateStringOffset(daysOffset: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, daysOffset)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    private fun getDateStringOffsetFrom(baseDateStr: String, daysOffset: Int): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(baseDateStr) ?: Date()
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.DAY_OF_YEAR, daysOffset)
        return sdf.format(cal.time)
    }

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanResult = MutableStateFlow<ScanResult?>(null)
    val scanResult: StateFlow<ScanResult?> = _scanResult.asStateFlow()

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    fun scanInbox() {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val result = SmsInboxScanner.scanInbox(getApplication())
                _scanResult.value = result
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun clearScanResult() {
        _scanResult.value = null
    }

    fun insertSimulatedTransaction(
        sender: String,
        amount: Double,
        currency: String = "RM",
        payer: String? = null,
        notes: String? = null,
        rawMessage: String
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(now))
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(now))

            val txn = TransferTransaction(
                sender = sender,
                amount = amount,
                currency = currency,
                timestamp = now,
                dateString = dateStr,
                timeString = timeStr,
                rawMessage = rawMessage,
                notes = payer ?: notes,
                isSimulated = true
            )
            repository.insert(txn)
        }
    }

    fun deleteTransaction(transaction: TransferTransaction) {
        viewModelScope.launch {
            repository.delete(transaction)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}
