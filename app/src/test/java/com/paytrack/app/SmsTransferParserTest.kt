package com.paytrack.app

import com.paytrack.app.sms.parser.SmsTransferParser
import org.junit.Assert.*
import org.junit.Test

class SmsTransferParserTest {

    @Test
    fun testMaybankDuitNowCredit() {
        val sender = "MAYBANK"
        val body = "RM 150.00 received via DuitNow from AHMAD BIN ISMAIL. Ref: MBB928374."
        val result = SmsTransferParser.parse(sender, body)

        assertTrue("Should be valid transfer", result.isValidTransfer)
        assertTrue("Should be credit", result.isCredit)
        assertEquals(150.00, result.amount, 0.001)
        assertEquals("RM", result.currency)
        assertEquals("Maybank", result.senderOrBank)
        assertNotNull("Reference should be extracted", result.reference)
    }

    @Test
    fun testCimbCredit() {
        val sender = "CIMB"
        val body = "CIMB: RM 85.50 has been credited to your account ending 4829 from SITI."
        val result = SmsTransferParser.parse(sender, body)

        assertTrue(result.isValidTransfer)
        assertTrue(result.isCredit)
        assertEquals(85.50, result.amount, 0.001)
        assertEquals("RM", result.currency)
        assertEquals("CIMB Bank", result.senderOrBank)
    }

    @Test
    fun testBankIslamTransfer() {
        val sender = "BANK ISLAM"
        val body = "Bank Islam: RM 250.00 transferred to your account from KHAIRUL. Txn ID: BI99281."
        val result = SmsTransferParser.parse(sender, body)

        assertTrue(result.isValidTransfer)
        assertTrue(result.isCredit)
        assertEquals(250.00, result.amount, 0.001)
        assertEquals("RM", result.currency)
        assertEquals("Bank Islam", result.senderOrBank)
    }

    @Test
    fun testTouchNGoEwallet() {
        val sender = "TNG"
        val body = "You have received RM 30.00 from WONG JIA WEI in your Touch 'n Go eWallet."
        val result = SmsTransferParser.parse(sender, body)

        assertTrue(result.isValidTransfer)
        assertTrue(result.isCredit)
        assertEquals(30.00, result.amount, 0.001)
        assertEquals("Touch 'n Go eWallet", result.senderOrBank)
    }

    @Test
    fun testAmountWithCommas() {
        val sender = "63833"
        val body = "RM 1,250.00 received from TAN HOCK CHUAN into your account."
        val result = SmsTransferParser.parse(sender, body)

        assertTrue(result.isValidTransfer)
        assertTrue(result.isCredit)
        assertEquals(1250.00, result.amount, 0.001)
    }

    @Test
    fun testInternationalUsdDeposit() {
        val sender = "CHASE"
        val body = "Credit alert: USD 500.00 has been deposited to your account from TECH CORP."
        val result = SmsTransferParser.parse(sender, body)

        assertTrue(result.isValidTransfer)
        assertTrue(result.isCredit)
        assertEquals(500.00, result.amount, 0.001)
        assertEquals("USD", result.currency)
    }

    @Test
    fun testDebitRejectionPayment() {
        val sender = "MAYBANK"
        val body = "RM 45.00 debited from your account ending 1234 for payment to PETRONAS."
        val result = SmsTransferParser.parse(sender, body)

        assertFalse("Debit should not be marked as valid incoming transfer", result.isValidTransfer)
        assertFalse(result.isCredit)
    }

    @Test
    fun testDebitRejectionSentMoney() {
        val sender = "MAYBANK"
        val body = "You have sent RM 100.00 via DuitNow to ABU BAKAR."
        val result = SmsTransferParser.parse(sender, body)

        assertFalse("Sent money should be rejected", result.isValidTransfer)
    }

    @Test
    fun testOtpRejection() {
        val sender = "63833"
        val body = "Your TAC is 839201 for Maybank2u login. Do not reveal this to anyone."
        val result = SmsTransferParser.parse(sender, body)

        assertFalse("OTP message should be rejected", result.isValidTransfer)
    }

    @Test
    fun testBankRakyatTransferWithRm0Header() {
        val sender = "BKRM"
        val body = "RM0 BKRM:MOHAMMAD AFIQ HARRAZ BIN JUMANG ADHA has transferred RM0.01 to ****6165 on 15/09/2026 22:44:21."
        val result = SmsTransferParser.parse(sender, body)

        assertTrue("Should be valid transfer", result.isValidTransfer)
        assertTrue("Should be credit", result.isCredit)
        assertEquals(0.01, result.amount, 0.001)
        assertEquals("RM", result.currency)
        assertEquals("Bank Rakyat", result.senderOrBank)
        assertEquals("MOHAMMAD AFIQ HARRAZ BIN JUMANG ADHA", result.payerName)
    }

    @Test
    fun testCimbDuitNowMerchantPayment() {
        val sender = "68833"
        val body = "RM0 RM0.00 CIMB: Payment for MYR 0.70 with Tran ID: 2026091524766289 is successful. Contact Merchant Support Call Centre for any queries."
        val result = SmsTransferParser.parse(sender, body)

        assertTrue("Should be valid transfer", result.isValidTransfer)
        assertTrue("Should be credit", result.isCredit)
        assertEquals(0.70, result.amount, 0.001)
        assertEquals("RM", result.currency)
        assertEquals("CIMB Bank", result.senderOrBank)
        assertEquals("2026091524766289", result.reference)
    }
}
