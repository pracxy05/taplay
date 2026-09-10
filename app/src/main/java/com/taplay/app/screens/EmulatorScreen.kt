package com.taplay.app.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taplay.app.data.model.MachineSessionStatus

@Composable
fun EmulatorScreen(viewModel: EmulatorViewModel) {
    val sessionStatusState by viewModel.sessionStatus.collectAsState()

    val isActive = sessionStatusState?.sessionStatus?.equals("ACTIVE", ignoreCase = true) == true
    val duration = sessionStatusState?.sessionDuration ?: 300
    val timeRemaining = sessionStatusState?.timeRemaining ?: 0

    val progress = if (isActive && duration > 0) {
        timeRemaining.toFloat() / duration.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "TimerProgress")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0A18)), // Cyber dark navy background
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Machine ID Banner
            Text(
                text = "ARCADE CABINET EMULATOR",
                color = Color(0xFF7C3AED),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "MACH-PHONE-2",
                color = Color(0xFF00E5FF),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isActive) Color(0xFF10B981).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f))
                    .border(
                        1.dp,
                        if (isActive) Color(0xFF10B981) else Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isActive) "GAME IN PROGRESS" else "NFC READY · TAP TO PLAY",
                    color = if (isActive) Color(0xFF10B981) else Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Circular Countdown Timer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(220.dp)
            ) {
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White.copy(alpha = 0.05f),
                    strokeWidth = 12.dp
                )

                CircularProgressIndicator(
                    progress = if (isActive) animatedProgress else 0f,
                    modifier = Modifier.fillMaxSize(),
                    color = if (isActive) Color(0xFF00E5FF) else Color.Gray,
                    strokeWidth = 12.dp
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isActive) {
                        val minutes = timeRemaining / 60
                        val seconds = timeRemaining % 60
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            color = Color.White,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "SESSION ACTIVE",
                            color = Color(0xFF10B981),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "IDLE",
                            color = Color.Gray,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Waiting for NFC Tap...",
                            color = Color.Gray.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}