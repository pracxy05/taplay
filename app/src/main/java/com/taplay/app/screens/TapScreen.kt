package com.taplay.app.screens

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.taplay.app.data.model.ArcadeGame
import com.taplay.app.data.model.TransactionResponse
import com.taplay.app.data.repository.WalletRepository
import com.taplay.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class TapStage {
    WAITING,
    DETECTED,
    CONFIRMED,
    STARTED
}

val defaultGame = ArcadeGame(
    machineId   = "MACH-PHONE-2",
    name        = "Air Hockey Blitz",
    tagId       = "MACH-PHONE-2",
    costCredits = 10,
    costAmount  = 10.0,
    genre       = "Sports · Arcade",
    difficulty  = "Medium",
    emoji       = "🏒"
)

@Composable
fun TapScreen(
    onSessionStarted: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val walletRepo = remember { WalletRepository(context) }

    var stage by remember { mutableStateOf(TapStage.WAITING) }
    // Default tag set to MACH-PHONE-2 to match HCE target emulator output
    var detectedTagId by remember { mutableStateOf("MACH-PHONE-2") }
    var selectedCharacter by remember { mutableStateOf("Blaze") }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showGameStarted by remember { mutableStateOf(false) }
    var isProcessingTxn by remember { mutableStateOf(false) }
    var txnErrorMessage by remember { mutableStateOf<String?>(null) }

    val userBalance by walletRepo.cachedBalance.collectAsState(initial = 540.0)
    val hasEnoughBalance = userBalance >= defaultGame.costCredits

    // Listen for Physical NFC Tag Taps (APDU / ISO-DEP / Type-4)
    DisposableEffect(Unit) {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        val activity = context as? Activity
        if (nfcAdapter != null && activity != null && nfcAdapter.isEnabled) {
            val options = android.os.Bundle()
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
            nfcAdapter.enableReaderMode(
                activity,
                { tag: Tag? ->
                    tag?.let {
                        val tagBytes = it.id
                        var tagHex = tagBytes.joinToString("") { b -> "%02X".format(b) }
                        
                        // Fallback to default machine ID if Hex string is short or custom APDU is used
                        if (tagHex.isBlank()) {
                            tagHex = "MACH-PHONE-2"
                        }

                        activity.runOnUiThread {
                            detectedTagId = tagHex
                            if (stage == TapStage.WAITING) {
                                stage = TapStage.DETECTED
                            }
                        }
                    }
                },
                NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_NFC_F or NfcAdapter.FLAG_READER_NFC_V or NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                options
            )
        }
        onDispose {
            val act = context as? Activity
            if (nfcAdapter != null && act != null) {
                nfcAdapter.disableReaderMode(act)
            }
        }
    }

    // Fallback simulation timer for testing without physical tag
    LaunchedEffect(Unit) {
        delay(3500)
        if (stage == TapStage.WAITING) {
            stage = TapStage.DETECTED
        }
    }

    // Game started animation
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
        if (!showGameStarted) {
            Column(modifier = Modifier.fillMaxSize()) {
                TapTopBar(
                    stage = stage,
                    onBackPressed = {
                        when (stage) {
                            TapStage.WAITING   -> onBack()
                            TapStage.DETECTED  -> showCancelDialog = true
                            TapStage.CONFIRMED -> { }
                            TapStage.STARTED   -> { }
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
                        TapStage.WAITING -> WaitingForTap(detectedTagId)

                        TapStage.DETECTED -> GameDetectedContent(
                            game = defaultGame,
                            tagId = detectedTagId,
                            userBalance = userBalance.toInt(),
                            hasEnoughBalance = hasEnoughBalance,
                            selectedCharacter = selectedCharacter,
                            isProcessing = isProcessingTxn,
                            errorMessage = txnErrorMessage,
                            onCharacterChange = { selectedCharacter = it },
                            onConfirm = {
                                isProcessingTxn = true
                                txnErrorMessage = null
                                scope.launch {
                                    val res: TransactionResponse = walletRepo.startTransaction(detectedTagId)
                                    isProcessingTxn = false
                                    if (res.authorized) {
                                        stage = TapStage.CONFIRMED
                                        showGameStarted = true
                                    } else {
                                        txnErrorMessage = res.message
                                    }
                                }
                            },
                            onAddMoney = { onBack() }
                        )

                        TapStage.CONFIRMED, TapStage.STARTED -> { }
                    }
                }
            }
        }

        if (showGameStarted) {
            GameStartedOverlay(
                gameName = defaultGame.name,
                character = selectedCharacter,
                emoji = defaultGame.emoji
            )
        }

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
                TapStage.WAITING   -> "NFC Tag Scan"
                TapStage.DETECTED  -> "Machine Detected"
                else               -> "Starting Session..."
            },
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun WaitingForTap(tagId: String) {
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

    val dotPulse by infinite.animateFloat(
        initialValue = 0.9f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "dot"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(ring1Scale)
                .alpha(ring1Alpha)
                .clip(CircleShape)
                .border(2.dp, TaplayCyan, CircleShape)
        )

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
                Text("TAP TAG", color = TaplayCyan, fontSize = 13.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("NFC ACTIVE", color = TaplayCyan.copy(alpha = 0.6f), fontSize = 10.sp)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tap phone on target machine NFC",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Target ID: $tagId",
                color = TaplayCyan,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GameDetectedContent(
    game: ArcadeGame,
    tagId: String,
    userBalance: Int,
    hasEnoughBalance: Boolean,
    selectedCharacter: String,
    isProcessing: Boolean,
    errorMessage: String?,
    onCharacterChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onAddMoney: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1E1040), Color(0xFF2A1260), Color(0xFF1A0D38))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(game.emoji, fontSize = 64.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = game.name,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${game.machineId}  •  Tag UID: $tagId",
                    color = TaplayCyan,
                    fontSize = 12.sp
                )
            }
        }

        if (errorMessage != null) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Text(
                    text = "⚠️ $errorMessage",
                    color = Color(0xFFC62828),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1430))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Session Cost", color = TaplayTextSecondary, fontSize = 11.sp)
                        Text("₹${game.costAmount.toInt()} (10 Credits)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Your Balance", color = TaplayTextSecondary, fontSize = 11.sp)
                        Text("₹$userBalance", color = TaplayCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        if (hasEnoughBalance) {
            Button(
                onClick = onConfirm,
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TaplayCyan)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TaplayBgDeep)
                } else {
                    Text(
                        text = "Pay ₹10 & Start Session  →",
                        color = TaplayBgDeep,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
            }
        } else {
            Button(
                onClick = onAddMoney,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48C88))
            ) {
                Text(
                    text = "Insufficient Balance — Top Up",
                    color = Color(0xFF2E1830),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun GameStartedOverlay(
    gameName: String,
    character: String,
    emoji: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0812)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(text = emoji, fontSize = 72.sp)
            Text(
                text = "Game Authorized!",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = gameName,
                color = TaplayCyan,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Session active on MACH-PHONE-2",
                color = TaplayTextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun CancelGameDialog(
    onCancel: () -> Unit,
    onStay: () -> Unit
) {
    Dialog(onDismissRequest = onStay) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1630))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("⚠️", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Cancel Game Tap?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4D6D))
                ) {
                    Text("Yes, Cancel", color = Color.White)
                }
            }
        }
    }
}