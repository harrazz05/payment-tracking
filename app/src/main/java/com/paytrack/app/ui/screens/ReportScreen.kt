package com.paytrack.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.paytrack.app.ui.theme.PositiveAmountColor
import com.paytrack.app.ui.viewmodel.MainViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val reportType by viewModel.reportType.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val customStartDate by viewModel.customStartDate.collectAsState()
    val customEndDate by viewModel.customEndDate.collectAsState()
    val transactions by viewModel.reportTransactions.collectAsState()

    var showCustomDialog by remember { mutableStateOf(false) }
    var tempStartDate by remember { mutableStateOf(customStartDate) }
    var tempEndDate by remember { mutableStateOf(customEndDate) }

    val totalAmount = transactions.sumOf { it.amount }
    val count = transactions.size

    val periodTitle = when (reportType) {
        MainViewModel.ReportType.DAILY -> "Daily Report ($selectedDate)"
        MainViewModel.ReportType.WEEKLY -> "Weekly Report (Past 7 Days)"
        MainViewModel.ReportType.MONTHLY -> "Monthly Report (Past 30 Days)"
        MainViewModel.ReportType.CUSTOM -> "Custom Range ($customStartDate to $customEndDate)"
    }

    val bankBreakdown = transactions.groupBy { it.sender }
        .mapValues { (_, txns) -> txns.sumOf { it.amount } }

    fun exportAsCsv() {
        val csvBuilder = StringBuilder()
        csvBuilder.append("PayTrack Financial Report\n")
        csvBuilder.append("Report Type,$periodTitle\n")
        csvBuilder.append("Total Amount Received (RM),${String.format(Locale.getDefault(), "%.2f", totalAmount)}\n")
        csvBuilder.append("Total Transactions,$count\n\n")
        csvBuilder.append("Date,Time,Bank/Channel,Payer/Notes,Amount (RM)\n")

        for (txn in transactions) {
            val notesEscaped = "\"${(txn.notes ?: "").replace("\"", "\"\"")}\""
            csvBuilder.append("${txn.dateString},${txn.timeString},\"${txn.sender}\",$notesEscaped,${String.format(Locale.getDefault(), "%.2f", txn.amount)}\n")
        }

        try {
            val fileName = "PayTrack_Report_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
            val file = File(context.cacheDir, fileName)
            file.writeText(csvBuilder.toString())

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "PayTrack Sheet - $periodTitle")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Export Sheet / Excel")
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun generateReportText(): String {
        val sb = StringBuilder()
        sb.append("📊 PAYTRACK FINANCIAL REPORT\n")
        sb.append("====================================\n")
        sb.append("Period: $periodTitle\n")
        sb.append("Total Money Received: RM ${String.format(Locale.getDefault(), "%.2f", totalAmount)}\n")
        sb.append("Total Transactions: $count\n")
        sb.append("====================================\n\n")

        sb.append("🏦 Breakdown by Bank / Channel:\n")
        for ((bank, amount) in bankBreakdown) {
            sb.append("- $bank: RM ${String.format(Locale.getDefault(), "%.2f", amount)}\n")
        }

        sb.append("\n📝 Itemized Transactions:\n")
        for ((index, txn) in transactions.withIndex()) {
            val payerInfo = if (!txn.notes.isNullOrBlank()) "(${txn.notes})" else ""
            sb.append("${index + 1}. [${txn.dateString} ${txn.timeString}] ${txn.sender} $payerInfo: +RM ${String.format(Locale.getDefault(), "%.2f", txn.amount)}\n")
        }

        sb.append("\nGenerated automatically via PayTrack Android.")
        return sb.toString()
    }

    fun shareReport() {
        val reportText = generateReportText()
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, reportText)
            putExtra(Intent.EXTRA_SUBJECT, "PayTrack Report - $periodTitle")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Report")
        context.startActivity(shareIntent)
    }

    fun copyToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("PayTrack Report", generateReportText())
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Report copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Reports", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { exportAsCsv() }) {
                        Icon(Icons.Default.Download, contentDescription = "Export Excel / Sheet")
                    }
                    IconButton(onClick = { copyToClipboard() }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Report")
                    }
                    IconButton(onClick = { shareReport() }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Report")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Report Type Selector Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = reportType == MainViewModel.ReportType.DAILY,
                        onClick = { viewModel.setReportType(MainViewModel.ReportType.DAILY) },
                        label = { Text("Daily") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = reportType == MainViewModel.ReportType.WEEKLY,
                        onClick = { viewModel.setReportType(MainViewModel.ReportType.WEEKLY) },
                        label = { Text("Weekly") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = reportType == MainViewModel.ReportType.MONTHLY,
                        onClick = { viewModel.setReportType(MainViewModel.ReportType.MONTHLY) },
                        label = { Text("Monthly") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = reportType == MainViewModel.ReportType.CUSTOM,
                        onClick = { 
                            tempStartDate = customStartDate
                            tempEndDate = customEndDate
                            showCustomDialog = true 
                        },
                        label = { Text("Custom") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                periodTitle.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            if (reportType == MainViewModel.ReportType.CUSTOM) {
                                IconButton(onClick = { showCustomDialog = true }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Edit Range", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "RM " + String.format(Locale.getDefault(), "%,.2f", totalAmount),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            "Total money received across $count transaction(s)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Export & Share Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { exportAsCsv() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Sheet (.CSV)")
                    }
                    OutlinedButton(
                        onClick = { shareReport() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Summary")
                    }
                }
            }

            item {
                Text(
                    "Breakdown by Bank / Channel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (bankBreakdown.isEmpty()) {
                item {
                    Text(
                        "No transactions found for this period.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(bankBreakdown.entries.toList()) { (bank, sum) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(bank, fontWeight = FontWeight.SemiBold)
                            Text(
                                "RM " + String.format(Locale.getDefault(), "%,.2f", sum),
                                fontWeight = FontWeight.Bold,
                                color = PositiveAmountColor
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "Itemized Transactions ($count)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(transactions) { txn ->
                TransactionItem(transaction = txn, onClick = {})
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Custom Date Range Dialog (allowing up to past 90 days)
    if (showCustomDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("Custom Date Range (Up to 90 days)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Enter start and end dates (YYYY-MM-DD):", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = tempStartDate,
                        onValueChange = { tempStartDate = it },
                        label = { Text("Start Date (YYYY-MM-DD)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tempEndDate,
                        onValueChange = { tempEndDate = it },
                        label = { Text("End Date (YYYY-MM-DD)") },
                        singleLine = true
                    )
                    Text(
                        "Note: You can select past transactions up to 90 days ago.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (tempStartDate.isNotBlank() && tempEndDate.isNotBlank()) {
                        viewModel.setCustomDateRange(tempStartDate, tempEndDate)
                        showCustomDialog = false
                    } else {
                        Toast.makeText(context, "Please enter valid dates", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Apply Range")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
