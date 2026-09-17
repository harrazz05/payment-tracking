# 📱 PayTrack — Android Transfer SMS Reader & Daily Reporting App

PayTrack is a native Android application built using **Kotlin**, **Jetpack Compose (Material 3)**, **Room Database**, and **WorkManager**. It automatically detects, parses, and logs money transferred to you from incoming bank SMS messages and the default SMS messaging app, calculates daily totals, publishes formatted reports, and lets you review your past transfer history.

---

## 🌟 Key Features

1. **Automatic Real-Time SMS Interception**:
   - Uses an Android `BroadcastReceiver` (`SmsReceiver`) to intercept incoming SMS messages in real time.
   - When a bank SMS alerts you of incoming money, PayTrack instantly extracts the amount, bank, and payer, saves it locally in the database, and fires a rich system notification.

2. **Historical SMS Inbox Sync**:
   - Don't want to wait for new SMS? Tap **"Scan Inbox"** on the dashboard.
   - Scans past messages stored in the Android Messages app (`Telephony.Sms.Inbox`), parses any prior money transfers, prevents duplicates, and populates your historical records.

3. **Intelligent Credit vs. Debit Filter**:
   - Strict pattern matching filters out money you spent/sent (e.g. *"debited"*, *"you paid"*, *"transferred from"*).
   - Only counts incoming money transferred **to you** (e.g. *"transferred to your account"*, *"credited with"*, *"DuitNow received"*, *"received from"*).
   - Pre-configured support for Malaysian banks (Maybank, CIMB, Bank Islam, RHB, Public Bank, Hong Leong, Touch 'n Go eWallet, DuitNow) and international currencies (USD, SGD, EUR, GBP, INR, etc.).

4. **Daily Calculation & Live Dashboard**:
   - Real-time display of **Today's Total Received** and transaction count.
   - Itemized feed of today's incoming transfers with timestamps and sender badges.
   - Tap any transaction to view the raw SMS body, payer information, or delete.

5. **End-of-Day Calculation & Report Publishing**:
   - Scheduled via Android `WorkManager` to run at the end of the day (23:59).
   - Generates a polished daily summary with:
     - Total money received today
     - Number of transfers & average transfer amount
     - Breakdown by bank / payment channel
     - Full itemized transaction list
   - **One-Tap Share**: Direct integration with Android Share Sheet to publish reports to **WhatsApp, Telegram, Email, Notes, or Copy to Clipboard**.

6. **Past Days Review & History**:
   - Access the **"Past Days"** tab to review previous daily totals at a glance.
   - Tap any past date to inspect its complete breakdown and re-share the report.

7. **Built-in SMS Simulator**:
   - Interactive testing playground.
   - Select pre-configured bank SMS presets (Maybank DuitNow, CIMB, Bank Islam, TNG eWallet, USD deposit, debit test) or paste your own SMS to preview what gets extracted.
   - Easily add test transactions to the database to test all features immediately.

---

## 🏗️ Architecture & Project Structure

```
paytrack-android/
├── app/
│   ├── build.gradle.kts
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/paytrack/app/
│   │   │   │   ├── PayTrackApp.kt                     # Application setup & notification channels
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── TransferTransaction.kt     # Room entity for transactions
│   │   │   │   │   │   └── DailySummary.kt            # Model for daily totals
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── TransferDao.kt             # Room queries & daily aggregation
│   │   │   │   │   │   └── AppDatabase.kt             # Room DB singleton
│   │   │   │   │   └── repository/
│   │   │   │   │       └── TransferRepository.kt      # Repository layer
│   │   │   │   ├── sms/
│   │   │   │   │   ├── parser/
│   │   │   │   │   │   └── SmsTransferParser.kt       # Smart regex & pattern recognition
│   │   │   │   │   ├── receiver/
│   │   │   │   │   │   └── SmsReceiver.kt             # Live SMS BroadcastReceiver
│   │   │   │   │   ├── scanner/
│   │   │   │   │   │   └── SmsInboxScanner.kt         # ContentResolver inbox scanner
│   │   │   │   │   └── worker/
│   │   │   │   │       └── DailyReportWorker.kt       # WorkManager end-of-day scheduler
│   │   │   │   └── ui/
│   │   │   │       ├── MainActivity.kt                # Jetpack Compose navigation & permissions
│   │   │   │       ├── theme/                         # Material 3 typography, colors, theme
│   │   │   │       ├── viewmodel/
│   │   │   │       │   └── MainViewModel.kt           # StateFlow & UI business logic
│   │   │   │       └── screens/
│   │   │   │           ├── DashboardScreen.kt         # Today's total & transaction feed
│   │   │   │           ├── HistoryScreen.kt           # Review past days totals
│   │   │   │           ├── ReportScreen.kt            # Publish & share daily reports
│   │   │   │           └── SimulatorScreen.kt         # Test SMS sandbox
│   │   │   └── res/                                   # Strings, colors, XML configs
│   │   └── test/java/com/paytrack/app/
│   │       └── SmsTransferParserTest.kt               # Comprehensive unit test suite
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 🔒 Permissions & Privacy

PayTrack runs **100% on-device**. No bank login, passwords, or remote servers are involved.
- `RECEIVE_SMS`: Required by Android to receive live incoming messages when transfers arrive.
- `READ_SMS`: Required to read transfer SMS alerts and scan existing messages in the Messages app.
- `POST_NOTIFICATIONS`: Required on Android 13+ to notify you when money is received and when the daily report is ready.

---

## 🚀 How to Run and Build

### Option 1: Open in Android Studio (Recommended)
1. Launch **Android Studio**.
2. Select **Open** and navigate to:
   ```
   C:\Users\afiqh\.gemini\antigravity\scratch\paytrack-android
   ```
3. Wait for Gradle sync to complete.
4. Connect an Android phone or launch an emulator.
5. Click **Run ▶ (app)**.

### Option 2: Build APK via Terminal
```bash
cd C:\Users\afiqh\.gemini\antigravity\scratch\paytrack-android
./gradlew assembleDebug
```
The resulting debug APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🧪 Testing

Run the automated unit tests to verify bank format recognition, credit/debit filtering, and amount parsing:
```bash
./gradlew test
```
