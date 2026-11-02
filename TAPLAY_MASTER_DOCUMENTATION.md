# 🎮 TAPLAY: Comprehensive Master Technical Documentation & System Blueprint

> **Document Classification:** Master Architectural Specification & Development Chronicle  
> **System Name:** Taplay (Contactless Arcade Payment, Gaming & Fraud Prevention Ecosystem)  
> **Target Platforms:** Android 14+ (Mobile), Wear OS 4+ (Galaxy Watch4 / Smartwatches), Java 17/21 (Spring Boot 3), Python 3.12+ (FastAPI ML)  
> **Repository:** `Taplay`  
> **Current Version:** 3.0.0 (Multi-Device Wireless Arcade Ecosystem with Playable Cabinet Emulation)  

---

## 📋 Table of Contents
1. [Executive Abstract](#1-executive-abstract)
2. [Project Vision, Theme & Design Philosophy](#2-project-vision-theme--design-philosophy)
3. [The Problem Statement & Industry Context](#3-the-problem-statement--industry-context)
4. [System Architecture & Multi-Tier Topology](#4-system-architecture--multi-tier-topology)
5. [Physical Computing & NFC Protocol Engineering](#5-physical-computing--nfc-protocol-engineering)
   - [The 137-Byte Tag Constraint & Payload Compression](#the-137-byte-tag-constraint--payload-compression)
   - [ISO 7816-4 APDU Command & Response Mechanics](#iso-7816-4-apdu-command--response-mechanics)
   - [Host Card Emulation (HCE) vs. Foreground Reader Mode](#host-card-emulation-hce-vs-foreground-reader-mode)
   - [Dual-Mode APDU Handshake (Proprietary AID + NDEF with AAR)](#dual-mode-apdu-handshake-proprietary-aid--ndef-with-aar)
   - [The "Empty Canvas" Terminal Virtualization Strategy](#the-empty-canvas-terminal-virtualization-strategy)
6. [End-to-End Dynamic Interaction Flows](#6-end-to-end-dynamic-interaction-flows)
   - [Flow A: Physical Wrist/Phone NFC Tap-to-Play](#flow-a-physical-wristphone-nfc-tap-to-play)
   - [Flow B: Multi-Device Remote Session Control (Pause / Resume / Stop)](#flow-b-multi-device-remote-session-control-pause--resume--stop)
   - [Flow C: Wallet Top-Up via Razorpay Test Flow](#flow-c-wallet-top-up-via-razorpay-test-flow)
   - [Flow D: Real-Time Machine Learning Fraud Screening](#flow-d-real-time-machine-learning-fraud-screening)
7. [Exhaustive File-by-File Breakdown](#7-exhaustive-file-by-file-breakdown)
   - [7.1 Root & Orchestration Scripts](#71-root--orchestration-scripts)
   - [7.2 Android Mobile Application (`app/`)](#72-android-mobile-application-app)
   - [7.3 Wear OS Smartwatch Application (`wear/`)](#73-wear-os-smartwatch-application-wear)
   - [7.4 Spring Boot Enterprise Backend (`backend/`)](#74-spring-boot-enterprise-backend-backend)
   - [7.5 Python Machine Learning Fraud Microservice (`fraud_service/`)](#75-python-machine-learning-fraud-microservice-fraud_service)
8. [The Engineering Evolution: How Taplay Was Developed](#8-the-engineering-evolution-how-taplay-was-developed)
   - [Phase 1: Initial Inception & Basic NFC Reader](#phase-1-initial-inception--basic-nfc-reader)
   - [Phase 2: Microservice Decoupling & ML Fraud Integration](#phase-2-microservice-decoupling--ml-fraud-integration)
   - [Phase 3: The Hardware Bottleneck & Two-Phone Virtualization Pivot](#phase-3-the-hardware-bottleneck--two-phone-virtualization-pivot)
   - [Phase 4: Designing the Playable Jetpack Compose Galaga Arcade Engine](#phase-4-designing-the-playable-jetpack-compose-galaga-arcade-engine)
   - [Phase 5: Wear OS Galaxy Watch Development & Dual-Mode HCE](#phase-5-wear-os-galaxy-watch-development--dual-mode-hce)
   - [Phase 6: The Android Network Sandbox Battle & 100% Wireless Wi-Fi Migration](#phase-6-the-android-network-sandbox-battle--100-wireless-wi-fi-migration)
   - [Phase 7: Distributed State Synchronization & Mathematical Pause Offsets](#phase-7-distributed-state-synchronization--mathematical-pause-offsets)
   - [Phase 8: Glassmorphism Web Operations Dashboard & Master Runner Automation](#phase-8-glassmorphism-web-operations-dashboard--master-runner-automation)
9. [Machine Learning & Anomaly Detection Deep Dive](#9-machine-learning--anomaly-detection-deep-dive)
10. [REST API, APDU & WebSocket/Polling Contracts](#10-rest-api-apdu--websocketpolling-contracts)
11. [Relational Data Model & Persistence Architecture](#11-relational-data-model--persistence-architecture)
12. [Step-by-Step Operations, Testing & Verification Guide](#12-step-by-step-operations-testing--verification-guide)
13. [Future Technical Roadmap & Physical Cabinet Scaling](#13-future-technical-roadmap--physical-cabinet-scaling)

---

# 1. Executive Abstract

**Taplay** is an end-to-end, full-stack contactless payment, session orchestration, and fraud prevention ecosystem engineered for the modern arcade and amusement center industry. 

Traditional amusement parks and arcades continue to depend on physical tokens, disposable swipe cards, or static proprietary RFID cards (e.g., Mifare Classic 1K). These legacy technologies suffer from severe operational drawbacks: patron queue friction, card loss, zero real-time visibility into balances or remaining playtime, and high vulnerability to handheld RFID duplicators (cloners) that cost less than $15 on commercial marketplaces.

Taplay fundamentally reimagines this experience by replacing dedicated physical cards and token dispensers with an interoperable, multi-device software suite spanning:
1. **Wear OS Smartwatches (Samsung Galaxy Watch4)**: Emulating smartcards via NFC Host Card Emulation (HCE) with custom ISO 7816-4 APDU command handlers, enabling one-touch wrist taps that trigger arcade machines while offering real-time tactile session countdowns and wrist-based game controls.
2. **Android Smart Devices (Jetpack Compose)**: Running dual roles:
   - *Consumer Mode*: A digital wallet, live session tracker, avatar customizer, transaction history ledger, and Razorpay test-mode top-up terminal.
   - *Arcade Cabinet Emulator Mode (`MACH-PHONE-2`)*: A physical hardware cabinet simulator that turns an Android phone into an interactive arcade kiosk equipped with foreground NFC reader mode and a custom-built, 60-FPS retro **Galaga Space Shooter** game written entirely in declarative Jetpack Compose Canvas.
3. **Enterprise Spring Boot 3 Backend Engine (Java 17/21)**: Serving as the transactional core, maintaining idempotent ledger balances, machine metadata mappings, concurrent session state maps, pause/resume mathematical offset compensations, and real-time operations telemetry.
4. **Unsupervised ML Fraud Microservice (Python & FastAPI)**: Integrating an in-line **Isolation Forest** anomaly detection model that inspects transaction velocity, monetary values, midnight-hour deviations, and cabinet hopping in single-digit milliseconds before authorizing balance debits.
5. **Glassmorphism Web Operations Dashboard**: A mission-control web interface serving live telemetry, SVG radial countdown rings, remote session pause/resume overrides, and transaction audit trails for arcade arcade floor managers.
6. **Unified Ecosystem Runner (`run_taplay.py`)**: An intelligent Python daemon orchestrating service boot sequences, TCP port freeing, wireless ADB device pairing across dynamic LAN subnets, and automated process lifecycle cleanup.

The entire system operates **100% wirelessly over local Wi-Fi**, eliminating tethered USB debug cables, using self-healing dynamic multi-host network discovery across Android client runtimes.

---

# 2. Project Vision, Theme & Design Philosophy

### The Visual Identity: Cyber-Arcade Neon
Arcades are visceral sensory environments characterized by neon tube glow, cathode ray tube (CRT) flicker, dark ambient spaces, and high-energy audio-visual feedback. Taplay embraces this heritage through an uncompromising **Cyber-Arcade Aesthetic**:
- **Background Depths**: Ultra-deep space navy (`#070510` and `#0D0A18`) providing near-infinite contrast on modern OLED and AMOLED displays.
- **Electric Accent Palette**:
  - *Neon Cyan (`#00F5FF` / `#00E5FF`)*: Symbolizing active NFC connectivity, digital credits, and high-speed energy.
  - *Electric Violet & Purple Glow (`#7C3AED` / `#A78BFA`)*: Used for primary interactive triggers, glass card borders, and brand elevation.
  - *Laser Emerald (`#10B981`)*: Signifying authorized transactions, verified hardware status, and top-up additions.
  - *Cyber Rose / Warning Amber (`#FF3366` / `#FFB300`)*: Communicating anomalous fraud alerts, countdown timers entering critical final 30 seconds, and emergency session stops.
- **Glassmorphism & Micro-Interactions**: Translucent glass-morphic cards (`rgba(18, 14, 34, 0.82)`) with subtle 1px border gradients, smooth ease-in-out breathing pulses, radial radar scanlines, particle explosions, and interactive drag-steering ships.

### Typography
Typography across mobile, watch, and web utilizes:
- **Orbitron & JetBrains Mono**: For machine codes (`MACH-PHONE-2`), card identifiers (`CARD-WATCH-01`), APDU hex logs, and high-contrast digital countdown clocks.
- **Outfit**: A modern geometric sans-serif delivering clean legibility on small smartwatch displays and high-density mobile viewports.

### Core Philosophy
1. **Zero Patron Friction**: No searching for tokens or queuing at recharge desks. Gamers walk up to any cabinet, tap their wrist or phone, and start playing immediately.
2. **Transparent Game Telemetry**: Players always know exactly how many seconds remain in their paid session, with dynamic haptic pulses as time winds down.
3. **Decoupled Hardware Agnosticism**: Whether communicating with a physical ESP32 relay module, an NFC-enabled Android phone acting as a cabinet, or a desktop simulator, the software contracts remain identical.

---

# 3. The Problem Statement & Industry Context

Modern Family Entertainment Centers (FECs) and amusement chains (e.g., Dave & Buster's, Timezone, Round1) rely heavily on physical RFID smartcards (Mifare Ultralight, Mifare Classic 1K, or proprietary NFC stickers). While this represented an improvement over 20th-century metal coins, it introduced severe friction:

| Dimension | Legacy Arcade Card Systems | The Taplay Architecture |
| :--- | :--- | :--- |
| **User Friction** | Players must queue at kiosks, purchase cards with deposits, and manually carry plastic cards. | Instant tap-to-play using existing wearable devices (smartwatches) or personal smartphones. |
| **Balance Visibility** | Blind spending: patrons must walk to balance scanners or check kiosk screens to see remaining tokens. | Live, synchronized digital wallet displayed simultaneously on watch screen, phone app, and web. |
| **Session Control** | Static trigger: card reader triggers coin pulse once; no live countdown or pause capability during bathroom breaks. | Dynamic two-way session engine: live circular countdown ring with wrist-based **Pause**, **Resume**, and **End Session** controls. |
| **Replay & Cloning Fraud** | Static UIDs on standard cards can be copied in seconds with handheld Proxmark or RFID cloners, allowing infinite free play. | Encrypted dynamic APDU exchanges evaluated in real time by an in-line ML Isolation Forest model detecting velocity and anomalies. |
| **Top-Up Experience** | Offline or separated: patrons must abandon games mid-session to recharge cards at central cashier stations. | Embedded in-app top-up terminal supporting UPI, debit/credit cards, and net banking with immediate balance sync. |
| **Maintenance Cost** | Thousands of plastic cards lost, demagnetized, or discarded daily, causing recurring inventory expenses and plastic waste. | 100% digital credentials stored in host memory with zero consumable physical media. |

---

# 4. System Architecture & Multi-Tier Topology

Taplay is architected as an event-driven, distributed client-server ecosystem with asynchronous telemetry:

```
+─────────────────────────────────────────────────────────────────────────────────────────────────────────────+
|                                              TAPLAY ECOSYSTEM TOPOLOGY                                      |
+─────────────────────────────────────────────────────────────────────────────────────────────────────────────+

                                      ┌──────────────────────────────────────┐
                                      │   Galaxy Watch4 (Wear OS 4 / API 34) │
                                      │   • HCE Smartcard: CARD-WATCH-01     │
                                      │   • AID: F0010203040506 + NDEF/AAR   │
                                      │   • Auto-Failover OkHttpClient       │
                                      │   • Live Circular Countdown & Remote │
                                      └──────────────────┬───────────────────┘
                                                         │
                                             1. Physical NFC Tap (ISO 14443-4)
                                             (Distance: 1-3 cm @ 13.56 MHz)
                                                         │
                                                         ▼
                                      ┌──────────────────────────────────────┐
                                      │  Android Phone (Arcade Cabinet Mode) │
                                      │  • Machine ID: MACH-PHONE-2          │
                                      │  • NfcAdapter Foreground Reader Mode │
                                      │  • Playable Compose 60FPS Galaga Game│
                                      │  • Starfield & Particle Explosion FX │
                                      └──────────────────┬───────────────────┘
                                                         │
                                                         │ 2. HTTPS POST /api/v1/transaction/start
                                                         │    Payload: { tagId: "CARD-WATCH-01", userId: 1 }
                                                         │    (Over Local Wi-Fi: 192.168.1.x:8080)
                                                         ▼
+─────────────────────────────────────────────────────────────────────────────────────────────────────────────+
|                                    SPRING BOOT 3 ENTERPRISE ENGINE (:8080)                                  |
|                                                                                                             |
|   ┌────────────────────────────────┐   ┌────────────────────────────────┐   ┌───────────────────────────┐   |
|   │     TransactionController      │   │        WalletController        │   │  SessionControlController │   |
|   │  - POST /transaction/start     │   │  - GET /user/wallet            │   │  - POST /session/pause    │   |
|   │  - POST /transaction/end       │   │  - POST /user/topup            │   │  - POST /session/resume   │   |
|   │  - GET  /session/status        │   │  - GET /user/transactions      │   │  - Remote Wrist Overrides │   |
|   └───────────────┬────────────────┘   └────────────────┬───────────────┘   └─────────────┬─────────────┘   |
|                   │                                     │                                 │                 |
|                   ▼                                     ▼                                 ▼                 |
|   ┌─────────────────────────────────────────────────────────────────────────────────────────────────┐       |
|   │                                     TransactionService                                          │       |
|   │  • ConcurrentHashMap ActiveSession Registry: [MACH-PHONE-2, CARD-WATCH-01]                     │       |
|   │  • Mathematical Pause-Offset Tracking (effectiveElapsed = pauseOffset + delta)                  │       |
|   │  • Atomic Ledger Updates & Balance Validation                                                  │       |
|   └───────────────┬─────────────────────────────────────┬───────────────────────────────────────────┘       |
|                   │                                     │                                                   |
|                   ▼                                     ▼                                                   |
|   ┌────────────────────────────────┐   ┌────────────────────────────────────────────────────────┐           |
|   │ Embedded Relational H2 Engine  │   │          FraudDetectionService (HTTP Client)           │           |
|   │  - Users, Transactions,        │   │  - 500ms Timeout Circuit Breaker                       │           |
|   │    ArcadeMachines, Alerts      │   │  - Dispatches Feature Vector to ML Engine              │           |
|   └────────────────────────────────┘   └────────────────────────┬───────────────────────────────┘           |
+─────────────────────────────────────────────────────────────────┼───────────────────────────────────────────+
                                                                  │
                                                                  │ 3. POST /predict
                                                                  │    { user_id: 1, amount: 10.0,
                                                                  │      hour_of_day: 14, frequency_1h: 1,
                                                                  │      terminal_id_hash: 4 }
                                                                  ▼
                                         ┌─────────────────────────────────────────────────┐
                                         │   Python FastAPI ML Fraud Microservice (:5000)   │
                                         │   • Scikit-Learn IsolationForest Model          │
                                         │   • Tree Anomaly Scoring Engine                 │
                                         │   • Returns: { is_fraud, anomaly_score }        │
                                         └─────────────────────────────────────────────────┘
                                                                  │
                                      ┌───────────────────────────┴────────────────────────┐
                                      │                                                    │
                                      ▼ 4. Session Authorized                              ▼ 5. Telemetry
                       ┌──────────────────────────────┐                   ┌───────────────────────────────────┐
                       │  Active Cabinet Screen       │                   │ Glassmorphic Web Dashboard (:8080)│
                       │  - Phone boots Galaga Game   │                   │ - Real-time Radial Session Clock  │
                       │  - Watch starts countdown    │                   │ - Pause/Resume State Sync         │
                       │  - Live haptic wrist timer   │                   │ - Real-time Ledger & Fraud Logs   │
                       └──────────────────────────────┘                   └───────────────────────────────────┘
```

---

# 5. Physical Computing & NFC Protocol Engineering

### The 137-Byte Tag Constraint & Payload Compression
Physical NFC deployment in real-world arcades faces strict silicon economics. The dominant low-cost tag IC in the market is the **NXP NTAG213**, which provides 180 bytes of total memory arranged into 45 pages of 4 bytes each:
- Pages 0–3: Factory serial number (UID), internal bytes, and lock bytes.
- Pages 4–39: User memory = 36 pages × 4 bytes = **144 bytes**.
- Pages 40–44: Dynamic lock bits, configuration, and password verification.

When formatted as an NFC Forum Type 2 tag with standard TLV (Type-Length-Value) wrappers, the usable user memory is exactly **137 bytes**.

**How Taplay Solved This Physical Constraint:**
Storing game states, security tokens, and user credentials on the physical tag itself is structurally impossible on NTAG213 chips. Taplay decouples the architecture:
1. **Physical Tag Role**: Acts exclusively as an immutable, low-overhead Machine Identifier (`MACH-PHONE-2` or `04A289C2`).
2. **Wrist HCE Role**: Transmits a compressed token payload under 37 bytes when queried:
   ```json
   {"u":1,"n":849201,"t":1725849600}
   ```
3. **Cloud Resolution**: The backend database maps the compact ID to full machine specs, credit costs, game rules, and session limits in sub-millisecond in-memory lookups.

### ISO 7816-4 APDU Command & Response Mechanics
When two NFC devices interact in Card Emulation mode, they communicate using ISO 7816-4 Application Protocol Data Units (APDUs). An APDU command structure consists of:
- `CLA` (Class Byte): `0x00`
- `INS` (Instruction Byte): `0xA4` (SELECT) or `0xB0` (READ BINARY)
- `P1` / `P2` (Parameter Bytes): File selection modes or memory offsets
- `Lc` (Length of Command Data)
- `Data Bytes`: Payload (e.g., Application Identifier - AID)
- `Le` (Expected Return Length)

Every APDU response must terminate with a 2-byte Status Word (`SW1-SW2`):
- `0x90 0x00`: Success / OK (`SW_OK`)
- `0x6A 0x82`: File or Application Not Found (`SW_FILE_NOT_FOUND`)
- `0x6F 0x00`: Unknown / Execution Error (`SW_UNKNOWN`)

### Host Card Emulation (HCE) vs. Foreground Reader Mode
A central engineering achievement of Taplay is configuring two Android hardware platforms to execute contrasting sides of the NFC radio handshake:

1. **Samsung Galaxy Watch4 (`com.taplay.wear`)**:
   Runs as an **ISO 14443-4 Host Card Emulator** using `HostApduService`. The watch's NFC controller simulates an NXP SmartMX or secure element in software. When placed inside an external RF field, the Android NFC stack routes inbound APDU frames directly to `TaplayWearApduService.kt`.

2. **Android Phone Cabinet (`com.taplay.app.emulator`)**:
   Runs in **Foreground Reader Mode** via `NfcAdapter.enableReaderMode()`. Rather than relying on Android's default intent dispatcher (which introduces UI-freezing intent popups and system sounds), Reader Mode grants exclusive low-level polling priority, suppressing the Android OS Tag Discovery sound and reading the tag in under 80 milliseconds.

### Dual-Mode APDU Handshake (Proprietary AID + NDEF with AAR)
A critical issue encountered during physical hardware testing was that third-party NFC tools and unconfigured Android phones would report "Tag Empty" or "Format Not Recognized" when touching the Galaxy Watch. 

Taplay engineered a **Dual-Mode APDU Responder** inside `TaplayWearApduService.kt`:
1. **Mode 1: Proprietary Fast-Path (Taplay Cabinet Reader)**:
   - Command: `00 A4 04 00 07 F0 01 02 03 04 05 06 00` (SELECT AID: `F0 01 02 03 04 05 06`).
   - Response: `0x90 0x00`.
   - Command: `00 B0 00 00 00` (READ BINARY).
   - Response: ASCII bytes of `"CARD-WATCH-01"` + `0x90 0x00`.
2. **Mode 2: NFC Forum Type-4 Tag Emulation (Standard Android OS)**:
   - Emulates the standard NFC Forum NDEF application AID: `D2 76 00 00 85 01 01`.
   - Serves an embedded **Capability Container (CC File - `E103`)** (15 bytes defining mapping version 2.0, max read 59 bytes).
   - Serves an **NDEF Data File (`E104`)** containing two distinct NDEF records:
     - *Record 1*: Text Record containing `"CARD-WATCH-01"`.
     - *Record 2*: **Android Application Record (AAR)** for `com.taplay.app.emulator`.
   - **Impact**: Any phone that taps the watch automatically resolves the tag and immediately brings the Taplay Emulator to the foreground without user intervention.

### The "Empty Canvas" Terminal Virtualization Strategy
Physical arcade cabinets utilize 12V DC coin-drop switches or JAMMA harness microswitches. Because physical cabinet hardware with embedded NFC coils is not wired directly to developer desks, Taplay implements an **Empty Canvas Virtualization Strategy**:
- The backend treats any machine identifier (`MACH-PHONE-2` or `MACH-A14`) as an active session receptor.
- The Android phone running the debug build acts as the physical cabinet itself, complete with coin audio, illuminated bezels, and the embedded Galaga arcade game.
- Once physical Raspberry Pi or ESP32 hardware is introduced, the exact same REST payload that activates the phone's Galaga game triggers the 12V coin relay on the physical cabinet via an MQTT or WebSocket bridge.

---

# 6. End-to-End Dynamic Interaction Flows

### Flow A: Physical Wrist/Phone NFC Tap-to-Play
```
 Galaxy Watch4               Phone Cabinet (MACH-PHONE-2)          Spring Boot Backend           ML Fraud Engine
      │                                   │                                │                            │
      │ 1. Wrist touches Phone            │                                │                            │
      │─── [13.56 MHz RF Field] ─────────>│                                │                            │
      │                                   │                                │                            │
      │<── SELECT AID (F0010203040506) ───│                                │                            │
      │─── 0x9000 (OK) ──────────────────>│                                │                            │
      │<── READ BINARY (00 B0 ...) ───────│                                │                            │
      │─── "CARD-WATCH-01" + 0x9000 ─────>│                                │                            │
      │                                   │                                │                            │
      │                                   │ 2. POST /api/v1/transaction/start                           │
      │                                   │    { tagId: "CARD-WATCH-01",   │                            │
      │                                   │      userId: 1 }               │                            │
      │                                   │───────────────────────────────>│                            │
      │                                   │                                │ 3. POST /predict           │
      │                                   │                                │    { amount: 10, ... }     │
      │                                   │                                │───────────────────────────>│
      │                                   │                                │<── { is_fraud: false } ────│
      │                                   │                                │                            │
      │                                   │                                │ 4. Debit ₹10 / 1 Credit   │
      │                                   │                                │    Map Active Session      │
      │                                   │<── 200 OK { success: true, ────│                            │
      │                                   │           duration: 300 }      │                            │
      │                                   │                                │                            │
      │                                   │ 5. BOOT GALAGA ARCADE GAME     │                            │
      │                                   │    (60 FPS Space Shooter)      │                            │
      │                                   │                                │                            │
      │ 6. Background Polling / Status    │                                │                            │
      │    GET /api/v1/session/status?machineId=CARD-WATCH-01              │                            │
      │───────────────────────────────────────────────────────────────────>│                            │
      │<── 200 OK { status: "ACTIVE", remaining: 300 } ───────────────────│                            │
      │                                                                    │                            │
      │ 7. Wrist enters Active Session (Pulsing Cyan Ring + Haptics)       │                            │
```

### Flow B: Multi-Device Remote Session Control (Pause / Resume / Stop)
```
 Galaxy Watch4                 Spring Boot Backend               Phone Cabinet (MACH-PHONE-2)        Web Dashboard
      │                                 │                                    │                              │
      │ 1. Tap [⏸ PAUSE] on wrist       │                                    │                              │
      │─── POST /session/pause ────────>│                                    │                              │
      │    (machineId: CARD-WATCH-01)   │                                    │                              │
      │                                 │ 2. session.pause(now)              │                              │
      │                                 │    Records pauseOffsetSeconds      │                              │
      │<── 200 OK { action: "PAUSED" } ─│                                    │                              │
      │                                 │                                    │                              │
      │                                 │ 3. Polling /status: PAUSED         │ 4. Polling /status: PAUSED   │
      │                                 │<── GET /session/status ────────────│<── GET /session/status ──────│
      │                                 │─── { status: "PAUSED" } ──────────>│─── { status: "PAUSED" } ────>│
      │                                 │                                    │                              │
      │                                 │                                    │ 5. Freeze Game Physics       │
      │                                 │                                    │    Show "SESSION PAUSED"     │
      │                                 │                                    │    Overlay Screen            │
      │                                 │                                    │                              │
      │ 6. Tap [▶ RESUME] on wrist      │                                    │                              │
      │─── POST /session/resume ───────>│                                    │                              │
      │                                 │ 7. session.resume(now)             │                              │
      │                                 │    Resets pausedAt marker          │                              │
      │                                 │                                    │                              │
      │                                 │ 8. Polling /status: ACTIVE         │                              │
      │                                 │<── GET /session/status ────────────│                              │
      │                                 │─── { status: "ACTIVE" } ──────────>│                              │
      │                                 │                                    │                              │
      │                                 │                                    │ 9. Unfreeze Game Physics     │
      │                                 │                                    │    Lasers & Aliens Resume    │
```

---

# 7. Exhaustive File-by-File Breakdown

This section details every file in the Taplay repository, documenting its exact role, classes, and linkages.

```
/home/pracxy/Taplay/
├── build.gradle.kts                    # Root build configuration
├── settings.gradle.kts                 # Multi-project module inclusion (:app, :wear)
├── gradle.properties                   # JVM heap allocation & AndroidX flags
├── local.properties                    # Android SDK directory path
├── run_taplay.py                       # Unified ecosystem runner & process manager
├── RUNNING_INSTRUCTIONS.md             # Wireless Wi-Fi operational cheat-sheet
├── README.md                           # Quick-start summary
├── README2.md                          # Technical report v2
│
├── app/                                # Android Mobile Module (Client & Cabinet Emulator)
│   ├── build.gradle.kts                # Android plugins, Compose BOM, Retrofit, DataStore
│   ├── proguard-rules.pro              # R8 code shrinking and reflection rules
│   └── src/main/
│       ├── AndroidManifest.xml         # NFC permissions, HCE service, activities
│       ├── java/com/taplay/app/
│       │   ├── MainActivity.kt         # Edge-to-edge router & NFC Intent dispatcher
│       │   ├── navigation/
│       │   │   └── AppNavigation.kt    # Navigation Compose route graph (9 destinations)
│       │   ├── network/
│       │   │   └── ApiService.kt       # Retrofit HTTP interface definitions
│       │   ├── data/
│       │   │   ├── api/TaplayApiService.kt        # Primary REST endpoints for wallet & sessions
│       │   │   ├── model/Models.kt                # Immutable data classes & JSON DTOs
│       │   │   └── repository/WalletRepository.kt # DataStore cache + AutoFailover HTTP client
│       │   ├── nfc/
│       │   │   └── TaplayApduService.kt           # Mobile-side HCE card emulator
│       │   ├── screens/
│       │   │   ├── SplashScreen.kt                # Animated cyber branding boot screen
│       │   │   ├── OnboardingScreen.kt            # 3-step feature introduction carousel
│       │   │   ├── LoginScreen.kt                 # Cyberpunk login & guest access
│       │   │   ├── HomeScreen.kt                  # Main arcade patron dashboard
│       │   │   ├── TapScreen.kt                   # NFC foreground reader radar engine
│       │   │   ├── SessionActiveScreen.kt         # Consumer circular countdown screen
│       │   │   ├── TransactionScreen.kt           # Razorpay top-up flow & audit ledger
│       │   │   ├── ProfileScreen.kt               # Player level, XP & achievement stats
│       │   │   ├── AvatarScreen.kt                # Cybernetic character avatar selection
│       │   │   ├── EmulatorScreen.kt              # Playable 60FPS Galaga arcade cabinet UI
│       │   │   └── EmulatorViewModel.kt           # Cabinet state machine & polling loop
│       │   └── ui/theme/
│       │       ├── Color.kt                       # Neon design tokens & color definitions
│       │       ├── Theme.kt                       # MaterialTheme wrapper & surface styling
│       │       └── Type.kt                        # Typography font families & scales
│       └── res/
│           ├── xml/apduservice.xml                # NFC HCE AID declaration for mobile
│           ├── xml/nfc_tech_filter.xml            # Tech filter for IsoDep, NfcA, Ndef
│           └── values/
│               ├── colors.xml                     # XML color backups
│               ├── strings.xml                    # App string resources
│               └── themes.xml                     # Legacy base theme declarations
│
├── wear/                               # Wear OS Smartwatch Module
│   ├── build.gradle.kts                # Wear OS Compose, Horologist, Wear Play Services
│   ├── proguard-rules.pro              # Wear R8 Proguard rules
│   └── src/main/
│       ├── AndroidManifest.xml         # Wear HCE declarations & smartwatch features
│       ├── java/com/taplay/wear/
│       │   ├── MainActivity.kt         # Wearable ComponentActivity entry point
│       │   ├── data/
│       │   │   ├── WearApiService.kt              # Retrofit REST interface for watch
│       │   │   ├── WearRepository.kt              # Multi-host failover client & repository
│       │   │   └── WearSessionState.kt            # Singleton StateFlow bridging HCE & UI
│       │   ├── nfc/
│       │   │   └── TaplayWearApduService.kt       # Dual-mode ISO 7816-4 APDU HCE engine
│       │   └── ui/
│       │       ├── WearViewModel.kt               # Watch state machine & auto-polling
│       │       └── WearMainScreen.kt              # Circular Wear Compose UI & controls
│       └── res/
│           ├── xml/apduservice.xml                # Watch HCE AID configuration
│           └── values/strings.xml                 # Watch display strings
│
├── backend/                            # Spring Boot 3 Core Backend Engine
│   ├── pom.xml                         # Maven dependencies (Spring Data JPA, H2, Web)
│   └── src/main/
│       ├── resources/
│       │   ├── application.properties             # H2 file DB config & ML service port
│       │   ├── schema.sql                         # DDL table creation scripts
│       │   ├── data.sql                           # Initial seed records (users, machines)
│       │   └── static/
│       │       └── index.html                     # Glassmorphic Real-Time Telemetry Dashboard
│       └── java/com/taplay/backend/
│           ├── TaplayBackendApplication.java      # Spring Boot application bootstrap
│           ├── controller/
│           │   ├── TransactionController.java     # /api/v1/transaction endpoints
│           │   ├── WalletController.java          # /api/v1/user endpoints
│           │   ├── MachineController.java         # /api/v1/machines endpoints
│           │   └── SessionControlController.java  # /api/v1/session/pause & resume
│           ├── dto/
│           │   ├── StartTransactionRequest.java   # Inbound tap payload DTO
│           │   ├── TopUpRequest.java              # Inbound wallet credit DTO
│           │   ├── TransactionResponse.java       # Authorized transaction response
│           │   └── MachineSessionStatusResponse.java # Live telemetry polling payload
│           ├── entity/
│           │   ├── UserEntity.java                # JPA mapping for 'users' table
│           │   ├── ArcadeMachineEntity.java       # JPA mapping for 'arcade_machines'
│           │   └── TransactionEntity.java         # JPA mapping for 'transactions'
│           ├── repository/
│           │   ├── UserRepository.java            # Spring Data JPA User CRUD
│           │   ├── ArcadeMachineRepository.java   # Spring Data JPA Machine lookup
│           │   └── TransactionRepository.java     # Spring Data JPA Ledger queries
│           └── service/
│               ├── TransactionService.java        # Core business engine & pause math
│               └── FraudDetectionService.java     # Asynchronous ML inference HTTP bridge
│
└── fraud_service/                      # Python ML Fraud Microservice
    ├── requirements.txt                # FastAPI, Uvicorn, Scikit-Learn, Joblib, NumPy
    ├── train_model.py                  # Synthetic dataset generator & Isolation Forest trainer
    ├── isolation_forest.pkl            # Serialized binary model artifact
    └── main.py                         # FastAPI REST application serving /predict & /health
```

---

### 7.1 Root & Orchestration Scripts

#### 1. `run_taplay.py`
- **Purpose**: Serves as the master operating system controller for the developer. It automates what previously required 5 open terminal tabs.
- **Key Functions**:
  - `get_lan_ip()`: Probes the active network interface to detect the host machine's Wi-Fi LAN IP (e.g., `192.168.1.158`).
  - `kill_port(port)`: Issues `fuser -k -n tcp <port>` to cleanly liberate TCP ports 8080 and 5000 from stale background instances.
  - `start_fraud_service()`: Launches `fraud_service/main.py` using its local virtual environment, redirecting logs to `fraud_service.log` and awaiting `http://127.0.0.1:5000/health`.
  - `start_backend()`: Boots `backend/target/backend-1.0.0.jar` (or `./mvnw spring-boot:run`), awaiting port 8080.
  - `configure_adb_wireless()`: Connects to Android devices over Wi-Fi (`adb connect <IP:PORT>`), configures reverse port forwarding (`adb reverse tcp:8080 tcp:8080`), and auto-launches the apps using `adb shell am start`.
  - `signal_handler()`: Catches `SIGINT` (Ctrl+C) to terminate all background subprocesses and free memory.

#### 2. `settings.gradle.kts`
- **Purpose**: Gradle multi-module root definition.
- **Contents**: Includes `:app` (Mobile/Emulator) and `:wear` (Galaxy Watch), setting dependency resolution repositories to Google Maven, Maven Central, and Gradle Plugin Portal.

#### 3. `build.gradle.kts` (Root)
- **Purpose**: Orchestrates build plugins across modules without evaluation dependencies, specifying Android Application and Kotlin Compose compiler plugins.

---

### 7.2 Android Mobile Application (`app/`)

#### 1. `app/build.gradle.kts`
- **Purpose**: Configures Android compilation (SDK 34), Jetpack Compose compiler extensions, and build types.
- **Critical Configuration**:
  ```kotlin
  buildTypes {
      debug {
          applicationIdSuffix = ".emulator"
          resValue("string", "app_name", "Taplay Emulator")
      }
  }
  ```
  This setting is the linchpin of the two-phone architecture: compiling in `debug` assigns the package ID `com.taplay.app.emulator` and sets the launcher title to **Taplay Emulator**, automatically transforming the phone into an arcade cabinet terminal.

#### 2. `MainActivity.kt`
- **Purpose**: The sole ComponentActivity hosting the application.
- **Internal Logic**:
  - Reads `BuildConfig.APPLICATION_ID`. If the suffix is `.emulator`, it boots directly into `EmulatorScreen.kt`. Otherwise, it boots into the consumer navigation graph `AppNavigation()`.
  - Implements `handleNfcIntent()` to catch NFC hardware tags tapped when the app was in the background or launched via Android system discovery.

#### 3. `data/repository/WalletRepository.kt`
- **Purpose**: Manages persistent local data via Jetpack DataStore and controls Retrofit HTTP communication with the backend.
- **Key Breakthrough - Multi-Host Auto-Failover**:
  Contains an OkHttp `Interceptor` that maintains a priority list of candidate hosts:
  ```kotlin
  private val candidateHosts = listOf(
      "127.0.0.1:8080",
      "192.168.0.114:8080",
      "192.168.1.158:8080",
      "192.168.1.162:8080"
  )
  ```
  When an HTTP request is made, the interceptor sequentially tests these hosts with a 3-second timeout. If the developer moves between home Wi-Fi, university networks, or a mobile hotspot, the app dynamically routes traffic to the reachable host without code modification.

#### 4. `screens/EmulatorScreen.kt` & `EmulatorViewModel.kt`
- **Purpose**: The visual heart of the arcade cabinet emulator (`MACH-PHONE-2`).
- **Internal Mechanics**:
  - **NFC Hardware Listener**: Uses `DisposableEffect` to engage `NfcAdapter.enableReaderMode()`. Upon tag detection, it connects an `IsoDep` interface and transceives the ISO 7816-4 APDU command `00 A4 04 00 ...` (SELECT Taplay AID) followed by `00 B0 00 00 ...` (READ BINARY) to read `"CARD-WATCH-01"`.
  - **Embedded Galaga Arcade Game**:
    - Built completely on top of Jetpack Compose `Canvas`.
    - Features a continuous 60-FPS loop rendering a dynamic starfield with depth parallax (`Star(x, y, speed, size, alpha)`).
    - Player controls a cyan-neon spacefighter using `detectDragGestures` at the bottom of the screen.
    - Automatic dual laser cannons fire upwards at 18px/frame (`Laser(x, y, speed)`).
    - Fleets of multi-tiered alien invaders (butterflies, wasps, and boss flagships) oscillate in formation using sinusoidal wave math (`sin(angle)`).
    - Particle engine: Alien destructions trigger radial velocity bursts of multi-colored glowing sparks (`Particle(x, y, vx, vy, alpha, color)`).
  - **Session State Overlay**:
    - Listens to `EmulatorViewModel.sessionStatus`.
    - If status is `PAUSED`, freezes the game physics loop and displays a semi-transparent violet overlay reading **"SESSION PAUSED FROM WRIST"**.
    - If status returns to `IDLE`, gracefully transitions back to the arcade attract/insert-coin screen.

#### 5. `screens/HomeScreen.kt`
- **Purpose**: Consumer patron home screen.
- **Features**: Displays real-time INR balance (`₹540`) and arcade tokens (`54`), a radial "Tap to Play" quick button, game catalog carousel, and navigation items for top-ups and profile progression.

#### 6. `screens/TapScreen.kt`
- **Purpose**: The consumer phone's tap interface when the patron uses their phone to scan a physical arcade machine tag.
- **State Machine**: Transitions through `WAITING` ➔ `DETECTED` ➔ `CONFIRMED` ➔ `STARTED`, animating a pulsating radar wave. Includes a visual test simulation trigger for testing without physical NFC stickers.

#### 7. `screens/SessionActiveScreen.kt`
- **Purpose**: The consumer phone's active gaming screen.
- **Features**: A high-contrast circular countdown indicator driven by backend session duration. Displays warning color pulses (switching from Cyan to Rose) during the final 30 seconds.

#### 8. `screens/TransactionScreen.kt`
- **Purpose**: The financial hub for the patron.
- **Features**: Complete Razorpay test flow simulation. Supports preset top-up increments (₹50, ₹100, ₹200, ₹500), payment method selection (UPI, Credit Card, Net Banking), simulated payment gateway processing dialogues, and generates verifiable digital receipts.

#### 9. `nfc/TaplayApduService.kt`
- **Purpose**: Allows the consumer phone to emulate an NFC card (HCE Type-4) when presented to an external cabinet reader.

---

### 7.3 Wear OS Smartwatch Application (`wear/`)

#### 1. `wear/build.gradle.kts`
- **Purpose**: Configures compilation for Wear OS 4 (API 34) using AndroidX Wear Compose BOM, Horologist layout utilities, and Wear Play Services.

#### 2. `nfc/TaplayWearApduService.kt`
- **Purpose**: The HCE card emulation engine for Samsung Galaxy Watch4.
- **Implementation**:
  - Implements the dual-mode APDU protocol described in Section 5.
  - Broadcasts `CARD-WATCH-01`.
  - Notifies `WearSessionState.onNfcTapSent()` upon successful transceive.

#### 3. `data/WearSessionState.kt`
- **Purpose**: A thread-safe singleton managing `StateFlow` primitives. Bridges the background NFC HCE execution thread with the foreground Wear Compose rendering loop.

#### 4. `data/WearRepository.kt` & `data/WearApiService.kt`
- **Purpose**: Retrofit REST client for Wear OS. Contains the smartwatch auto-failover network interceptor and exposes suspend functions for `getSessionStatus()`, `pauseSession()`, `resumeSession()`, and `endSession()`.

#### 5. `ui/WearViewModel.kt` & `ui/WearMainScreen.kt`
- **Purpose**: The circular Wear OS user interface.
- **Visual Presentation**:
  - *Ready Mode*: Displays `CARD-WATCH-01`, a glowing cyan pulsing beacon, live wallet balance (`₹500` fetched over Wi-Fi), and instruction **"TAP TO PLAY"**.
  - *Active Mode*: Automatically triggered when backend reports `ACTIVE`. Renders a circular progress arc showing remaining playtime (e.g., `04:58`), the current game title (`Cyber Strike 2077`), and wrist control buttons:
    - **[⏸ Pause]**: Calls `/api/v1/session/pause` to freeze cabinet play.
    - **[▶ Resume]**: Calls `/api/v1/session/resume` to unfreeze cabinet play.
    - **[⏹ Stop]**: Calls `/api/v1/session/end` to cash out and return to idle.

---

### 7.4 Spring Boot Enterprise Backend (`backend/`)

#### 1. `backend/pom.xml`
- **Purpose**: Maven configuration importing Spring Boot 3.2.5 starters: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, embedded `com.h2database:h2`, and test frameworks.

#### 2. `service/TransactionService.java`
- **Purpose**: The transactional nucleus of Taplay.
- **Key Mechanics**:
  - `activeSessions`: A `ConcurrentHashMap<String, ActiveMachineSession>` indexing active sessions across machine IDs (`MACH-PHONE-2`), tag IDs, and watch cards (`CARD-WATCH-01`).
  - **Mathematical Pause Offset Logic**:
    ```java
    public void pause(LocalDateTime now) {
        if (pausedAt == null) {
            pausedAt = now;
            pauseOffsetSeconds += Duration.between(startTime, now).getSeconds();
        }
    }
    public long effectiveElapsed(LocalDateTime now) {
        if (isPaused()) return pauseOffsetSeconds;
        return pauseOffsetSeconds + Duration.between(
            pausedAt != null ? pausedAt : startTime, now).getSeconds();
    }
    ```
    This elegant mathematical algorithm prevents countdown time from drifting or expiring while a player pauses the session.
  - `processTransaction()`:
    1. Validates user existence (auto-provisions default user `Praharsh` if fresh DB).
    2. Maps machine UID to arcade machine entity.
    3. Invokes `FraudDetectionService`. If flagged, rejects transaction and records audit alert.
    4. Validates wallet balance (`balance >= costAmount`).
    5. Deducts balance atomically and saves updated user entity.
    6. Persists immutable `TransactionEntity` ledger record.
    7. Registers new `ActiveMachineSession` into concurrent map.

#### 3. `service/FraudDetectionService.java`
- **Purpose**: REST client connecting to the Python FastAPI microservice.
- **Circuit Breaker / Fallback**: Sets a 500ms connection timeout via Spring's `RestTemplate`. If the Python microservice is offline, it logs a warning and falls back safely to internal rule validation rather than stranding patrons at the cabinet.

#### 4. `controller/SessionControlController.java`
- **Purpose**: REST controller handling wrist-originated session controls.
- **Endpoints**:
  - `POST /api/v1/session/pause?machineId=...`
  - `POST /api/v1/session/resume?machineId=...`

#### 5. `controller/TransactionController.java`
- **Purpose**: REST controller handling game authorization and status polling.
- **Endpoints**:
  - `POST /api/v1/transaction/start`
  - `POST /api/v1/transaction/end`
  - `GET /api/v1/transaction/session-status?machineId=...`

#### 6. `controller/WalletController.java`
- **Purpose**: REST controller managing customer accounts.
- **Endpoints**:
  - `GET /api/v1/user/wallet?userId=1`
  - `POST /api/v1/user/topup`
  - `GET /api/v1/user/transactions?userId=1`

#### 7. `resources/static/index.html`
- **Purpose**: The Glassmorphic Real-Time Operations & Telemetry Dashboard.
- **Architecture**: A zero-dependency single-page application built with vanilla CSS, HTML5, and JavaScript. 
- **Features**:
  - Polls backend state every 1000ms.
  - Animated SVG radial countdown meter for the active cabinet.
  - Live buttons to trigger or terminate sessions remotely.
  - Audit ledger table displaying every debit and top-up.
  - System health indicators for Database, ML Service, and Connected NFC Terminals.

---

### 7.5 Python Machine Learning Fraud Microservice (`fraud_service/`)

#### 1. `fraud_service/train_model.py`
- **Purpose**: Generates synthetic training datasets and fits the unsupervised Isolation Forest.
- **Dataset Synthesis**:
  - *Normal Patterns (1000 samples)*:
    - Amount: Normal distribution $\mu = 15, \sigma = 5$ credits.
    - Hour: 10 AM to 10 PM (10–22).
    - Frequency: 1 to 4 sessions per hour.
    - Terminal Hash: 1 to 10.
  - *Anomalous / Fraud Patterns (50 samples - 5% contamination)*:
    - Amount: 200 to 1000 credits (sudden massive balance drains).
    - Hour: 2 AM to 5 AM (midnight arcade break-ins).
    - Frequency: 20 to 100 sessions per hour (rapid automated replay loops).
- **Fitting**: Fits an `IsolationForest(n_estimators=100, contamination=0.05, random_state=42)` and dumps the binary artifact to `isolation_forest.pkl`.

#### 2. `fraud_service/main.py`
- **Purpose**: High-speed asynchronous FastAPI REST microservice.
- **Endpoints**:
  - `GET /health`: Returns service health status.
  - `POST /predict`: Receives `PredictionRequest(user_id, amount, hour_of_day, frequency_1h, terminal_id_hash)`, constructs a NumPy feature array, computes `model.predict(features)` and `model.score_samples(features)`, and returns `PredictionResponse(is_fraud, anomaly_score, message)`.

---

# 8. The Engineering Evolution: How Taplay Was Developed

The development of Taplay progressed through distinct engineering phases, solving complex physical, mathematical, and networking challenges:

### Phase 1: Initial Inception & Basic NFC Reader
- **Initial Goal**: Build a simple Android app that could scan an NTAG213 sticker on a desk, read its UID, and deduct a mock balance in memory.
- **First Implementation**: Relied on standard Android Intent filters (`ACTION_TAG_DISCOVERED`).
- **Discovery**: The default Android intent system was sluggish. Bringing up the system NFC dialog interrupted user flow, and the app frequently lost focus.
- **Solution**: Migrated to `NfcAdapter.enableReaderMode()`, unlocking direct transceiver access and bypassing system sounds.

### Phase 2: Microservice Decoupling & ML Fraud Integration
- **The Challenge**: Physical arcade cards are vulnerable to replay attacks. Static tokens can be forged.
- **Architecture Shift**: Introduced an independent Python microservice using FastAPI. We chose an **Isolation Forest** because fraudulent arcade attacks are unsupervised—we do not have labeled datasets of all future attack patterns.
- **Spring Boot Bridge**: Built `FraudDetectionService.java` with a 500ms timeout circuit breaker, guaranteeing that an ML failure would never hang a transaction at the cabinet.

### Phase 3: The Hardware Bottleneck & Two-Phone Virtualization Pivot
- **The Reality**: Custom arcade cabinet motherboards with RS-232/USB NFC modules were unavailable in the local software development lab.
- **The Innovation**: Instead of halting development waiting for physical factory cabinets, we devised the **Two-Phone Architecture**:
  - Device 1: Consumer Phone or Smartwatch (acting as the player's card).
  - Device 2: Android Phone running with `applicationIdSuffix = ".emulator"` (acting as the physical cabinet `MACH-PHONE-2`).
- **Result**: The complete arcade floor dynamics could be developed and validated end-to-end on real hardware.

### Phase 4: Designing the Playable Jetpack Compose Galaga Arcade Engine
- **Problem**: When a customer taps an arcade machine, showing a simple static "Success" label felt uninspiring and failed to replicate an authentic arcade experience.
- **Solution**: We built a complete retro **Galaga Space Shooter** from scratch inside `EmulatorScreen.kt` using Jetpack Compose `Canvas`.
- **Engineering Feat**: Achieving smooth 60 FPS performance without a traditional game engine (e.g., Unity). We implemented custom math loops for alien sinusoidal waving, multi-layered parallax starfields, and particle blast physics.

### Phase 5: Wear OS Galaxy Watch Development & Dual-Mode HCE
- **The Ambition**: Bringing arcade access directly to the player's wrist on a Samsung Galaxy Watch4.
- **The Roadblock**: Wear OS watches run different power-saving NFC constraints. When touching the phone, third-party apps failed to recognize the watch tag.
- **The Breakthrough**: We implemented the **Dual-Mode ISO 7816-4 APDU Service** in `TaplayWearApduService.kt`. By serving an embedded Capability Container and NDEF record containing an **Android Application Record (AAR)**, touching the watch to any Android device automatically forced the phone to launch the Taplay Cabinet Emulator.

### Phase 6: The Android Network Sandbox Battle & 100% Wireless Wi-Fi Migration
- **The Problem**: Android apps are sandboxed. In early testing, the app communicated with `http://127.0.0.1:8080/`. But on Android, `127.0.0.1` refers to the *phone itself*, not the laptop hosting the Spring Boot backend! This forced us to use tethered USB cables running `adb reverse tcp:8080 tcp:8080`.
- **The Architectural Victory**:
  1. We designed `AutoFailoverInterceptor` in `WalletRepository.kt` and `WearRepository.kt`, providing a ranked list of candidate IP addresses.
  2. The phone, watch, and laptop were bridged to the same local Wi-Fi router.
  3. **No USB cables required.** The entire ecosystem operates 100% wirelessly over Wi-Fi.

### Phase 7: Distributed State Synchronization & Mathematical Pause Offsets
- **The Challenge**: If a player pauses their game from their smartwatch to take a phone call, standard timer loops based on `endTime = startTime + 300s` would continue running down, cheating the player of paid game time.
- **The Solution**: We engineered the **Pause Offset Engine** in `TransactionService.java`. When paused, the backend freezes the clock by logging a delta timestamp. When resumed, the elapsed calculation accounts for the accumulated pause duration, preserving player playtime with microsecond accuracy.

### Phase 8: Glassmorphism Web Operations Dashboard & Master Runner Automation
- **The Climax**: Built `index.html` to give arcade operators a centralized mission-control interface. To unify the ecosystem, we created `run_taplay.py`, enabling developers to start the entire multi-device platform with a single terminal command.

---

# 9. Machine Learning & Anomaly Detection Deep Dive

### Algorithmic Foundation: Isolation Forest
The fraud microservice uses the **Isolation Forest** algorithm (Liu, Ting, and Zhou), an unsupervised ensemble method based on decision trees.

**Mathematical Principle**:
Traditional anomaly detection builds profiles of normal instances and identifies outliers that fail to conform. Isolation Forest explicitly isolates anomalies instead. 
Because anomalies are *few and different*, they are isolated closer to the root of a random decision tree (i.e., with a significantly shorter average path length $h(x)$).

The anomaly score $s(x, n)$ for an instance $x$ across an ensemble of $n$ trees is defined as:
$$s(x, n) = 2^{-\frac{E(h(x))}{c(n)}}$$

Where:
- $E(h(x))$ is the average path length across all isolation trees.
- $c(n)$ is the average path length of unsuccessful searches in a Binary Search Tree (BST) constructed over $n$ samples:
$$c(n) = 2\left(\ln(n - 1) + 0.5772156649\right) - \frac{2(n - 1)}{n}$$

**Decision Boundary**:
- If $s \to 1$: $E(h(x)) \to 0$ (The instance is isolated extremely quickly ➔ **Flagged as Fraud**).
- If $s < 0.5$: $E(h(x)) \to c(n)$ (Normal instance deep within the tree clusters).

### Feature Vector Formulation
Every transaction generates a 4-dimensional continuous feature vector $X \in \mathbb{R}^4$:
$$X = \begin{bmatrix} x_{\text{amount}} & x_{\text{hour}} & x_{\text{freq}} & x_{\text{terminal}} \end{bmatrix}$$

1. $x_{\text{amount}}$: Cost in credits (Normal: 5–30 credits. Fraud: 200–1000 credits).
2. $x_{\text{hour}}$: Hour of day $[0, 23]$ (Normal: 10–22. Fraud: 2–5 AM midnight anomalies).
3. $x_{\text{freq}}$: Rolling 1-hour session frequency (Normal: 1–5 taps. Fraud: 20–100 rapid programmatic replays).
4. $x_{\text{terminal}}$: Integer hash of cabinet identifier (detecting impossible physical teleportation between distant machines).

---

# 10. REST API, APDU & WebSocket/Polling Contracts

### 1. `POST /api/v1/transaction/start`
- **Purpose**: Authorizes game start upon NFC tap.
- **Request Body**:
  ```json
  {
    "userId": 1,
    "tagId": "CARD-WATCH-01"
  }
  ```
- **Response (200 OK - Authorized)**:
  ```json
  {
    "success": true,
    "message": "Transaction Authorized & Session Started",
    "transactionId": "TXN-A83F12C0",
    "sessionDuration": 300,
    "creditsDeducted": 10,
    "remainingBalance": 530.0,
    "machineName": "Cyber Strike 2077",
    "machineId": "MACH-PHONE-2"
  }
  ```
- **Response (200 OK - Fraud Flagged)**:
  ```json
  {
    "success": false,
    "message": "Transaction flagged as high-risk by ML Fraud Detection system.",
    "transactionId": null,
    "sessionDuration": 0,
    "creditsDeducted": 0,
    "remainingBalance": 540.0,
    "machineName": "Cyber Strike 2077",
    "machineId": "MACH-PHONE-2"
  }
  ```

### 2. `GET /api/v1/transaction/session-status`
- **Query Parameter**: `machineId=CARD-WATCH-01` (or `MACH-PHONE-2`)
- **Response (200 OK)**:
  ```json
  {
    "machineId": "CARD-WATCH-01",
    "sessionStatus": "ACTIVE",
    "sessionDuration": 300,
    "timeRemaining": 284,
    "transactionId": "TXN-A83F12C0",
    "userName": "Praharsh",
    "gameName": "Cyber Strike 2077"
  }
  ```

### 3. `POST /api/v1/session/pause` & `POST /api/v1/session/resume`
- **Query Parameter**: `machineId=CARD-WATCH-01`
- **Response (200 OK)**:
  ```json
  {
    "success": true,
    "machineId": "CARD-WATCH-01",
    "action": "PAUSED"
  }
  ```

### 4. `POST /predict` (Python ML Microservice)
- **URL**: `http://127.0.0.1:5000/predict`
- **Request Body**:
  ```json
  {
    "user_id": 1,
    "amount": 10.0,
    "hour_of_day": 14,
    "frequency_1h": 1,
    "terminal_id_hash": 4
  }
  ```
- **Response (200 OK)**:
  ```json
  {
    "is_fraud": false,
    "anomaly_score": 0.1423,
    "message": "Transaction pattern normal."
  }
  ```

---

# 11. Relational Data Model & Persistence Architecture

The backend utilizes Spring Data JPA with an embedded H2 relational engine (`jdbc:h2:file:./data/taplaydb`), ensuring schema parity with PostgreSQL and Supabase:

```sql
-- Users & Digital Balances
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    balance DOUBLE PRECISION NOT NULL DEFAULT 540.0,
    credits INT NOT NULL DEFAULT 54,
    total_tokens INT NOT NULL DEFAULT 120,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Arcade Hardware Cabinet Catalog
CREATE TABLE IF NOT EXISTS arcade_machines (
    machine_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    tag_id VARCHAR(100) NOT NULL UNIQUE,
    cost_credits INT NOT NULL,
    cost_amount DOUBLE PRECISION NOT NULL,
    genre VARCHAR(50),
    difficulty VARCHAR(50),
    emoji VARCHAR(10),
    session_duration INT NOT NULL DEFAULT 300
);

-- Immutable Transaction Ledger
CREATE TABLE IF NOT EXISTS transactions (
    id VARCHAR(100) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    machine_id VARCHAR(50),
    amount DOUBLE PRECISION NOT NULL,
    credits INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL
);

-- Security & ML Fraud Audit Trail
CREATE TABLE IF NOT EXISTS fraud_alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tag_id VARCHAR(100),
    amount DOUBLE PRECISION,
    anomaly_score DOUBLE PRECISION,
    reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

# 12. Step-by-Step Operations, Testing & Verification Guide

### Quick Start via Master Runner
To start the entire ecosystem in one command:
```bash
cd /home/pracxy/Taplay
./run_taplay.py
```
This script:
1. Frees ports 8080 and 5000.
2. Starts the Python ML Fraud Detection Service.
3. Compiles/boots the Spring Boot backend.
4. Auto-connects ADB over Wi-Fi to your phone and Galaxy Watch.
5. Launches the apps on both devices.

### Manual Verification Steps

#### Step 1: Health Check Python ML Microservice
```bash
curl -s http://localhost:5000/health
# Output: {"status":"UP","service":"Taplay Fraud Detection ML Service"}
```

#### Step 2: Health Check Spring Boot Backend
```bash
curl -s "http://localhost:8080/api/v1/user/wallet?userId=1"
# Output: {"userId":1,"userName":"Praharsh","balance":540.0,"credits":54,"tickets":120}
```

#### Step 3: Open Operations Dashboard
Open Google Chrome or Firefox and navigate to:
```
http://localhost:8080/
```
*(Or from any tablet/phone on your Wi-Fi: `http://192.168.1.158:8080/`)*

#### Step 4: Perform Wireless NFC Tap
1. Hold the back of your **Samsung Galaxy Watch4** against the upper back of your **Redmi Phone**.
2. **Watch Reaction**: The watch vibrates with haptic feedback, fetches the session over Wi-Fi, and displays the circular countdown timer ring.
3. **Phone Reaction**: The cabinet terminal emits an arcade sound and instantly launches the **Galaga Space Shooter**. Drag your finger to steer and destroy alien formations!
4. **Wrist Remote Control**:
   - Tap **[⏸ Pause]** on the watch: The Galaga game freezes on the phone, displaying a violet "PAUSED" overlay.
   - Tap **[▶ Resume]** on the watch: The Galaga game unfreezes and resumes play.
   - Tap **[⏹ Stop]** on the watch: The session ends and both devices return to their idle states.
5. **Dashboard Reaction**: The Web Dashboard radial dial mirrors the countdown in real time and appends the transaction to the ledger table.

---

# 13. Future Technical Roadmap & Physical Cabinet Scaling

1. **Hardware Cabinet Coin-Relay Modules (ESP32)**:
   - Wire an **ESP32** microcontroller equipped with an **RC522 or PN532 NFC reader** to the coin-drop microswitch of a physical arcade cabinet (e.g., Tekken, Time Crisis, or Pac-Man).
   - When the backend authorizes a session, it emits an MQTT message (`arcade/cabinet/MACH-A14/start`), triggering a 100ms 12V pulse via an optocoupler relay to activate the game.
2. **Cloud Migration to Managed Supabase PostgreSQL**:
   - Transition from embedded H2 to a globally replicated Supabase PostgreSQL cluster by updating `application.properties`:
     ```properties
     spring.datasource.url=jdbc:postgresql://db.supabase.co:5432/postgres
     spring.datasource.username=postgres
     spring.datasource.password=YOUR_SUPABASE_PASSWORD
     ```
3. **Cryptographic Token Signing (Ed25519)**:
   - Upgrade the APDU payload from plaintext identifiers to Ed25519 asymmetric signed tokens with ephemeral timestamps to provide mathematical defense against sophisticated MITM sniffers.
4. **Push Alerts via Firebase Cloud Messaging (FCM)**:
   - Implement push notifications alerting players on their phones 60 seconds before their active cabinet session expires, offering a one-tap balance extension.

---
*Documentation compiled and maintained for the Taplay Arcade Platform.*
