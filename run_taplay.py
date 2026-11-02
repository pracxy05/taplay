#!/usr/bin/env python3
"""
================================================================================
  🎮 TAPLAY UNIFIED ECOSYSTEM RUNNER & MASTER CONTROLLER
================================================================================
One command brings the entire Taplay ecosystem alive:
  1. Starts Python ML Fraud Detection Service (:5000)
  2. Starts Spring Boot Backend & Arcade Dashboard (:8080)
  3. Connects ADB wirelessly to Phone & Galaxy Watch
  4. Configures ADB reverse port-forwarding (tcp:8080 & tcp:5000)
  5. Automatically launches Phone Arcade Cabinet and Galaxy Watch App
  6. Keeps everything running with live telemetry and clean Ctrl+C shutdown
"""

# ==============================================================================
# ⚙️  CONFIG SECTION — CHANGE YOUR PHONE & WATCH IP:PORT HERE
# ==============================================================================
# Phone Wireless Debugging address (IP:PORT) or USB Serial (leave blank to skip)
PHONE_ADDRESS ="10.108.251.51:39879"

# Galaxy Watch Wireless Debugging address (IP:PORT) (leave blank to skip)
WATCH_ADDRESS = "10.108.251.34:41243"

# Automatically launch the apps on phone and watch once connected:
AUTO_LAUNCH_APPS = True

# Automatically install freshly compiled debug APKs when devices connect:
AUTO_INSTALL_APKS = False

# Backend & Fraud Service ports:
BACKEND_PORT = 8080
FRAUD_PORT = 5000
# ==============================================================================

import os
import sys
import time
import socket
import signal
import argparse
import subprocess
import threading
import urllib.request
import urllib.error

PROJECT_ROOT = os.path.dirname(os.path.abspath(__file__))
BACKEND_DIR = os.path.join(PROJECT_ROOT, "backend")
FRAUD_DIR = os.path.join(PROJECT_ROOT, "fraud_service")
PHONE_APK = os.path.join(PROJECT_ROOT, "app", "build", "outputs", "apk", "debug", "app-debug.apk")
WATCH_APK = os.path.join(PROJECT_ROOT, "wear", "build", "outputs", "apk", "debug", "wear-debug.apk")
JAR_PATH = os.path.join(BACKEND_DIR, "target", "backend-1.0.0.jar")

# Track subprocesses for clean shutdown
active_processes = []
shutdown_event = threading.Event()

# ANSI Color codes for beautiful terminal UI
class Colors:
    HEADER = '\033[95m'
    BLUE = '\033[94m'
    CYAN = '\033[96m'
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    BOLD = '\033[1m'
    DIM = '\033[2m'
    RESET = '\033[0m'

def log(msg, level="INFO"):
    prefix = {
        "INFO": f"{Colors.BLUE}[INFO]{Colors.RESET}",
        "SUCCESS": f"{Colors.GREEN}[✓]{Colors.RESET}",
        "WARN": f"{Colors.YELLOW}[⚠]{Colors.RESET}",
        "ERROR": f"{Colors.RED}[✗]{Colors.RESET}",
        "ECO": f"{Colors.CYAN}[TAPLAY]{Colors.RESET}",
    }.get(level, f"[{level}]")
    print(f"{prefix} {msg}")

def get_lan_ip():
    """Detect local IP on the active Wi-Fi / LAN network."""
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.settimeout(0.5)
        # Connect to any public DNS to identify outgoing interface
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        return "127.0.0.1"

def kill_port(port):
    """Ensure port is completely freed before starting service."""
    try:
        cmd = f"fuser -k -n tcp {port} 2>/dev/null || true"
        subprocess.run(cmd, shell=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        time.sleep(0.5)
    except Exception:
        pass

def wait_for_http(url, timeout_sec=35, service_name="Service"):
    """Poll an HTTP endpoint until 200 OK or timeout."""
    start_time = time.time()
    while time.time() - start_time < timeout_sec:
        if shutdown_event.is_set():
            return False
        try:
            req = urllib.request.Request(url, headers={'User-Agent': 'Taplay-Runner'})
            with urllib.request.urlopen(req, timeout=1.5) as resp:
                if resp.status in (200, 204):
                    return True
        except Exception:
            pass
        time.sleep(1.0)
    return False

def start_fraud_service():
    """Start Python ML Fraud Detection Service."""
    log(f"Starting Python ML Fraud Detection Service on port {FRAUD_PORT}...", "ECO")
    kill_port(FRAUD_PORT)

    venv_python = os.path.join(FRAUD_DIR, "venv", "bin", "python")
    python_bin = venv_python if os.path.exists(venv_python) else sys.executable

    log_file = open(os.path.join(PROJECT_ROOT, "fraud_service.log"), "w")
    proc = subprocess.Popen(
        [python_bin, "main.py"],
        cwd=FRAUD_DIR,
        stdout=log_file,
        stderr=subprocess.STDOUT
    )
    active_processes.append((proc, "ML Fraud Detection Service", log_file))

    health_url = f"http://127.0.0.1:{FRAUD_PORT}/health"
    if wait_for_http(health_url, timeout_sec=10, service_name="Fraud Service"):
        log(f"Fraud Detection Service is {Colors.GREEN}ONLINE{Colors.RESET} at http://127.0.0.1:{FRAUD_PORT}", "SUCCESS")
        return True
    else:
        log("Fraud Detection Service startup timed out. Check fraud_service.log", "ERROR")
        return False

def start_backend():
    """Start Spring Boot Backend & Arcade Server."""
    log(f"Starting Spring Boot Backend on port {BACKEND_PORT}...", "ECO")
    kill_port(BACKEND_PORT)

    log_file = open(os.path.join(PROJECT_ROOT, "backend.log"), "w")

    if os.path.exists(JAR_PATH):
        cmd = ["java", "-jar", JAR_PATH]
        log(f"Using precompiled JAR: {os.path.basename(JAR_PATH)}", "INFO")
    else:
        cmd = ["mvn", "spring-boot:run"]
        log("JAR not found, launching with Maven spring-boot:run...", "INFO")

    proc = subprocess.Popen(
        cmd,
        cwd=BACKEND_DIR,
        stdout=log_file,
        stderr=subprocess.STDOUT
    )
    active_processes.append((proc, "Spring Boot Backend", log_file))

    health_url = f"http://127.0.0.1:{BACKEND_PORT}/api/v1/user/profile"
    if wait_for_http(health_url, timeout_sec=40, service_name="Spring Boot"):
        log(f"Spring Boot Backend is {Colors.GREEN}ONLINE{Colors.RESET} at http://127.0.0.1:{BACKEND_PORT}", "SUCCESS")
        return True
    else:
        log("Backend startup timed out. Check backend.log", "ERROR")
        return False

def adb_command(args, device=None, timeout=5):
    """Run an ADB command and return stdout string."""
    cmd = ["adb"]
    if device:
        cmd.extend(["-s", device])
    cmd.extend(args)
    try:
        res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, timeout=timeout)
        return res.stdout.strip()
    except subprocess.TimeoutExpired:
        return ""
    except Exception as e:
        return ""

def is_device_connected(target_addr):
    """Check if target address is listed as an authorized device."""
    output = adb_command(["devices"])
    for line in output.splitlines():
        parts = line.split()
        if len(parts) >= 2 and parts[0] == target_addr and parts[1] == "device":
            return True
    return False

def setup_device(addr, device_type):
    """Connect to device via ADB, setup port forwarding, install APK, and launch app."""
    if not addr or addr.strip() == "":
        return False

    addr = addr.strip()
    log(f"Connecting to {device_type} at {Colors.BOLD}{addr}{Colors.RESET}...", "ECO")

    # If it's an IP:PORT format, execute adb connect
    if ":" in addr:
        res = adb_command(["connect", addr], timeout=8)
        log(f"ADB Connect result: {res}", "INFO")

    if not is_device_connected(addr):
        log(f"Could not connect to {device_type} ({addr}).", "WARN")
        print(f"    {Colors.YELLOW}Tips to connect {device_type}:{Colors.RESET}")
        print(f"    1. Verify screen is AWAKE and unlocked.")
        print(f"    2. Go to Settings -> Developer Options -> Wireless Debugging -> Turn ON.")
        print(f"    3. Ensure the IP and 5-digit port match the number shown on the device screen.")
        print(f"    4. Check both laptop and device are on the same Wi-Fi network.")
        return False

    log(f"{device_type} connected successfully!", "SUCCESS")

    # Reverse ports for backend (:8080) and fraud (:5000)
    rev8080 = adb_command(["reverse", f"tcp:{BACKEND_PORT}", f"tcp:{BACKEND_PORT}"], device=addr)
    rev5000 = adb_command(["reverse", f"tcp:{FRAUD_PORT}", f"tcp:{FRAUD_PORT}"], device=addr)
    log(f"Configured ADB reverse tcp:{BACKEND_PORT} and tcp:{FRAUD_PORT} on {device_type}.", "SUCCESS")

    # Optional APK installation
    if AUTO_INSTALL_APKS:
        apk_path = WATCH_APK if device_type.lower() == "watch" else PHONE_APK
        if os.path.exists(apk_path):
            log(f"Installing {os.path.basename(apk_path)} on {device_type}...", "INFO")
            install_res = adb_command(["install", "-r", "-d", apk_path], device=addr, timeout=30)
            log(f"Install result: {install_res}", "INFO")

    # Launch apps
    if AUTO_LAUNCH_APPS:
        if device_type.lower() == "watch":
            log("Launching Taplay Wear on Galaxy Watch...", "INFO")
            adb_command(["shell", "am", "start", "-n", "com.taplay.wear/com.taplay.wear.MainActivity"], device=addr)
        else:
            log("Launching Taplay Arcade Emulator on Phone...", "INFO")
            adb_command(["shell", "am", "start", "-n", "com.taplay.app.emulator/com.taplay.app.MainActivity"], device=addr)

    return True

def device_watcher(phone_addr, watch_addr):
    """Background watcher that keeps trying to reconnect devices if Wi-Fi drops."""
    phone_ok = False
    watch_ok = False

    while not shutdown_event.is_set():
        if phone_addr:
            if not is_device_connected(phone_addr):
                if setup_device(phone_addr, "Phone"):
                    phone_ok = True
            elif not phone_ok:
                phone_ok = True

        if watch_addr:
            if not is_device_connected(watch_addr):
                if setup_device(watch_addr, "Galaxy Watch"):
                    watch_ok = True
            elif not watch_ok:
                watch_ok = True

        time.sleep(10)

def cleanup_and_exit(signum=None, frame=None):
    """Handle graceful shutdown of all processes."""
    print()
    log("Shutting down Taplay services...", "WARN")
    shutdown_event.set()

    for proc, name, log_file in active_processes:
        try:
            log(f"Stopping {name} (PID: {proc.pid})...", "INFO")
            proc.terminate()
            proc.wait(timeout=3)
        except Exception:
            try:
                proc.kill()
            except Exception:
                pass
        finally:
            if log_file and not log_file.closed:
                log_file.close()

    try:
        subprocess.run(["adb", "reverse", "--remove-all"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    except Exception:
        pass

    log("All services terminated cleanly. Have a great day!", "SUCCESS")
    sys.exit(0)

def main():
    parser = argparse.ArgumentParser(description="Taplay All-in-One Runner")
    parser.add_argument("--phone", default=PHONE_ADDRESS, help="Phone ADB address (e.g. 192.168.0.128:34231)")
    parser.add_argument("--watch", default=WATCH_ADDRESS, help="Galaxy Watch ADB address (e.g. 192.168.0.127:35973)")
    parser.add_argument("--install", action="store_true", help="Force reinstall APKs on connected devices")
    args = parser.parse_args()

    phone_addr = args.phone
    watch_addr = args.watch

    global AUTO_INSTALL_APKS
    if args.install:
        AUTO_INSTALL_APKS = True

    signal.signal(signal.SIGINT, cleanup_and_exit)
    signal.signal(signal.SIGTERM, cleanup_and_exit)

    lan_ip = get_lan_ip()

    print(f"""
{Colors.CYAN}{Colors.BOLD}========================================================================
    🕹️   T A P L A Y   U N I F I E D   E C O S Y S T E M   R U N N E R
========================================================================{Colors.RESET}
  {Colors.BOLD}Laptop Wi-Fi IP :{Colors.RESET} {Colors.GREEN}{lan_ip}{Colors.RESET}
  {Colors.BOLD}Phone Target    :{Colors.RESET} {phone_addr or '(None)'}
  {Colors.BOLD}Watch Target    :{Colors.RESET} {watch_addr or '(None)'}
  {Colors.BOLD}Web Dashboard   :{Colors.RESET} {Colors.CYAN}http://localhost:{BACKEND_PORT}{Colors.RESET} (or http://{lan_ip}:{BACKEND_PORT})
  {Colors.BOLD}Fraud Service   :{Colors.RESET} http://localhost:{FRAUD_PORT}
========================================================================
""")

    # 1. Start Fraud Service
    if not start_fraud_service():
        log("Failed to start Fraud Detection Service. Exiting.", "ERROR")
        cleanup_and_exit()

    # 2. Start Spring Boot Backend
    if not start_backend():
        log("Failed to start Spring Boot Backend. Exiting.", "ERROR")
        cleanup_and_exit()

    # 3. Setup Devices
    setup_device(phone_addr, "Phone")
    setup_device(watch_addr, "Galaxy Watch")

    # 4. Start Background Watcher thread
    watcher_thread = threading.Thread(target=device_watcher, args=(phone_addr, watch_addr), daemon=True)
    watcher_thread.start()

    print(f"""
{Colors.GREEN}{Colors.BOLD}========================================================================
  🚀  ALL TAPLAY SERVICES ARE LIVE AND OPERATIONAL!
========================================================================{Colors.RESET}
  🌐 {Colors.BOLD}Open Arcade Cabinet Dashboard in your Browser:{Colors.RESET}
     {Colors.CYAN}http://localhost:{BACKEND_PORT}{Colors.RESET}   or   {Colors.CYAN}http://{lan_ip}:{BACKEND_PORT}{Colors.RESET}

  💳 {Colors.BOLD}NFC Tap Testing Flow:{Colors.RESET}
     1. Phone shows 'Taplay Emulator' (MACH-PHONE-2)
     2. Watch shows 'Taplay Wear' (CARD-WATCH-01, Balance: ₹540)
     3. Tap Watch to back of Phone -> Galaga Space Arcade launches!
     4. Live Session Controls on Watch: Pause / Resume / Stop

  {Colors.YELLOW}Press Ctrl+C at any time to gracefully stop all services.{Colors.RESET}
========================================================================
""")

    # Keep main thread alive
    try:
        while not shutdown_event.is_set():
            time.sleep(1)
    except KeyboardInterrupt:
        cleanup_and_exit()

if __name__ == "__main__":
    main()
