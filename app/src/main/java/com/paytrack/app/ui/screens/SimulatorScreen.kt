package com.paytrack.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paytrack.app.sms.parser.ParsedTransfer
import com.paytrack.app.sms.parser.SmsTransferParser
import com.paytrack.app.ui.theme.PositiveAmountColor
import com.paytrack.app.ui.viewmodel.MainViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulatorScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current

    val sampleMessages = listOf(
        Pair("Maybank DuitNow (Credit)", "RM 150.00 received via DuitNow from AHMAD BIN ISMAIL. Ref: MBB928374."),
        Pair("CIMB Instant Transfer (Credit)", "CIMB: RM 85.50 has been credited to your account ending 4829 from SITI NORHALIZA."),
        Pair("Bank Islam Fund Transfer", "Bank Islam: RM 250.00 transferred to your account from KHAIRUL. Txn ID: BI99281."),
        Pair("Touch 'n Go eWallet", "You have received RM 30.00 from WONG JIA WEI in your Touch 'n Go eWallet."),
        Pair("International Transfer (USD)", "Credit alert: USD 500.00 has been deposited to your account from TECH CORP."),
        Pair("Debit (Rejection Test)", "RM 45.00 debited from your account ending 1234 for payment to PETRONAS.")
    )

    var senderInput by remember { mutableStateOf("MAYBANK") }
    var messageInput by remember { mutableStateOf(sampleMessages[0].second) }
    var parseResult by remember { mutableStateOf<ParsedTransfer?>(null) }

    LaunchedEffect(Unit) {
        parseResult = SmsTransferParser.parse(senderInput, messageInput)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SMS Parser Simulator", fontWeight = FontWeight.Bold) }
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
                Text(
                    "Test Bank SMS Parser",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    "Select a preset bank SMS or paste your own message text below to test how the app extracts transfer amounts and filters incoming credits from debits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Text("Select Sample Bank Alert:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    sampleMessages.forEach { (label, text) ->
                        OutlinedButton(
                            onClick = {
                                messageInput = text
                                senderInput = if (label.contains("CIMB")) "CIMB" else if (label.contains("Bank Islam")) "BANK ISLAM" else "MAYBANK"
                                parseResult = SmsTransferParser.parse(senderInput, text)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(label, maxLines = 1)
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = senderInput,
                    onValueChange = {
                        senderInput = it
                        parseResult = SmsTransferParser.parse(senderInput, messageInput)
                    },
                    label = { Text("SMS Sender / Bank Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = messageInput,
                    onValueChange = {
                        messageInput = it
                        parseResult = SmsTransferParser.parse(senderInput, messageInput)
                    },
                    label = { Text("SMS Message Body") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                val result = parseResult
                if (result != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (result.isValidTransfer && result.isCredit)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            else
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (result.isValidTransfer) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (result.isValidTransfer) PositiveAmountColor else MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (result.isValidTransfer) "VALID INCOMING TRANSFER" else "TRANSACTION REJECTED",
                                    fontWeight = FontWeight.Bold,
                                    color = if (result.isValidTransfer) PositiveAmountColor else MaterialTheme.colorScheme.error
                                )
                            }

                            if (result.isValidTransfer) {
                                Text("Extracted Amount: " + result.currency + " " + String.format(Locale.getDefault(), "%.2f", result.amount), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("Detected Bank: " + result.senderOrBank)
                                if (!result.payerName.isNullOrBlank()) {
                                    Text("Payer / Originator: " + result.payerName)
                                }
                                if (!result.reference.isNullOrBlank()) {
                                    Text("Reference: " + result.reference)
                                }
                            } else {
                                Text("Reason: " + (result.rejectionReason ?: "Not a credit transfer"), color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }
            }

            item {
                val result = parseResult
                Button(
                    onClick = {
                        if (result != null && result.isValidTransfer && result.isCredit) {
                            viewModel.insertSimulatedTransaction(
                                sender = result.senderOrBank,
                                amount = result.amount,
                                currency = result.currency,
                                payer = result.payerName,
                                notes = result.reference,
                                rawMessage = messageInput
                            )
                            Toast.makeText(context, "Added " + result.currency + " " + result.amount + " to today's transfers!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Cannot add: Not a valid incoming transfer", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = result != null && result.isValidTransfer && result.isCredit,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add to Today's Transactions Database")
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
