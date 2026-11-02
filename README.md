# 🎮 Taplay - Production Arcade Payment System

Taplay is a complete, production-ready arcade payment ecosystem featuring:
1. **Android App (Kotlin & Jetpack Compose)**: NFC physical tag reader, Host Card Emulation (HCE Type-4 token transmitter), dynamic wallet, Razorpay test mode top-up flow, and live active session countdown timer.
2. **Spring Boot Backend (Java 17 / Spring Boot 3)**: REST transaction endpoints, embedded H2 database (compatible with Supabase / PostgreSQL), wallet balance debit, machine tag mapping, and ML fraud check integration.
3. **Python FastAPI Fraud Microservice**: Isolation Forest ML model trained on transaction patterns (amounts, time of day, frequency) serving `/predict`.

---

## 🏗️ Architecture & Flow Overview

```
+-----------------------------------------------------------------------------------+
|                                 PHYSICAL NFC FLOW                                 |
|                                                                                   |
|  [Phone Tap on Machine Tag] ---> Reads Tag Serial/UID (e.g. "04A289C2")           |
|                                         |                                         |
|                                         v                                         |
|  [Android App] ------------ HTTPS POST /api/v1/transaction/start                  |
|     - Body: { tag_id: "04A289C2", user_id: 1 }                                   |
|                                         |                                         |
|                                         v                                         |
|  [Spring Boot Backend] <---> [FastAPI Fraud Microservice] (POST /predict)         |
|     1. Maps Tag UID -> Machine Details (Cost, Name, Duration)                     |
|     2. Runs ML Fraud check                                                        |
|     3. Debits Wallet Balance in DB                                                |
|     4. Logs session & returns session_duration + countdown timer to Android App  |
|                                         |                                         |
|                                         v                                         |
|  [Android App] <----------------- Active Session Screen (Live Countdown Timer)     |
+-----------------------------------------------------------------------------------+
```

---

## 🛠️ One-Time Setup on CachyOS (Arch Linux)

Install Java OpenJDK, Maven, and Android tools using pacman:
```bash
sudo pacman -S jdk-openjdk maven android-tools
```

---

## 🚀 Step-by-Step Terminal Execution Guide

### Step 1: Launch Python FastAPI Fraud Microservice

```bash
cd /home/pracxy/Taplay/fraud_service

# Activate/Create virtual environment & start microservice
python3 -m venv venv
./venv/bin/pip install -r requirements.txt
./venv/bin/python train_model.py
./venv/bin/python main.py
```
> Server runs at `http://localhost:5000` (Health check: `http://localhost:5000/health`).

---

### Step 2: Launch Spring Boot Backend

Open terminal window #2:

```bash
cd /home/pracxy/Taplay/backend

# Run Spring Boot backend (uses embedded H2 DB with pre-seeded arcade tag 04A289C2)
./mvnw spring-boot:run
# Or if maven is installed:
mvn spring-boot:run
```
> Server runs at `http://localhost:8080` (H2 Console: `http://localhost:8080/h2-console`).

---

### Step 3: Run / Deploy Android App to USB Connected Phone

Open **Android Studio** or terminal window #3:

#### Option A: Via Android Studio (Recommended)
1. Open the project folder `/home/pracxy/Taplay` in **Android Studio**.
2. Make sure USB Debugging is enabled on your phone and selected in the top device target bar.
3. Click the green **Run ▶** button (or press `Shift + F10`) to compile and install the upgraded app directly onto your phone!

#### Option B: Via Terminal Command Line
```bash
cd /home/pracxy/Taplay

# Build and install APK directly to your USB connected device
./gradlew installDebug
```

---

## 📦 System Features Implemented

### 📱 Android Application (`app/`)
- **Physical NFC Tag Scan**: Built foreground NFC tag reader in `TapScreen.kt` capturing physical tag serial numbers.
- **HCE Card Emulation**: `TaplayApduService.kt` transmits ISO 14443-A Type 4 token (< 137 bytes).
- **Wallet & Dynamic Sync**: `WalletRepository.kt` handles local DataStore caching and syncs real balance with backend REST APIs.
- **Razorpay Test Top-Up**: Built top-up modal with preset (`₹50`, `₹100`, `₹200`, `₹500`) and custom amounts, payment method selection, and history receipt.
- **Live Countdown Timer**: `SessionActiveScreen.kt` runs live countdown driven by backend-provided `session_duration` with visual warning pulse when under 30s.
- **Dark Arcade UI**: Unified neon cyan & dark space design system across `HomeScreen`, `TapScreen`, `SessionActiveScreen`, `TransactionScreen`, and `ProfileScreen`.

### ⚡ Spring Boot Backend (`backend/`)
- **REST Endpoints**:
  - `POST /api/v1/transaction/start`: Processes NFC tag tap, runs ML fraud check, debits balance, and returns session duration + countdown.
  - `POST /api/v1/transaction/end`: Reports game completion.
  - `GET /api/v1/user/wallet`: Retrieves current wallet balance.
  - `POST /api/v1/user/topup`: Adds money to wallet.
  - `GET /api/v1/user/transactions`: Retrieves transaction history logs.
  - `GET /api/v1/machines/{tagId}`: Maps tag UID to arcade machine pricing and duration.
- **Database**: Zero-setup embedded H2 DB with auto-schema creation (`users`, `transactions`, `arcade_machines`, `fraud_alerts`).

### 🤖 Python Fraud Microservice (`fraud_service/`)
- **Isolation Forest Model**: Detects anomalous transaction amounts, midnight hours, and rapid frequencies.
- **FastAPI `/predict` Endpoint**: Integrates seamlessly with Spring Boot transaction authorization.
