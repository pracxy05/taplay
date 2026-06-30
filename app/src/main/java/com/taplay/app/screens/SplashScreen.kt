package com.taplay.app.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.taplay.app.ui.theme.*

@Composable
fun SplashScreen(onNavigateNext: () -> Unit) {

    // ── Scale animation state + auto navigation
    var scaleTarget by remember { mutableStateOf(0.82f) }

    LaunchedEffect(Unit) {
        scaleTarget = 1f
        delay(3000)
        onNavigateNext()
    }

    // ── Animations
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    // Glow pulse on NFC waves
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Fade in for TAPLAY text
    val textAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 900,
            delayMillis = 300
        ),
        label = "textAlpha"
    )

    // Zoom in for TAPLAY text
    val textScale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = tween(
            durationMillis = 900,
            delayMillis = 300,
            easing = EaseOutBack
        ),
        label = "textScale"
    )

    // ── Root container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF12093A),   // Deep violet center
                        TaplayBgDeep        // Almost black edges
                    ),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // ── Background NFC watermark (3 waves, semi-transparent)
        Canvas(
            modifier = Modifier
                .size(300.dp)
                .scale(pulseScale)
                .alpha(pulseAlpha)
        ) {
            val cx = size.width / 2f
            val cy = size.height / 1.6f
            val strokeWidth = 18f
            val baseRadius = size.minDimension * 0.18f

            // Draw NFC dot at center
            drawCircle(
                color = TaplayCyan,
                radius = 14f,
                center = Offset(cx, cy)
            )

            // Draw 3 NFC arc waves
            listOf(0, 1, 2).forEach { i ->
                val radius = baseRadius * (i + 1) * 1.35f
                val sweepAngle = 90f
                val startAngle = 225f

                drawArc(
                    color = if (i == 2) TaplayCyan else TaplayPrimary,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(cx - radius, cy - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(
                        width = strokeWidth - (i * 2f),
                        cap = StrokeCap.Round
                    )
                )
            }
        }

        // ── Foreground text — TAPLAY + tagline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(textAlpha)
                .scale(textScale)
        ) {
            Text(
                text = "TAPLAY",
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                color = TaplayTextPrimary,
                textAlign = TextAlign.Center,
                letterSpacing = 8.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Tap. Play. Win.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TaplayCyan,
                textAlign = TextAlign.Center,
                letterSpacing = 3.sp
            )
        }
    }
}