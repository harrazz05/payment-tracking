package com.paytrack.app.sms.parser

import java.util.regex.Pattern

data class ParsedTransfer(
    val isValidTransfer: Boolean,
    val isCredit: Boolean,
    val amount: Double = 0.0,
    val currency: String = "RM",
    val senderOrBank: String = "Unknown",
    val payerName: String? = null,
    val reference: String? = null,
    val rejectionReason: String? = null
)

object SmsTransferParser {

    // Negative keywords that signify money leaving the account (debits/payments/withdrawals)
    private val DEBIT_PATTERNS = listOf(
        Pattern.compile("""(?i)\b(transferred from|debited|debited from|paid to|payment to|purchase of|withdrawn|deducted|sent to|transfer to a/c|charge of)\b"""),
        Pattern.compile("""(?i)\byou have sent\b"""),
        Pattern.compile("""(?i)\byou paid\b"""),
        Pattern.compile("""(?i)\bdebit alert\b""")
    )

    // Positive keywords that signify incoming transfers / money credited to the user
    private val CREDIT_PATTERNS = listOf(
        Pattern.compile("""(?i)\b(transferred to your|credited to|credited with|received|deposit|deposited|payment received|duitnow received|instant transfer received|fund transfer received|received from|received via)\b"""),
        Pattern.compile("""(?i)\bcredit alert\b"""),
        Pattern.compile("""(?i)\bhas transferred\b"""),
        Pattern.compile("""(?i)\bhas sent you\b"""),
        Pattern.compile("""(?i)\breload received\b"""),
        Pattern.compile("""(?i)\breceived.*from\b"""),
        Pattern.compile("""(?i)\bpayment\s+for\b.*?\bsuccessful\b""")
    )

    // Currency and amount matching patterns
    // e.g. RM 1,234.50, RM50.00, MYR 100, $ 25.50, USD 300, SGD 50, EUR 70.00, INR 5,000
    private val AMOUNT_PATTERNS = listOf(
        // Pattern 1: Currency prefix followed by amount, e.g. RM 50.00, RM50, MYR 120.50, $ 20.00, USD 450.00
        Pattern.compile("""(?i)(RM|MYR|USD|SGD|EUR|GBP|INR|IDR|AUD|CAD|\$)\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)"""),
        // Pattern 2: Amount followed by currency, e.g. 50.00 MYR, 120.00 RM
        Pattern.compile("""([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)\s*(?i)(RM|MYR|USD|SGD|EUR|GBP|INR)""")
    )

    // Payer extraction patterns: "from JOHN DOE", "from ALI", "by SITI", "from account ending"
    private val PAYER_PATTERNS = listOf(
        Pattern.compile("""(?i)\b(?:from|by)\s+([A-Z0-9\s./-]{2,50}?)(?:\s+(?:on|via|ref|a/c|ending|at|to|with|$)|\.)"""),
        Pattern.compile("""(?i)(?:^|[a-z0-9]+\s*:\s*)([A-Z0-9\s./-]{2,50}?)\s+has\s+(?:transferred|sent)"""),
        Pattern.compile("""(?i)DuitNow.*?from\s+([A-Za-z0-9\s]{2,50})""")
    )

    // Reference ID extraction pattern: e.g. "Ref: 123456", "Ref No: ABC123"
    private val REF_PATTERN = Pattern.compile("""(?i)(?:Ref(?:erence)?(?:\s*No\.?)?|Txn ID|Tran ID)\s*[:#]?\s*([A-Za-z0-9]+)""")

    /**
     * Parse an SMS sender and message body to detect incoming money transfers.
     */
    fun parse(sender: String, messageBody: String): ParsedTransfer {
        val cleanBody = messageBody.trim()
        val cleanSender = sender.trim()

        // 1. Check for Debit / Outgoing transfers first
        for (pattern in DEBIT_PATTERNS) {
            val matcher = pattern.matcher(cleanBody)
            if (matcher.find()) {
                // If it explicitly says "transferred to your", verify it's not "transferred from your"
                val match = try {
                    matcher.group(1)
                } catch (_: Exception) {
                    null
                } ?: matcher.group(0)
                return ParsedTransfer(
                    isValidTransfer = false,
                    isCredit = false,
                    senderOrBank = cleanSender,
                    rejectionReason = "Detected as outgoing/debit transaction ($match)"
                )
            }
        }

        // 2. Check for Credit / Incoming transfer keywords
        var isCredit = false
        for (pattern in CREDIT_PATTERNS) {
            if (pattern.matcher(cleanBody).find()) {
                isCredit = true
                break
            }
        }

        // If no explicit credit keyword found, check if it's from a known banking sender and has transfer words
        val isBankSender = isKnownBankSender(cleanSender)
        if (!isCredit && isBankSender) {
            if (cleanBody.contains("transfer", ignoreCase = true) || cleanBody.contains("credited", ignoreCase = true)) {
                isCredit = true
            }
        }

        if (!isCredit) {
            return ParsedTransfer(
                isValidTransfer = false,
                isCredit = false,
                senderOrBank = cleanSender,
                rejectionReason = "No incoming money transfer keywords detected"
            )
        }

        // 3. Extract Amount & Currency
        var extractedAmount = 0.0
        var extractedCurrency = "RM"
        var amountFound = false

        for (pattern in AMOUNT_PATTERNS) {
            val matcher = pattern.matcher(cleanBody)
            while (matcher.find()) {
                val group1 = matcher.group(1) ?: ""
                val group2 = matcher.group(2) ?: ""

                // Determine which group is currency and which is number
                val (currStr, numStr) = if (group1.matches(Regex("[0-9,.]+"))) {
                    Pair(group2, group1)
                } else {
                    Pair(group1, group2)
                }

                val parsedNum = numStr.replace(",", "").toDoubleOrNull()
                if (parsedNum != null && parsedNum > 0.0) {
                    extractedAmount = parsedNum
                    extractedCurrency = currStr.uppercase().replace("$", "USD")
                    if (extractedCurrency == "MYR") extractedCurrency = "RM"
                    amountFound = true
                    break
                }
            }
            if (amountFound) break
        }

        if (!amountFound || extractedAmount <= 0.0) {
            return ParsedTransfer(
                isValidTransfer = false,
                isCredit = true,
                senderOrBank = cleanSender,
                rejectionReason = "Could not extract valid monetary amount"
            )
        }

        // 4. Extract Payer Name (if available)
        var payerName: String? = null
        for (pattern in PAYER_PATTERNS) {
            val matcher = pattern.matcher(cleanBody)
            if (matcher.find()) {
                val candidate = matcher.group(1)?.trim()
                if (!candidate.isNullOrBlank() && !candidate.equals("your", ignoreCase = true) && !candidate.equals("a/c", ignoreCase = true)) {
                    payerName = candidate
                    break
                }
            }
        }

        // 5. Extract Reference Number
        var reference: String? = null
        val refMatcher = REF_PATTERN.matcher(cleanBody)
        if (refMatcher.find()) {
            reference = refMatcher.group(1)
        }

        // Identify Bank / Service
        val bankLabel = resolveBankLabel(cleanSender, cleanBody)

        return ParsedTransfer(
            isValidTransfer = true,
            isCredit = true,
            amount = extractedAmount,
            currency = extractedCurrency,
            senderOrBank = bankLabel,
            payerName = payerName,
            reference = reference
        )
    }

    private fun isKnownBankSender(sender: String): Boolean {
        val lower = sender.lowercase()
        return lower.contains("maybank") || lower.contains("cimb") || lower.contains("rhb") ||
               lower.contains("public") || lower.contains("islam") || lower.contains("hlb") ||
               lower.contains("ambank") || lower.contains("tng") || lower.contains("duitnow") ||
               lower.contains("bkrm") ||
               lower.contains("bank") || lower.matches(Regex("^[0-9]{4,6}$"))
    }

    private fun resolveBankLabel(sender: String, body: String): String {
        val s = (sender + " " + body).uppercase()
        return when {
            s.contains("MAYBANK") -> "Maybank"
            s.contains("CIMB") -> "CIMB Bank"
            s.contains("BANK ISLAM") -> "Bank Islam"
            s.contains("RHB") -> "RHB Bank"
            s.contains("PUBLIC BANK") || s.contains("PBEBANK") -> "Public Bank"
            s.contains("HONG LEONG") || s.contains("HLB") -> "Hong Leong Bank"
            s.contains("AMBANK") -> "AmBank"
            s.contains("BKRM") -> "Bank Rakyat"
            s.contains("TNG") || s.contains("TOUCH 'N GO") || s.contains("TOUCH N GO") -> "Touch 'n Go eWallet"
            s.contains("DUITNOW") -> "DuitNow"
            s.contains("CITIBANK") -> "Citibank"
            s.contains("HSBC") -> "HSBC Bank"
            s.contains("STANDARD CHARTERED") -> "Standard Chartered"
            sender.isNotBlank() -> sender
            else -> "Bank Transfer"
        }
    }
}
