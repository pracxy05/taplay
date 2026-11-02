package com.taplay.app.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

// ── Retro Arcade Galaga Models ──────────────────────────────────────────────

private data class Star(var x: Float, var y: Float, val speed: Float, val size: Float, val alpha: Float)
private data class Laser(var x: Float, var y: Float, val speed: Float = 18f)
private data class Alien(
    var x: Float,
    var y: Float,
    val initialX: Float,
    val initialY: Float,
    val type: Int, // 0: butterfly, 1: wasp, 2: boss
    var isAlive: Boolean = true,
    var hitFlash: Int = 0
)
private data class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float, var alpha: Float, val color: Color)

@Composable
fun EmulatorScreen(viewModel: EmulatorViewModel) {
    val sessionStatusState by viewModel.sessionStatus.collectAsState()

    val rawStatus = sessionStatusState?.sessionStatus ?: "IDLE"
    val isActive = rawStatus.equals("ACTIVE", ignoreCase = true)
    val isPaused = rawStatus.equals("PAUSED", ignoreCase = true)
    val duration = sessionStatusState?.sessionDuration ?: 300
    val timeRemaining = sessionStatusState?.timeRemaining ?: 0

    val context = androidx.compose.ui.platform.LocalContext.current

    // Listen for Physical Galaxy Watch NFC taps with exclusive high system priority
    DisposableEffect(Unit) {
        val nfcAdapter = android.nfc.NfcAdapter.getDefaultAdapter(context)
        val activity = context as? android.app.Activity
        if (nfcAdapter != null && activity != null && nfcAdapter.isEnabled) {
            val options = android.os.Bundle()
            options.putInt(android.nfc.NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 100)
            nfcAdapter.enableReaderMode(
                activity,
                { tag: android.nfc.Tag? ->
                    tag?.let {
                        var resolvedTagId = "CARD-WATCH-01"
                        val isoDep = android.nfc.tech.IsoDep.get(it)
                        if (isoDep != null) {
                            try {
                                isoDep.connect()
                                isoDep.timeout = 2500
                                // SELECT AID: F0 01 02 03 04 05 06
                                val selectApdu = byteArrayOf(
                                    0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(),
                                    0x07.toByte(), 0xF0.toByte(), 0x01.toByte(), 0x02.toByte(),
                                    0x03.toByte(), 0x04.toByte(), 0x05.toByte(), 0x06.toByte(), 0x00.toByte()
                                )
                                isoDep.transceive(selectApdu)
                                
                                // READ BINARY: 00 B0 00 00 00
                                val readApdu = byteArrayOf(0x00.toByte(), 0xB0.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte())
                                val readRes = isoDep.transceive(readApdu)
                                if (readRes.size >= 2) {
                                    val ascii = String(readRes.dropLast(2).toByteArray(), Charsets.UTF_8).trim()
                                    if (ascii.isNotBlank()) resolvedTagId = ascii
                                }
                            } catch (e: Exception) {
                                android.util.Log.w("EmulatorNFC", "IsoDep transceive notice: ${e.message}")
                            } finally {
                                runCatching { isoDep.close() }
                            }
                        } else {
                            val tagBytes = it.id
                            val hex = tagBytes.joinToString("") { b -> "%02X".format(b) }
                            if (hex.isNotBlank()) resolvedTagId = hex
                        }
                        
                        android.util.Log.i("EmulatorNFC", "Watch tap received: $resolvedTagId -> triggering startTransaction")
                        activity.runOnUiThread {
                            viewModel.onNfcTagDetected(resolvedTagId)
                        }
                    }
                },
                android.nfc.NfcAdapter.FLAG_READER_NFC_A or
                android.nfc.NfcAdapter.FLAG_READER_NFC_B or
                android.nfc.NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or
                android.nfc.NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                options
            )
        }
        onDispose {
            val act = context as? android.app.Activity
            if (nfcAdapter != null && act != null) {
                nfcAdapter.disableReaderMode(act)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070510)),
        contentAlignment = Alignment.Center
    ) {
        if (isActive || isPaused) {
            GalagaArcadeGame(
                timeRemaining = timeRemaining,
                totalDuration = duration,
                isPaused = isPaused,
                machineId = sessionStatusState?.machineId ?: "MACH-PHONE-2"
            )
        } else {
            EmulatorIdleCabinet(
                machineId = "MACH-PHONE-2"
            )
        }
    }
}

// ── Galaga Arcade Game ──────────────────────────────────────────────────────

@Composable
private fun GalagaArcadeGame(
    timeRemaining: Int,
    totalDuration: Int,
    isPaused: Boolean,
    machineId: String
) {
    var score by remember { mutableIntStateOf(0) }
    var playerX by remember { mutableFloatStateOf(0.5f) } // 0f to 1f screen width
    val lasers = remember { mutableStateListOf<Laser>() }
    val aliens = remember { mutableStateListOf<Alien>() }
    val particles = remember { mutableStateListOf<Particle>() }
    val stars = remember {
        mutableStateListOf<Star>().apply {
            repeat(45) {
                add(Star(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 4f + 2f, Random.nextFloat() * 2f + 1f, Random.nextFloat() * 0.7f + 0.3f))
            }
        }
    }

    // Initialize Alien Fleet if empty
    LaunchedEffect(Unit) {
        if (aliens.isEmpty()) {
            for (row in 0..2) {
                for (col in 0..5) {
                    val initialX = 0.15f + col * 0.14f
                    val initialY = 0.18f + row * 0.08f
                    aliens.add(Alien(initialX, initialY, initialX, initialY, type = row))
                }
            }
        }
    }

    // Game loop running at ~60fps
    LaunchedEffect(isPaused) {
        var frame = 0L
        while (!isPaused) {
            frame++

            // Move stars
            stars.forEach { star ->
                star.y += star.speed / 800f
                if (star.y > 1f) {
                    star.y = 0f
                    star.x = Random.nextFloat()
                }
            }

            // Fire laser every 15 frames
            if (frame % 14 == 0L) {
                lasers.add(Laser(x = playerX, y = 0.82f))
            }

            // Move lasers
            val laserIterator = lasers.iterator()
            while (laserIterator.hasNext()) {
                val l = laserIterator.next()
                l.y -= l.speed / 1000f
                if (l.y < 0.05f) {
                    laserIterator.remove()
                }
            }

            // Sway & animate aliens
            val sway = (sin(frame * 0.06) * 0.04).toFloat()
            aliens.forEach { alien ->
                if (alien.isAlive) {
                    alien.x = alien.initialX + sway
                    if (alien.hitFlash > 0) alien.hitFlash--
                }
            }

            // Collision detection (lasers vs aliens)
            val toRemoveLasers = mutableSetOf<Laser>()
            lasers.forEach { laser ->
                aliens.forEach { alien ->
                    if (alien.isAlive) {
                        val dx = (laser.x - alien.x)
                        val dy = (laser.y - alien.y)
                        if (dx * dx + dy * dy < 0.003f) {
                            alien.isAlive = false
                            toRemoveLasers.add(laser)
                            score += when (alien.type) {
                                0 -> 400
                                1 -> 200
                                else -> 100
                            }
                            // Spawn explosion particles
                            repeat(8) {
                                val angle = Random.nextFloat() * 6.28f
                                val speed = Random.nextFloat() * 0.015f + 0.005f
                                particles.add(
                                    Particle(
                                        x = alien.x,
                                        y = alien.y,
                                        vx = (kotlin.math.cos(angle) * speed).toFloat(),
                                        vy = (sin(angle) * speed).toFloat(),
                                        alpha = 1f,
                                        color = if (alien.type == 0) Color(0xFFFF0055) else Color(0xFF00E5FF)
                                    )
                                )
                            }
                        }
                    }
                }
            }
            lasers.removeAll(toRemoveLasers)

            // Respawn wave if all aliens cleared
            if (aliens.isNotEmpty() && aliens.none { it.isAlive }) {
                aliens.forEach {
                    it.isAlive = true
                    it.hitFlash = 0
                }
                score += 1000 // Wave bonus
            }

            // Update particles
            val particleIterator = particles.iterator()
            while (particleIterator.hasNext()) {
                val p = particleIterator.next()
                p.x += p.vx
                p.y += p.vy
                p.alpha -= 0.05f
                if (p.alpha <= 0f) {
                    particleIterator.remove()
                }
            }

            delay(16) // ~60fps
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val newX = playerX + (dragAmount.x / size.width)
                    playerX = newX.coerceIn(0.08f, 0.92f)
                }
            }
    ) {
        // Space Canvas (Stars, Ships, Lasers, Aliens, Particles)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Draw Starfield
            stars.forEach { s ->
                drawCircle(
                    color = Color.White.copy(alpha = s.alpha),
                    radius = s.size,
                    center = Offset(s.x * w, s.y * h)
                )
            }

            // 2. Draw Lasers
            lasers.forEach { laser ->
                drawLine(
                    color = Color(0xFF00F5FF),
                    start = Offset(laser.x * w, laser.y * h),
                    end = Offset(laser.x * w, (laser.y - 0.03f) * h),
                    strokeWidth = 6f
                )
                drawCircle(
                    color = Color(0xFFFF00E5),
                    radius = 4f,
                    center = Offset(laser.x * w, laser.y * h)
                )
            }

            // 3. Draw Aliens
            aliens.forEach { alien ->
                if (alien.isAlive) {
                    drawGalagaAlien(alien, w, h)
                }
            }

            // 4. Draw Particles
            particles.forEach { p ->
                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = 4f,
                    center = Offset(p.x * w, p.y * h)
                )
            }

            // 5. Draw Player Spaceship
            drawPlayerShip(playerX * w, 0.85f * h)
        }

        // Top Arcade HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SCORE",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "%06d".format(score),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Session Countdown Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF1E1040).copy(alpha = 0.85f))
                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val minutes = timeRemaining / 60
                    val seconds = timeRemaining % 60
                    Text(
                        text = "⏱ %02d:%02d".format(minutes, seconds),
                        color = if (timeRemaining <= 30) Color(0xFFFF4444) else Color(0xFF00F5FF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = machineId,
                    color = Color(0xFFBB86FC),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "LIVE SESSION",
                    color = Color(0xFF10B981),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Controls hint at bottom
        Text(
            text = "◀ Drag finger to steer spaceship ▶",
            color = Color.White.copy(alpha = 0.35f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )

        // Paused Overlay (Wrist Control Sync)
        if (isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC070510)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(text = "⏸", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "GAME PAUSED",
                        color = Color(0xFFFFAB00),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Controlled from Galaxy Watch",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Tap ▶ Resume on your wrist to continue",
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ── Draw Helper Functions ───────────────────────────────────────────────────

private fun DrawScope.drawGalagaAlien(alien: Alien, w: Float, h: Float) {
    val cx = alien.x * w
    val cy = alien.y * h
    val r = 16f

    val color = when (alien.type) {
        0 -> Color(0xFFFF1744) // Red Boss
        1 -> Color(0xFFFFD600) // Yellow Butterfly
        else -> Color(0xFF00E5FF) // Cyan Wasp
    }

    // Alien Body
    val path = Path().apply {
        moveTo(cx, cy - r)
        lineTo(cx + r, cy)
        lineTo(cx + r * 0.7f, cy + r)
        lineTo(cx - r * 0.7f, cy + r)
        lineTo(cx - r, cy)
        close()
    }
    drawPath(path, color)

    // Alien Wings
    drawLine(color = color, start = Offset(cx - r * 1.5f, cy - r * 0.5f), end = Offset(cx, cy), strokeWidth = 3f)
    drawLine(color = color, start = Offset(cx + r * 1.5f, cy - r * 0.5f), end = Offset(cx, cy), strokeWidth = 3f)

    // Eyes
    drawCircle(Color.White, radius = 2.5f, center = Offset(cx - 4f, cy - 2f))
    drawCircle(Color.White, radius = 2.5f, center = Offset(cx + 4f, cy - 2f))
}

private fun DrawScope.drawPlayerShip(x: Float, y: Float) {
    val shipPath = Path().apply {
        moveTo(x, y - 28f)           // Nose
        lineTo(x + 22f, y + 16f)      // Right wingtip
        lineTo(x + 10f, y + 10f)      // Right thruster indent
        lineTo(x, y + 14f)            // Center engine
        lineTo(x - 10f, y + 10f)      // Left thruster indent
        lineTo(x - 22f, y + 16f)      // Left wingtip
        close()
    }

    // Ship Body Gradient
    drawPath(shipPath, Brush.verticalGradient(listOf(Color(0xFF00F5FF), Color(0xFF7C3AED))))

    // Cockpit
    drawCircle(Color.White, radius = 4f, center = Offset(x, y - 8f))

    // Thruster Flames
    drawLine(Color(0xFFFF5252), Offset(x - 6f, y + 14f), Offset(x - 6f, y + 26f), strokeWidth = 4f)
    drawLine(Color(0xFFFFD600), Offset(x, y + 14f), Offset(x, y + 30f), strokeWidth = 5f)
    drawLine(Color(0xFFFF5252), Offset(x + 6f, y + 14f), Offset(x + 6f, y + 26f), strokeWidth = 4f)
}

// ── Cabinet Idle UI ─────────────────────────────────────────────────────────

@Composable
private fun EmulatorIdleCabinet(machineId: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "cabinetPulse")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "ringScale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "ringAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Cabinet Header
        Text(
            text = "ARCADE CABINET EMULATOR",
            color = Color(0xFFBB86FC),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = machineId,
            color = Color(0xFF00E5FF),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Status Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF00E5FF).copy(alpha = 0.12f))
                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), RoundedCornerShape(50))
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            Text(
                text = "● NFC EMULATION ACTIVE",
                color = Color(0xFF00E5FF),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Glowing NFC Target Target
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(240.dp * ringScale)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFF00E5FF).copy(alpha = ringAlpha), CircleShape)
            )

            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFF7C3AED).copy(alpha = 0.25f),
                                Color(0xFF00E5FF).copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(2.dp, Color(0xFF7C3AED).copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "👾", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "HOLD GALAXY WATCH",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "OR NFC CARD HERE",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(44.dp))

        // Instruction Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF140F24))
                .border(1.dp, Color(0xFF261D42), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "READY FOR INSTANT PLAY",
                    color = Color(0xFFFFD600),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "1 Tap = ₹10 (10 Credits) · 5 Min Galaga Session",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}