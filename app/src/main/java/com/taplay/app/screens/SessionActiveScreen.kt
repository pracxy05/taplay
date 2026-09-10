package com.taplay.app.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taplay.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SessionActiveScreen(
    sessionDurationSeconds: Int = 300, // 5 minutes default
    machineName: String = "Air Hockey Blitz",
    machineId: String = "MACH-A14",
    onSessionEnded: () -> Unit
) {
    var timeLeftSeconds by remember { mutableStateOf(sessionDurationSeconds) }
    var isPaused by remember { mutableStateOf(false) }
    var showEndDialog by remember { mutableStateOf(false) }

    // Countdown Timer Loop
    LaunchedEffect(isPaused, timeLeftSeconds) {
        if (!isPaused && timeLeftSeconds > 0) {
            delay(1000)
            timeLeftSeconds -= 1
        } else if (timeLeftSeconds == 0) {
            onSessionEnded()
        }
    }

    val minutes = timeLeftSeconds / 60
    val seconds = timeLeftSeconds % 60
    val formattedTime = "%02d:%02d".format(minutes, seconds)
    val progress = timeLeftSeconds.toFloat() / sessionDurationSeconds.toFloat()

    val isWarning = timeLeftSeconds <= 30
    val accentColor = if (isWarning) TaplayError else TaplayCyan

    val infinite = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = if (isWarning) 1.06f else 1.02f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D0A18),
                        Color(0xFF130E26),
                        Color(0xFF090712)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = accentColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Text(
                            text = if (isWarning) "SESSION ENDING SOON" else "SESSION LIVE",
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = machineName,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Arcade Terminal $machineId",
                    color = TaplayTextSecondary,
                    fontSize = 13.sp
                )
            }

            // Big Circular Countdown Clock
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = accentColor,
                    strokeWidth = 10.dp,
                    trackColor = Color(0xFF1E1A3E)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedTime,
                        color = Color.White,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = if (isPaused) "PAUSED" else "REMAINING",
                        color = if (isPaused) TaplayWarning else TaplayTextSecondary,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Game Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16122C))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Credits Used", color = TaplayTextSecondary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("10", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(TaplayBorder)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Bonus Earned", color = TaplayTextSecondary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("+2 Tokens", color = TaplayGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedButton(
                    onClick = { isPaused = !isPaused },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TaplayBorderBright)
                ) {
                    Text(
                        text = if (isPaused) "Resume" else "Pause",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { showEndDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TaplayError)
                ) {
                    Text(
                        text = "End Session",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (showEndDialog) {
            AlertDialog(
                onDismissRequest = { showEndDialog = false },
                containerColor = Color(0xFF1C1630),
                title = { Text("End Game Session?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to end your current session early?", color = TaplayTextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            showEndDialog = false
                            onSessionEnded()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TaplayError)
                    ) {
                        Text("End Game")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEndDialog = false }) {
                        Text("Cancel", color = TaplayCyan)
                    }
                }
            )
        }
    }
}