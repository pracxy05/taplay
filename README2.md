# 🎮 Taplay - Comprehensive Project Report & Technical Documentation

> **Document Version:** 2.0 (Full Technical Report)  
> **Target Environment:** CachyOS (Arch Linux) · Android 14+ (Physical Device via USB) · Java 17/21 · Python 3.12+  
> **Status:** Full-Stack Integrated (Android App + Spring Boot Core + FastAPI ML Microservice)

---

## 📋 Table of Contents
1. [Executive Summary & Vision](#1-executive-summary--vision)
2. [High-Level Architecture & Data Flow](#2-high-level-architecture--data-flow)
3. [Technology Stack & Tooling](#3-technology-stack--tooling)
4. [Component Deep Dives](#4-component-deep-dives)
   - [A. Android Mobile App (Kotlin & Jetpack Compose)](#a-android-mobile-app-kotlin--jetpack-compose)
   - [B. Spring Boot Backend Engine (Java 17/21)](#b-spring-boot-backend-engine-java-1721)
   - [C. ML Fraud Detection Microservice (Python & FastAPI)](#c-ml-fraud-detection-microservice-python--fastapi)
5. [Physical Constraints & Hardware Reality](#5-physical-constraints--hardware-reality)
   - [The 137-Byte Tag Constraint](#the-137-byte-tag-constraint)
   - [The "Empty Canvas" Terminal Strategy](#the-empty-canvas-terminal-strategy)
6. [Complete Step-by-Step Execution Guide](#6-complete-step-by-step-execution-guide)
7. [API & Data Contract Reference](#7-api--data-contract-reference)
8. [Project File Map](#8-project-file-map)
9. [Future Roadmap](#9-future-roadmap)

---

## 1. Executive Summary & Vision

**Taplay** is a next-generation contactless arcade payment and game-session management platform designed to replace legacy physical tokens, swipe cards, and proprietary RFID cards in modern amusement centers.

### The Problem It Solves:
1. **Physical Friction**: Arcade patrons lose physical recharge cards, wait in queues for token kiosks, and lack visibility into real-time balances.
2. **Offline & Static Cards**: Standard arcade cards cannot run active timers, display dynamic session analytics, or verify user identity.
3. **Card Cloners & Replay Fraud**: Static RFID cards can be duplicated using cheap handheld RFID copiers, leading to revenue loss.
4. **Disjointed Top-Ups**: Users must leave gaming stations to reload cards at physical cash desks.

### The Taplay Solution:
- **Zero-Friction Tap-to-Play**: Users simply tap their smartphone against an arcade cabinet's NFC tag (or hold their phone to a terminal emulator).
- **Dual NFC Modes**: The app functions both as an **NFC Reader** (scanning cabinet tags) and as an **NFC Host Card Emulator (HCE Type-4)** (transmitting encrypted tokens under 137 bytes).
- **AI-Driven Fraud Detection**: An inline Machine Learning model evaluates every transaction in milliseconds against anomalous spending patterns, sudden midnight activity, or rapid frequency spikes.
- **In-App Top-Up Flow**: Integrated digital wallet with Razorpay test mode (UPI, Cards, NetBanking) allowing instant credit reloads.
- **Live Active Session Engine**: Real-time circular countdown timers track the game duration on the player's screen with visual alerts and remote session termination.

---

## 2. High-Level Architecture & Data Flow

```
+─────────────────────────────────────────────────────────────────────────────────────────+
|                                    TAPLAY ECOSYSTEM                                     |
+─────────────────────────────────────────────────────────────────────────────────────────+

  [ PHYSICAL CABINET ]
       ┌───────────────┐
       │ NTAG213 Chip  │  (UID: e.g. "04A289C2", <137 Bytes memory)
       └───────┬───────┘
               │  1. Physical NFC Tap (ISO 14443-A)
               ▼
  [ ANDROID CLIENT (Xiaomi 16 / USB) ]
       ┌────────────────────────────────────────────────────────┐
       │ • ReaderMode: Scans Tag UID                            │
       │ • HostApduService (HCE): Emulates Type-4 Smartcard     │
       │ • Jetpack Compose UI: Radar Animation / Game Selection │
       │ • DataStore: Local Balance & Profile Cache             │
       │ • Retrofit: HTTP REST Client                           │
       └───────────────────────┬────────────────────────────────┘
                               │  2. HTTPS POST /api/v1/transaction/start
                               │     { "tagId": "04A289C2", "userId": 1 }
                               ▼
  [ SPRING BOOT BACKEND (:8080) ]
       ┌────────────────────────────────────────────────────────┐
       │ • TransactionController: Validates incoming request    │
       │ • TransactionService: Transactional balance management │
       │ • H2 Embedded Database: Users, Machines, Ledger        │
       └───────────────┬──────────────────────────────▲─────────┘
                       │                              │
                       │ 3. POST /predict             │ 4. { is_fraud: false,
                       │    { amount, hour, freq }    │      anomaly_score: -0.12 }
                       ▼                              │
  [ PYTHON ML FRAUD SERVICE (:5000) ]                 │
       ┌──────────────────────────────────────────────┴─────────┐
       │ • FastAPI + Uvicorn Microservice                       │
       │ • Scikit-Learn Isolation Forest Model                  │
       │ • Detects: High velocity, unusual hours, spike amounts │
       └────────────────────────────────────────────────────────┘
                               │
                               │ 5. Session Approved & Balance Debited
                               ▼
  [ VIRTUAL TERMINAL / "EMPTY CANVAS" ]
       ┌────────────────────────────────────────────────────────┐
       │ • Session dispatch event (Virtual room/Canvas)         │
       │ • Future: Relay to ESP32 / Raspberry Pi Cabinet Relay  │
       └───────────────────────┬────────────────────────────────┘
                               │ 6. Response: { success: true, duration: 300s }
                               ▼
  [ ANDROID CLIENT: ACTIVE SESSION SCREEN ]
       ┌────────────────────────────────────────────────────────┐
       │ • Live Circular Countdown Timer (5:00 -> 0:00)         │
       │ • Remaining balance updated automatically              │
       │ • Session extension & early cashout options            │
       └────────────────────────────────────────────────────────┘
```

---

## 3. Technology Stack & Tooling

| Tier | Technology / Library | Version / Spec | Purpose |
| :--- | :--- | :--- | :--- |
| **Android Client** | **Kotlin** | 1.9.x | Core programming language |
| **UI Framework** | **Jetpack Compose + Material 3** | Latest BOM | Declarative cyber-arcade UI, dark theme, fluid animations |
| **Networking** | **Retrofit 2 + Gson** | 2.9.0 | REST API client for backend communication |
| **Local Storage** | **Jetpack DataStore (Preferences)** | 1.0.0 | Offline balance cache, settings, and user session |
| **NFC Reader** | **Android NFC Adapter `enableReaderMode`** | API 24+ | Low-latency physical tag UID scanning |
| **NFC Emulation** | **HostApduService (HCE)** | ISO 7816-4 / Type-4 | Emulating an NFC tag to cabinet readers with custom AID |
| **Backend Core** | **Spring Boot 3** | 3.2.5 | Enterprise REST API and transactional business logic |
| **Language (Backend)**| **Java OpenJDK** | 17 / 21 | High-concurrency backend services |
| **Persistence** | **Embedded H2 Database** | In-Memory / File | Relational database (schema 100% compatible with PostgreSQL/Supabase) |
| **ORM** | **Spring Data JPA / Hibernate** | 3.2.5 | Entity mapping for `User`, `Machine`, `Transaction` |
| **ML Microservice**| **Python + FastAPI** | Python 3.12+ | High-speed asynchronous inference microservice |
| **Machine Learning**| **Scikit-Learn (Isolation Forest)** | 1.4+ | Unsupervised anomaly & fraud detection |
| **Build Tools** | **Gradle** (App) / **Maven** (Backend) | Gradle 9.2.1 / Maven 3.9 | Automated builds, dependency management, and packaging |

---

## 4. Component Deep Dives

### A. Android Mobile App (Kotlin & Jetpack Compose)

The mobile application is built with a custom neon-arcade design system (Deep Navy `#0D0A18`, Violet `#7C3AED`, Neon Cyan `#00E5FF`, Neon Green `#10B981`).

#### Key Screens:
1. **Home Screen (`HomeScreen.kt`)**:
   - Dynamic user greeting and tier level (e.g., Level 12 Arcade Master).
   - Real-time balance card displaying both INR (`₹540`) and Arcade Credits (`54`).
   - Quick navigation: "Tap to Play", "Top Up", "Game Catalog", and "Passes".
   - Carousel of popular games and recent activity summary.
2. **Tap & Play Screen (`TapScreen.kt`)**:
   - Multi-stage state machine: `WAITING` ➔ `DETECTED` ➔ `CONFIRMED` ➔ `STARTED`.
   - Continuous radar ripple animation inviting the user to bring the phone near an NFC tag.
   - Dual-detection support:
     - **Physical Hardware**: Uses `NfcAdapter.enableReaderMode` to read NTAG213 tags directly.
     - **Visual Simulation**: Includes an automatic fallback timer so testing can proceed seamlessly even without physical tags.
   - Character avatar selector (Blaze, Frost, Volt, Shadow) to tie session scores to user profile.
3. **Active Session Screen (`SessionActiveScreen.kt`)**:
   - Triggered immediately once a game session starts.
   - High-contrast circular progress ring counting down the exact session duration returned by the backend.
   - Live machine identifier (`MACH-A14`), session tokens, and game statistics.
   - Actions: "Extend Session (+5 min)" or "End Game Session".
4. **Transaction & Top-Up Screen (`TransactionScreen.kt`)**:
   - Transaction ledger listing history with badges: `GAME_PLAY`, `TOPUP`, `REFUND`.
   - Top-up modal supporting preset amounts (₹50, ₹100, ₹200, ₹500).
   - Razorpay test flow simulation: Supports UPI (`success@razorpay`), Credit/Debit cards, and Net Banking.
   - Smooth transitions from payment selection ➔ animated processing spinner ➔ digital receipt.
5. **Profile & Avatar Screen (`ProfileScreen.kt`, `AvatarScreen.kt`)**:
   - Player stats, XP progression, and unlocked cybernetic arcade avatars.

#### Dual NFC Architecture:
- **Reader Mode**: Scans NFC chips installed on arcade cabinets to identify the machine ID.
- **Host Card Emulation (`TaplayApduService.kt`)**:
  - Registered under AID `F0010203040506`.
  - Emulates an ISO 14443-4 Type 4 smartcard.
  - When polled by an external card reader with `SELECT AID`, it responds with `0x9000` (Success) and outputs a compact encrypted session token containing user ID, nonce, and timestamp.

---

### B. Spring Boot Backend Engine (Java 17/21)

Located in `/home/pracxy/Taplay/backend`, this service acts as the central brain.

#### Architectural Highlights:
- **Zero External Dependencies Required**: Uses an embedded H2 database (`jdbc:h2:file:./data/taplaydb`), meaning the backend runs immediately without installing PostgreSQL, MySQL, or Docker.
- **Production-Ready Relational Schema**:
  - `UserEntity`: Manages credits, INR balance, XP, and tier.
  - `ArcadeMachineEntity`: Maps NFC Tag UIDs to game titles, credit costs, and durations.
  - `TransactionEntity`: Records immutable audit records of all debits and top-ups with status (`COMPLETED`, `FLAGGED_FRAUD`, `FAILED`).
- **Fraud Integration via `FraudDetectionService`**:
  - Automatically queries the Python ML microservice via HTTP POST before executing any balance debit.
  - Graceful fallback: If the ML service is offline, transactions continue safely with localized validation.

---

### C. ML Fraud Detection Microservice (Python & FastAPI)

Located in `/home/pracxy/Taplay/fraud_service`, this service provides real-time fraud scoring.

#### Model Details:
- **Algorithm**: `IsolationForest` from Scikit-Learn.
- **Feature Vector**:
  1. `amount`: Cost of the transaction in credits.
  2. `hour_of_day`: 0 to 23 (flags unusual 3 AM bursts).
  3. `frequency_1h`: Number of transactions initiated in the trailing 60 minutes.
  4. `terminal_id_hash`: Cabinet hash to detect impossible physical travel speeds.
- **Output**:
  - `is_fraud`: Boolean flag (`true` if anomaly score is beyond contamination boundary).
  - `anomaly_score`: Float value indicating outlier distance.
  - `message`: Human-readable explanation.

---

## 5. Physical Constraints & Hardware Reality

### The 137-Byte Tag Constraint
Standard affordable NFC tags (like **NTAG213**) have a total user memory limit of **144 bytes**, with practical usable payload space capped at **137 bytes**.
- **How Taplay Solves This**:
  - Rather than storing large game records or session tokens on the tag, the tag stores **only** a compact identifier or machine serial (e.g., `04A289C2`).
  - When using phone-to-terminal emulation via HCE, the payload is minimized into a compressed JSON structure:
    ```json
    {"u":1,"n":849201,"t":1725849600}
    ```
    *(Total size: ~36 bytes, well below the 137-byte ceiling).*

### The "Empty Canvas" Terminal Strategy
Because physical arcade hardware cabinets with embedded NFC readers are not yet physically wired in the development environment:
- The backend treats the arcade cabinet interface as an **"Empty Canvas" / Virtual Session Receiver**.
- When the phone sends a start transaction request, the backend validates the balance, scores fraud, and dispatches the session command to this virtual canvas.
- The phone immediately enters the **Active Session** countdown screen, providing the exact user experience that will occur once physical ESP32/Raspberry Pi hardware is connected to the cabinet.

---

## 6. Complete Step-by-Step Execution Guide

To run the entire ecosystem locally on CachyOS / Linux with a physical phone connected over USB:

### Terminal 1: Launch Python ML Fraud Microservice
```bash
cd /home/pracxy/Taplay/fraud_service

# Run the FastAPI server using the dedicated virtual environment
./venv/bin/python main.py
```
*Verification:* Open `http://localhost:5000/health` in your browser.  
Response: `{"status":"UP","service":"Taplay Fraud Detection ML Service"}`

---

### Terminal 2: Launch Spring Boot Backend
```bash
cd /home/pracxy/Taplay/backend

# Compile and start the Spring Boot server
mvn spring-boot:run
```
*Verification:* 
- Spring Boot starts on port `8080`.
- H2 Console available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/taplaydb`, User: `sa`, Password: empty).

---

### Terminal 3: Configure USB Networking & Run Android App
With your phone connected via USB and USB Debugging enabled:

```bash
# 1. Forward laptop's ports to the connected phone
adb reverse tcp:8080 tcp:8080
adb reverse tcp:5000 tcp:5000

# 2. Build and install the app on your phone
cd /home/pracxy/Taplay
./gradlew installDebug
```

> **Note on Network Routing:**  
> Running `adb reverse tcp:8080 tcp:8080` allows the Android app running on your physical device to communicate directly with `http://localhost:8080/` on your laptop over the USB cable without needing to be on the same Wi-Fi network!

---

## 7. API & Data Contract Reference

### 1. Start Game Transaction
- **Endpoint:** `POST /api/v1/transaction/start`
- **Request Body:**
  ```json
  {
    "userId": 1,
    "tagId": "04A289C2"
  }
  ```
- **Success Response (200 OK):**
  ```json
  {
    "success": true,
    "message": "Game session started successfully!",
    "transactionId": "d5a892b1-4f1e-4c07-9b2f-98c4398e0a12",
    "sessionDuration": 300,
    "creditsDeducted": 10,
    "remainingBalance": 530.0,
    "machineName": "Air Hockey Blitz",
    "machineId": "MACH-A14"
  }
  ```

### 2. Wallet Balance & Details
- **Endpoint:** `GET /api/v1/user/wallet?userId=1`
- **Response (200 OK):**
  ```json
  {
    "userId": 1,
    "userName": "Praharsh",
    "balance": 540.0,
    "credits": 54,
    "tickets": 120
  }
  ```

### 3. Top-Up Wallet (Razorpay Flow)
- **Endpoint:** `POST /api/v1/user/topup`
- **Request Body:**
  ```json
  {
    "userId": 1,
    "amount": 200.0,
    "paymentId": "pay_TEST_983241"
  }
  ```

### 4. ML Fraud Anomaly Check
- **Endpoint:** `POST http://localhost:5000/predict`
- **Request Body:**
  ```json
  {
    "user_id": 1,
    "amount": 10.0,
    "hour_of_day": 14,
    "frequency_1h": 1,
    "terminal_id_hash": 4
  }
  ```
- **Response:**
  ```json
  {
    "is_fraud": false,
    "anomaly_score": 0.1423,
    "message": "Transaction pattern normal."
  }
  ```

---

## 8. Project File Map

```
/home/pracxy/Taplay/
├── README.md                           # Original summary readme
├── README2.md                          # Comprehensive Technical Project Report (This document)
├── local.properties                    # Android SDK path configuration (sdk.dir)
├── build.gradle.kts                    # Root Gradle build script
│
├── app/                                # Android Jetpack Compose Application
│   ├── build.gradle.kts                # Android dependencies (Compose, Retrofit, DataStore)
│   └── src/main/
│       ├── AndroidManifest.xml         # Permissions, NFC features, Service declarations
│       ├── java/com/taplay/app/
│       │   ├── MainActivity.kt         # Entry point and screen navigation router
│       │   ├── data/
│       │   │   ├── api/TaplayApiService.kt      # Retrofit REST interface
│       │   │   ├── model/Models.kt              # Data models (UserWallet, Transaction, etc.)
│       │   │   └── repository/WalletRepository.kt # DataStore cache + API synchronization
│       │   ├── nfc/
│       │   │   └── TaplayApduService.kt         # Host Card Emulation (HCE Type-4)
│       │   ├── screens/
│       │   │   ├── HomeScreen.kt                # Main arcade dashboard
│       │   │   ├── TapScreen.kt                 # NFC radar & tag detection engine
│       │   │   ├── SessionActiveScreen.kt       # Live circular countdown timer
│       │   │   ├── TransactionScreen.kt         # Razorpay top-up flow & history
│       │   │   ├── ProfileScreen.kt             # Player stats & settings
│       │   │   └── AvatarScreen.kt              # Character selection
│       │   └── ui/theme/                        # Dark neon theme & typography
│       └── res/xml/apduservice.xml              # NFC HCE AID registration
│
├── backend/                            # Spring Boot 3 Core Backend
│   ├── pom.xml                         # Maven dependencies (Spring Data JPA, H2, Web)
│   └── src/main/
│       ├── resources/application.properties     # H2 DB config and fraud service URL
│       └── java/com/taplay/backend/
│           ├── BackendApplication.java          # Spring Boot main class
│           ├── controller/                      # TransactionController, WalletController, etc.
│           ├── entity/                          # UserEntity, ArcadeMachineEntity, TransactionEntity
│           ├── repository/                      # Spring Data JPA repositories
│           └── service/                         # TransactionService, FraudDetectionService
│
└── fraud_service/                      # Python ML Fraud Microservice
    ├── requirements.txt                # FastAPI, Uvicorn, Scikit-Learn, Joblib, NumPy
    ├── train_model.py                  # Isolation Forest training script
    ├── isolation_forest.pkl            # Pre-trained ML model artifact
    └── main.py                         # FastAPI microservice serving /predict
```

---

## 9. Future Roadmap

1. **Hardware Cabinet Terminal**:
   - Connect an ESP32 or Raspberry Pi with an PN532 / RC522 NFC module to an arcade machine coin-drop relay.
   - When a transaction succeeds, backend sends a WebSocket / MQTT message to trip the 12V coin relay for game start.
2. **Cloud Migration (Supabase)**:
   - Switch Spring Boot's datasource from embedded H2 to a managed Supabase PostgreSQL instance by simply editing `application.properties`.
3. **Live Razorpay Production Webhooks**:
   - Replace test UPI validation with server-side Razorpay webhook signature verification for real INR top-ups.
4. **Push Notifications**:
   - Implement Firebase Cloud Messaging (FCM) to warn players 60 seconds before their active game session expires.
