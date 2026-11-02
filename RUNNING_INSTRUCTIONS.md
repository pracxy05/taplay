# Taplay — 100% Wireless Wi-Fi Architecture & Running Instructions

This guide explains how the **Taplay Galaxy Watch4**, **Phone Arcade Cabinet**, and **Backend** communicate completely over local Wi-Fi without needing a USB cable.

---

## 1. Why Was a USB Cable Needed Before, and Why Is It Not Needed Now?

### The Reason:
Previously, the phone app was configured with `http://127.0.0.1:8080/` (localhost). On an Android phone, `127.0.0.1` refers to the **phone itself**, NOT your laptop. The only way `127.0.0.1` could reach the laptop was by using an ADB reverse proxy over USB (`adb reverse tcp:8080 tcp:8080`).

### The Solution:
We updated both the **Watch** (`WearRepository.kt`) and the **Phone** (`WalletRepository.kt`) to connect directly to your laptop's local Wi-Fi IP address:
```
http://192.168.1.158:8080/
```
Now:
- **No USB cable is required**.
- **No `adb reverse` is required**.
- The Phone, Watch, and Laptop communicate directly over your local Wi-Fi network!

---

## 2. Current Device Network Addresses

All 3 devices are connected to the same Wi-Fi router:

| Device | Role | Network Address | Connection Method |
| :--- | :--- | :--- | :--- |
| **Laptop (CachyOS)** | Spring Boot (:8080) & ML Fraud (:5000) | `192.168.1.158` | Wi-Fi (`wlan0`) |
| **Galaxy Watch4 Classic** | Virtual Card (HCE `CARD-WATCH-01`) | `192.168.1.152` | Wi-Fi |
| **Redmi Phone** | Arcade Cabinet Terminal (`MACH-PHONE-2`) | `192.168.1.148` | Wi-Fi |

---

## 3. Live Screenshots (Over Wi-Fi)

| Screen | Description | Visual |
| :--- | :--- | :--- |
| **Galaxy Watch (Ready)** | Displays `CARD-WATCH-01`, glowing neon cyan `<NFC>` pulse ring, **TAP TO PLAY**, and live wallet balance (**₹500** fetched over Wi-Fi). | ![Watch Ready](file:///home/pracxy/.gemini/antigravity-ide/brain/a3acb7ab-9a33-4851-ad52-3c271e8c5a9b/watch_idle_shown.png) |
| **Phone Cabinet (Idle)** | Displays `MACH-PHONE-2 · HOLD GALAXY WATCH OR NFC CARD HERE` with active NFC reader mode. | ![Phone Idle](file:///home/pracxy/.gemini/antigravity-ide/brain/a3acb7ab-9a33-4851-ad52-3c271e8c5a9b/phone_idle_ready.png) |
| **Phone Arcade Game (Active)** | Real-time **Galaga Space Shooter** launches upon NFC tap. Player spaceship with drag steering, auto-firing dual lasers, and alien formations. | ![Galaga Game Active](file:///home/pracxy/.gemini/antigravity-ide/brain/a3acb7ab-9a33-4851-ad52-3c271e8c5a9b/phone_active_wifi.png) |
| **Galaxy Watch (Active Session)** | Live circular countdown timer ring with game name (`Cyber Strike 2077`), countdown, and **[⏸ Pause]** & **[⏹ Stop]** wrist controls. | ![Watch Active](file:///home/pracxy/.gemini/antigravity-ide/brain/a3acb7ab-9a33-4851-ad52-3c271e8c5a9b/watch_active_display.png) |

---

## 4. How to Start the Services (If Restarted)

Both services are currently **ALREADY RUNNING** in the background:
- **Port 5000**: Python ML Fraud Detection Service
- **Port 8080**: Spring Boot Backend & Dashboard

If you ever restart your laptop or need to run them manually:

### Terminal 1: Python ML Fraud Detection
```bash
cd /home/pracxy/Taplay/fraud_service
./venv/bin/python main.py
```

### Terminal 2: Spring Boot Backend
```bash
cd /home/pracxy/Taplay/backend
mvn spring-boot:run
```

### Web Operations Dashboard:
Open your browser and navigate to:
```
http://localhost:8080/
```
*(Or from any phone/tablet on your Wi-Fi: `http://192.168.1.158:8080/`)*

---

## 5. Live NFC Tap Demonstration Steps

1. **Verify Both Apps Are Open**:
   - **On Phone**: Open **Taplay Emulator** (it shows `MACH-PHONE-2 · HOLD GALAXY WATCH OR NFC CARD HERE`).
   - **On Galaxy Watch**: Open **Taplay Wear** (it shows `CARD-WATCH-01 · TAP TO PLAY · ₹500`).

2. **Touch Watch to Phone**:
   - Touch the back of your Galaxy Watch against the upper back of your phone where the NFC antenna is located.
   - The phone's NFC reader mode detects `CARD-WATCH-01`.
   - The phone sends the transaction request to `http://192.168.1.158:8080/api/v1/transaction/start`.
   - The backend authorizes the transaction and deducts ₹10.

3. **Multi-Device Synchronization**:
   - **Phone**: Instantly launches the **Galaga Space Shooter** arcade game! Drag your finger horizontally across the bottom to steer and blast aliens.
   - **Galaxy Watch**: Instantly switches to the **Active Session** screen with the circular countdown timer and controls:
     - Tap **[⏸] Pause** on watch -> Game on phone pauses!
     - Tap **[▶] Resume** on watch -> Game on phone resumes!
     - Tap **[⏹] Stop** on watch -> Game ends and returns to the idle screen.
   - **Web Dashboard**: Displays the live countdown ring and logs the transaction in the ledger table in real time.

---

## 6. What If You Change Wi-Fi Networks in the Future?

If you connect your laptop to a different Wi-Fi network (or mobile hotspot), your laptop IP will change. To update it:

1. Check your new laptop IP:
   ```bash
   ip -4 addr show dev wlan0 | grep "inet "
   ```
2. If the IP changes (e.g., from `192.168.1.158` to `192.168.x.x`):
   - In `wear/src/main/java/com/taplay/wear/data/WearRepository.kt`, update `baseUrl`.
   - In `app/src/main/java/com/taplay/app/data/repository/WalletRepository.kt`, update `baseUrl`.
   - Re-deploy to Watch and Phone:
     ```bash
     JAVA_HOME=/home/pracxy/.gradle/jdks/eclipse_adoptium-21-amd64-linux.2 ./gradlew :wear:assembleDebug :app:assembleDebug
     adb -s <watch-ip-port> install -r wear/build/outputs/apk/debug/wear-debug.apk
     adb -s <phone-ip-port> install -r app/build/outputs/apk/debug/app-debug.apk
     ```
