package com.taplay.app.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import com.taplay.app.ui.theme.*

// ── Tap flow stages
enum class TapStage {
    WAITING,        // NFC ring pulsing, waiting for tap
    DETECTED,       // Machine found, showing game details
    CONFIRMED,      // User confirmed, game starting animation
    STARTED         // Navigate to session
}

// ── Mock game data (replace with real NFC data later)
data class ArcadeGame(
    val name: String,
    val machine: String,
    val costCredits: Int,
    val costTokens: Int,
    val genre: String,
    val difficulty: String,
    val tip1: String,
    val tip2: String,
    val tip3: String,
    val emoji: String
)

val mockGame = ArcadeGame(
    name        = "Air Hockey Blitz",
    machine     = "Machine #A14",
    costCredits = 10,
    costTokens  = 2,
    genre       = "Sports · Arcade",
    difficulty  = "Medium",
    tip1        = "Aim for the corners — most players defend the center",
    tip2        = "Short quick shots beat long power shots every time",
    tip3        = "Watch your opponent's mallet, not the puck",
    emoji       = "🏒"
)

@Composable
fun TapScreen(
    onSessionStarted: () -> Unit,
    onBack: () -> Unit
) {
    var stage by remember { mutableStateOf(TapStage.WAITING) }
    var selectedCharacter by remember { mutableStateOf("Blaze") }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showGameStarted by remember { mutableStateOf(false) }

    // Mock user balance
    val userBalance = 85
    val hasEnoughBalance = userBalance >= mockGame.costCredits

    // Simulate NFC tag detection after 3 seconds (replace with real NFC intent later)
    LaunchedEffect(Unit) {
        delay(3200)
        if (stage == TapStage.WAITING) {
            stage = TapStage.DETECTED
        }
    }

    // Game started animation then navigate
    LaunchedEffect(showGameStarted) {
        if (showGameStarted) {
            delay(2200)
            onSessionStarted()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D0A18),
                        Color(0xFF110E20),
                        Color(0xFF0A0812)
                    )
                )
            )
    ) {
        // ── Main content
        if (!showGameStarted) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Top bar
                TapTopBar(
                    stage = stage,
                    onBackPressed = {
                        when (stage) {
                            TapStage.WAITING   -> onBack()
                            TapStage.DETECTED  -> showCancelDialog = true
                            TapStage.CONFIRMED -> { /* locked */ }
                            TapStage.STARTED   -> { /* locked */ }
                        }
                    }
                )

                AnimatedContent(
                    targetState = stage,
                    transitionSpec = {
                        (fadeIn(tween(400)) + slideInVertically { it / 2 }).togetherWith(
                            fadeOut(tween(200))
                        )
                    },
                    label = "tapStage"
                ) { currentStage ->
                    when (currentStage) {
                        TapStage.WAITING -> WaitingForTap()

                        TapStage.DETECTED -> GameDetectedContent(
                            game            = mockGame,
                            userBalance     = userBalance,
                            hasEnoughBalance = hasEnoughBalance,
                            selectedCharacter = selectedCharacter,
                            onCharacterChange = { selectedCharacter = it },
                            onConfirm = {
                                stage = TapStage.CONFIRMED
                                showGameStarted = true
                            },
                            onAddMoney = { /* navigate to wallet */ }
                        )

                        TapStage.CONFIRMED, TapStage.STARTED -> { /* handled by overlay */ }
                    }
                }
            }
        }

        // ── Game started overlay animation
        if (showGameStarted) {
            GameStartedOverlay(
                gameName = mockGame.name,
                character = selectedCharacter,
                emoji = mockGame.emoji
            )
        }

        // ── Cancel dialog
        if (showCancelDialog) {
            CancelGameDialog(
                onCancel = {
                    showCancelDialog = false
                    onBack()
                },
                onStay = { showCancelDialog = false }
            )
        }
    }
}

// ─────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────
@Composable
private fun TapTopBar(stage: TapStage, onBackPressed: () -> Unit) {
    val isLocked = stage == TapStage.CONFIRMED || stage == TapStage.STARTED

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { if (!isLocked) onBackPressed() },
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (isLocked) Color(0xFF1A1A2A) else Color(0xFF1E1930))
                .alpha(if (isLocked) 0.3f else 1f)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = when (stage) {
                TapStage.WAITING   -> "Tap to Play"
                TapStage.DETECTED  -> "Game Found"
                else               -> "Starting..."
            },
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─────────────────────────────────────────
// STAGE 1 — WAITING FOR NFC TAP
// ─────────────────────────────────────────
@Composable
private fun WaitingForTap() {
    val infinite = rememberInfiniteTransition(label = "nfcRing")

    val ring1Scale by infinite.animateFloat(
        initialValue = 1f, targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(1400, easing = EaseOutCubic), RepeatMode.Restart),
        label = "r1"
    )
    val ring1Alpha by infinite.animateFloat(
        initialValue = 0.7f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1400, easing = EaseOutCubic), RepeatMode.Restart),
        label = "a1"
    )
    val ring2Scale by infinite.animateFloat(
        initialValue = 1f, targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            tween(1400, easing = EaseOutCubic, delayMillis = 400),
            RepeatMode.Restart
        ),
        label = "r2"
    )
    val ring2Alpha by infinite.animateFloat(
        initialValue = 0.5f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(1400, easing = EaseOutCubic, delayMillis = 400),
            RepeatMode.Restart
        ),
        label = "a2"
    )
    val ring3Scale by infinite.animateFloat(
        initialValue = 1f, targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            tween(1400, easing = EaseOutCubic, delayMillis = 800),
            RepeatMode.Restart
        ),
        label = "r3"
    )
    val ring3Alpha by infinite.animateFloat(
        initialValue = 0.35f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(1400, easing = EaseOutCubic, delayMillis = 800),
            RepeatMode.Restart
        ),
        label = "a3"
    )

    val dotPulse by infinite.animateFloat(
        initialValue = 0.9f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "dot"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Expanding rings
        listOf(
            ring1Scale to ring1Alpha,
            ring2Scale to ring2Alpha,
            ring3Scale to ring3Alpha
        ).forEach { (rScale, rAlpha) ->
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(rScale)
                    .alpha(rAlpha)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = TaplayCyan,
                        shape = CircleShape
                    )
            )
        }

        // Center circle
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(dotPulse)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            TaplayCyan.copy(alpha = 0.25f),
                            TaplayCyan.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .border(2.dp, TaplayCyan.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("NFC", color = TaplayCyan, fontSize = 13.sp, letterSpacing = 4.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("●●●", color = TaplayCyan.copy(alpha = 0.6f), fontSize = 10.sp)
            }
        }

        // Instruction text at bottom of screen
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hold your phone near",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "the arcade machine",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Keep the phone steady until the machine is detected",
                color = TaplayTextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp)
            )
        }
    }
}

// ─────────────────────────────────────────
// STAGE 2 — GAME DETECTED
// ─────────────────────────────────────────
@Composable
private fun GameDetectedContent(
    game: ArcadeGame,
    userBalance: Int,
    hasEnoughBalance: Boolean,
    selectedCharacter: String,
    onCharacterChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onAddMoney: () -> Unit
) {
    val characters = listOf("Blaze", "Nova", "Rex", "Cipher", "Astra")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── Game picture card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1E1040), Color(0xFF2A1260), Color(0xFF1A0D38))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(game.emoji, fontSize = 72.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = game.name,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = game.machine,
                    color = TaplayCyan,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }
            // Detected badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF22C55E).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFF22C55E), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(14.dp)
                    )
                    Text("Detected", color = Color(0xFF22C55E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ── Game info + cost card
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0EEFF))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Genre", color = Color(0xFF6B6584), fontSize = 11.sp)
                        Text(game.genre, color = Color(0xFF1E1840), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Difficulty", color = Color(0xFF6B6584), fontSize = 11.sp)
                        Text(game.difficulty, color = Color(0xFF1E1840), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFDDD8F0))
                Spacer(modifier = Modifier.height(16.dp))

                Text("Cost Breakdown", color = Color(0xFF4A4568), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CostChip("${game.costCredits} Credits", Color(0xFFCBE0FF), Color(0xFF1A3A6A), Modifier.weight(1f))
                    CostChip("${game.costTokens} Tokens", Color(0xFFFFF5E8), Color(0xFF5A3A0A), Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Balance status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (hasEnoughBalance) Color(0xFFDCFCE7) else Color(0xFFFFE4E1)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (hasEnoughBalance) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                            contentDescription = null,
                            tint = if (hasEnoughBalance) Color(0xFF22C55E) else Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (hasEnoughBalance) "Balance sufficient" else "Insufficient balance",
                            color = if (hasEnoughBalance) Color(0xFF166534) else Color(0xFF991B1B),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        text = "₹ $userBalance available",
                        color = if (hasEnoughBalance) Color(0xFF166534) else Color(0xFF991B1B),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // ── Player tip card
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8EC))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Pro Tips  🎯",
                    color = Color(0xFF4A3010),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                listOf(game.tip1, game.tip2, game.tip3).forEachIndexed { i, tip ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("${i + 1}.", color = TaplayCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(tip, color = Color(0xFF5A4020), fontSize = 13.sp, lineHeight = 19.sp)
                    }
                }
            }
        }

        // ── Character picker card
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF5FF))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Select Your Character",
                    color = Color(0xFF1A2E4A),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Customize more in Avatar settings",
                    color = Color(0xFF5A6A80),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                val charEmojis = mapOf(
                    "Blaze"  to "🔥",
                    "Nova"   to "⚡",
                    "Rex"    to "🦖",
                    "Cipher" to "🤖",
                    "Astra"  to "🌟"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    characters.forEach { char ->
                        val isSelected = char == selectedCharacter
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isSelected) TaplayCyan.copy(alpha = 0.15f) else Color.White)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) TaplayCyan else Color(0xFFDDE8F8),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onCharacterChange(char) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(charEmojis[char] ?: "🎮", fontSize = 22.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = char,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TaplayCyan else Color(0xFF4A5A70),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // ── Action button
        if (hasEnoughBalance) {
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TaplayCyan)
            ) {
                Text(
                    text = "Confirm & Start Game  →",
                    color = TaplayBgDeep,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
            }
        } else {
            Button(
                onClick = onAddMoney,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48C88))
            ) {
                Text(
                    text = "Add Money to Wallet  →",
                    color = Color(0xFF2E1830),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun CostChip(label: String, bg: Color, textColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

// ─────────────────────────────────────────
// GAME STARTED OVERLAY
// ─────────────────────────────────────────
@Composable
private fun GameStartedOverlay(
    gameName: String,
    character: String,
    emoji: String
) {
    val infinite = rememberInfiniteTransition(label = "startPulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(600, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "startScale"
    )
    val bgAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(400),
        label = "bgAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(bgAlpha)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        TaplayCyan.copy(alpha = 0.18f),
                        Color(0xFF0A0812)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = emoji,
                fontSize = 80.sp,
                modifier = Modifier.scale(pulse)
            )

            Text(
                text = "Game Started!",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Text(
                text = gameName,
                color = TaplayCyan,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Playing as  $character",
                color = TaplayTextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Cyan progress bar
            val progress by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(2000, easing = EaseInOutCubic),
                label = "bar"
            )

            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(3.dp))
                        .background(TaplayCyan)
                )
            }

            Text(
                text = "Loading your session...",
                color = TaplayTextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

// ─────────────────────────────────────────
// CANCEL DIALOG
// ─────────────────────────────────────────
@Composable
private fun CancelGameDialog(
    onCancel: () -> Unit,
    onStay: () -> Unit
) {
    Dialog(onDismissRequest = onStay) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1630))
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("⚠️", fontSize = 40.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Leave the queue?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "If you leave now, you'll need to tap again to start a new session. Your credits won't be deducted.",
                    color = TaplayTextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48C88))
                ) {
                    Text(
                        "Leave & Go Home",
                        color = Color(0xFF2E1830),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(
                    onClick = onStay,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Stay Here",
                        color = TaplayCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}