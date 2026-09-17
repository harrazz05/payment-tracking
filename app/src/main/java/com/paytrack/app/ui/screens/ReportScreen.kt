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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paytrack.app.ui.theme.PositiveAmountColor
import com.paytrack.app.ui.viewmodel.MainViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedDate.collectAsState()
    val transactions by viewModel.selectedDateTransactions.collectAsState()

    val totalAmount = transactions.sumOf { it.amount }
    val count = transactions.size
    val average = if (count > 0) totalAmount / count else 0.0

    val bankBreakdown = transactions.groupBy { it.sender }
        .mapValues { (_, txns) -> txns.sumOf { it.amount } }

    fun generateReportText(): String {
        val sb = StringBuilder()
        sb.append("📊 PAYTRACK DAILY TRANSFER REPORT\n")
        sb.append("====================================\n")
        sb.append("Date: " + selectedDate + "\n")
        sb.append("Total Money Received: RM " + String.format(Locale.getDefault(), "%.2f", totalAmount) + "\n")
        sb.append("Total Transfers: " + count + "\n")
        sb.append("Average Transfer: RM " + String.format(Locale.getDefault(), "%.2f", average) + "\n")
        sb.append("====================================\n\n")

        sb.append("🏦 Breakdown by Bank / Channel:\n")
        for ((bank, amount) in bankBreakdown) {
            sb.append("- " + bank + ": RM " + String.format(Locale.getDefault(), "%.2f", amount) + "\n")
        }

        sb.append("\n📝 Itemized Transactions:\n")
        for ((index, txn) in transactions.withIndex()) {
            val payerInfo = if (!txn.notes.isNullOrBlank()) "(" + txn.notes + ")" else ""
            sb.append((index + 1).toString() + ". [" + txn.timeString + "] " + txn.sender + " " + payerInfo + ": +RM " + String.format(Locale.getDefault(), "%.2f", txn.amount) + "\n")
        }

        sb.append("\nGenerated automatically via PayTrack Android.")
        return sb.toString()
    }

    fun shareReport() {
        val reportText = generateReportText()
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, reportText)
            putExtra(Intent.EXTRA_SUBJECT, "Daily Transfer Report - " + selectedDate)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Publish & Share Daily Report")
        context.startActivity(shareIntent)
    }

    fun copyToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Daily Transfer Report", generateReportText())
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Report copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Report", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { copyToClipboard() }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Report")
                    }
                    IconButton(onClick = { shareReport() }) {
                        Icon(Icons.Default.Share, contentDescription = "Publish & Share Report")
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
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
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
                                "REPORT FOR DATE",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    selectedDate,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium
                                )
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
                            "Total money transferred to you across " + count + " transaction(s)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = { shareReport() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publish / Share Report (WhatsApp, Email, etc.)")
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
                        "No transactions recorded for this date.",
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
                    "Itemized Transactions (" + count + ")",
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
}
