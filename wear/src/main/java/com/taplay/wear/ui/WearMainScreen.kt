package com.taplay.wear.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import kotlin.math.cos
import kotlin.math.sin

// ── Taplay Arcade colour palette ──────────────────────────────────────────

private val NeonCyan    = Color(0xFF00F5FF)
private val NeonPurple  = Color(0xFFBB86FC)
private val NeonGold    = Color(0xFFFFD600)
private val ArcadeDark  = Color(0xFF0A0E1A)
private val ArcadeMid   = Color(0xFF121929)
private val DangerRed   = Color(0xFFFF4444)
private val PauseAmber  = Color(0xFFFFAB00)

// ── Root entry point ──────────────────────────────────────────────────────

@Composable
fun WearMainScreen(vm: WearViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ArcadeDark),
        contentAlignment = Alignment.Center
    ) {
        when (state.screen) {
            WearScreenState.IDLE       -> IdleScreen(state, vm)
            WearScreenState.ACTIVE     -> ActiveScreen(state, vm)
            WearScreenState.PAUSED     -> PausedScreen(state, vm)
            WearScreenState.CONNECTING -> ConnectingScreen()
            WearScreenState.ERROR      -> ErrorScreen(state, vm)
        }
    }
}

// ── IDLE — "TAP TO PLAY" ─────────────────────────────────────────────────

@Composable
private fun IdleScreen(state: WearUiState, vm: WearViewModel) {
    // Pulsing ring animation
    val pulse = rememberInfiniteTransition(label = "pulse")
    val ringAlpha by pulse.animateFloat(
        initialValue = 0.25f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "ringAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(8.dp)
    ) {
        // Arcade machine chip label
        Text(
            text = state.machineId,
            color = NeonCyan.copy(alpha = 0.7f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(4.dp))

        // Pulsing NFC icon ring
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = ringAlpha * 0.15f))
                .drawBehind {
                    drawCircle(
                        color = NeonCyan.copy(alpha = ringAlpha),
                        radius = size.minDimension / 2f - 3.dp.toPx(),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text("⟨NFC⟩", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        }

        Spacer(Modifier.height(6.dp))

        Text(
            "TAP TO PLAY",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )

        Spacer(Modifier.height(2.dp))

        Text(
            "Hold watch near\narcade terminal",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 9.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        // Wallet balance chip
        WalletChip(balance = state.balance)

        // Tap counter
        if (state.tapCount > 0) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Taps: ${state.tapCount}",
                color = NeonGold.copy(alpha = 0.7f),
                fontSize = 9.sp
            )
        }
    }
}

// ── ACTIVE — countdown + controls ────────────────────────────────────────

@Composable
private fun ActiveScreen(state: WearUiState, vm: WearViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(6.dp)
    ) {
        Text(
            state.gameName.take(18),
            color = NeonPurple,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(3.dp))

        // Circular countdown
        CircularCountdown(
            timeRemaining = state.timeRemaining,
            totalDuration = state.sessionDuration,
            modifier = Modifier.size(72.dp)
        )

        Spacer(Modifier.height(4.dp))

        Text(
            "GAME IN PROGRESS",
            color = NeonCyan,
            fontSize = 8.sp,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(6.dp))

        // Controls row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CompactButton(
                onClick = { vm.pauseSession() },
                colors = ButtonDefaults.buttonColors(backgroundColor = PauseAmber)
            ) {
                Text("⏸", fontSize = 10.sp)
            }
            CompactButton(
                onClick = { vm.endSession() },
                colors = ButtonDefaults.buttonColors(backgroundColor = DangerRed)
            ) {
                Text("⏹", fontSize = 10.sp)
            }
        }
    }
}

// ── PAUSED ────────────────────────────────────────────────────────────────

@Composable
private fun PausedScreen(state: WearUiState, vm: WearViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(8.dp)
    ) {
        Text("PAUSED", color = PauseAmber, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(4.dp))
        Text(
            "${state.timeRemaining}s left",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CompactButton(
                onClick = { vm.resumeSession() },
                colors = ButtonDefaults.buttonColors(backgroundColor = NeonCyan)
            ) {
                Text("▶", fontSize = 10.sp, color = ArcadeDark)
            }
            CompactButton(
                onClick = { vm.endSession() },
                colors = ButtonDefaults.buttonColors(backgroundColor = DangerRed)
            ) {
                Text("⏹", fontSize = 10.sp)
            }
        }
    }
}

// ── CONNECTING ────────────────────────────────────────────────────────────

@Composable
private fun ConnectingScreen() {
    CircularProgressIndicator(
        modifier = Modifier.size(40.dp),
        indicatorColor = NeonCyan,
        trackColor = NeonCyan.copy(alpha = 0.15f)
    )
}

// ── ERROR ─────────────────────────────────────────────────────────────────

@Composable
private fun ErrorScreen(state: WearUiState, vm: WearViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(10.dp)
    ) {
        Text("⚠", color = DangerRed, fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            state.lastError ?: "Connection error",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 9.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        CompactButton(
            onClick = { vm.fetchUserProfile(); vm.startPolling() },
            colors = ButtonDefaults.buttonColors(backgroundColor = NeonCyan)
        ) {
            Text("Retry", fontSize = 9.sp, color = ArcadeDark)
        }
    }
}

// ── Shared composables ────────────────────────────────────────────────────

@Composable
private fun WalletChip(balance: Double) {
    Box(
        modifier = Modifier
            .background(
                brush = Brush.horizontalGradient(
                    listOf(NeonPurple.copy(alpha = 0.25f), NeonCyan.copy(alpha = 0.15f))
                ),
                shape = CircleShape
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "₹${"%.0f".format(balance)}",
            color = NeonGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Draws an arc-based countdown ring similar to a watchface complication.
 */
@Composable
private fun CircularCountdown(
    timeRemaining: Int,
    totalDuration: Int,
    modifier: Modifier = Modifier
) {
    val fraction = if (totalDuration > 0) timeRemaining.toFloat() / totalDuration else 0f
    val sweepAngle = fraction * 360f

    val progressColor = when {
        fraction > 0.5f -> NeonCyan
        fraction > 0.25f -> NeonGold
        else -> DangerRed
    }

    Box(
        modifier = modifier.drawBehind {
            val stroke = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
            val inset = 4.dp.toPx()
            // Background track
            drawArc(
                color = progressColor.copy(alpha = 0.18f),
                startAngle = -90f, sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(size.width - inset * 2, size.height - inset * 2),
                style = stroke
            )
            // Progress arc
            drawArc(
                color = progressColor,
                startAngle = -90f, sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(size.width - inset * 2, size.height - inset * 2),
                style = stroke
            )
        },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatTime(timeRemaining),
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "left",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 8.sp
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}
