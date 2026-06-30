package com.taplay.app.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taplay.app.ui.theme.*

// ── Data model for each onboarding page
data class OnboardingPage(
    val icon: @Composable () -> Unit,
    val title: String,
    val subtitle: String,
    val description: String
)

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {

    val pages = listOf(
        OnboardingPage(
            icon = { NfcWaveIcon() },
            title = "Ditch the Card.\nUse Your Phone.",
            subtitle = "Tap to pay at any Taplay arcade",
            description = "No more fumbling for coins or swipe cards. Just tap your phone on any Taplay-enabled machine and your session begins instantly — fast, secure, and completely contactless."
        ),
        OnboardingPage(
            icon = { WalletIcon() },
            title = "Top Up.\nPlay Instantly.",
            subtitle = "Your digital wallet, always ready",
            description = "Load credits into your Taplay wallet anytime from the app. Every tap deducts automatically — you'll always see your live balance, session history, and total spending in one place."
        ),
        OnboardingPage(
            icon = { ControllerIcon() },
            title = "Your Game.\nYour Rules.",
            subtitle = "Track sessions, earn rewards",
            description = "Monitor active sessions in real-time, get alerts when credits run low, and earn loyalty points on every play. Taplay turns every arcade visit into a smart, connected experience."
        )
    )

    var currentPage by remember { mutableStateOf(0) }
    var dragOffset by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF12093A), TaplayBgDeep),
                    radius = 1400f
                )
            )
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragOffset < -80f && currentPage < pages.size - 1) currentPage++
                        else if (dragOffset > 80f && currentPage > 0) currentPage--
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { _, delta -> dragOffset += delta }
                )
            }
    ) {

        // ── Swipeable page content
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { it } + fadeIn(tween(300))).togetherWith(
                        slideOutHorizontally { -it } + fadeOut(tween(300))
                    )
                } else {
                    (slideInHorizontally { -it } + fadeIn(tween(300))).togetherWith(
                        slideOutHorizontally { it } + fadeOut(tween(300))
                    )
                }
            },
            label = "pageContent",
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPageContent(page = pages[page])
        }

        // ── Bottom: dots + buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // Dot indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pages.indices.forEach { index ->
                    val isActive = index == currentPage
                    val dotWidth by animateDpAsState(
                        targetValue = if (isActive) 28.dp else 8.dp,
                        animationSpec = tween(300),
                        label = "dotWidth"
                    )
                    val dotAlpha by animateFloatAsState(
                        targetValue = if (isActive) 1f else 0.35f,
                        animationSpec = tween(300),
                        label = "dotAlpha"
                    )
                    Box(
                        modifier = Modifier
                            .width(dotWidth)
                            .height(8.dp)
                            .alpha(dotAlpha)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TaplayCyan)
                    )
                }
            }

            // CTA Button
            if (currentPage == pages.size - 1) {
                Button(
                    onClick = onGetStarted,
                    modifier = Modifier.fillMaxWidth(0.75f).height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TaplayCyan)
                ) {
                    Text(
                        text = "GET STARTED",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        color = TaplayBgDeep
                    )
                }
            } else {
                Button(
                    onClick = { currentPage++ },
                    modifier = Modifier.fillMaxWidth(0.75f).height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.5.dp, TaplayCyan)
                ) {
                    Text(
                        text = "NEXT",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        color = TaplayCyan
                    )
                }
            }

            // Skip link
            if (currentPage < pages.size - 1) {
                Text(
                    text = "Skip",
                    fontSize = 13.sp,
                    color = TaplayTextSecondary,
                    modifier = Modifier
                        .alpha(0.55f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onGetStarted() }
                )
            }
        }
    }
}

// ── Single page layout
@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .padding(top = 80.dp, bottom = 220.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
            page.icon()
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = page.title,
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            color = TaplayTextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 38.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = page.subtitle,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TaplayCyan,
            textAlign = TextAlign.Center,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = page.description,
            fontSize = 14.sp,
            color = TaplayTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

// ── Icon: NFC waves (Slide 1)
@Composable
fun NfcWaveIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "nfc")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "nfcPulse"
    )
    Canvas(modifier = Modifier.size(160.dp).scale(pulse)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val base = size.minDimension * 0.12f
        drawCircle(color = TaplayCyan, radius = 12f, center = Offset(cx, cy))
        listOf(0, 1, 2).forEach { i ->
            val r = base * (i + 1) * 1.6f
            drawArc(
                color = if (i == 2) TaplayCyan else TaplayPrimary,
                startAngle = 210f, sweepAngle = 120f, useCenter = false,
                topLeft = Offset(cx - r, cy - r), size = Size(r * 2f, r * 2f),
                style = Stroke(width = 14f - i * 2f, cap = StrokeCap.Round)
            )
        }
    }
}

// ── Icon: Wallet (Slide 2)
@Composable
fun WalletIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "wallet")
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.75f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "walletGlow"
    )
    Canvas(modifier = Modifier.size(160.dp).alpha(glow)) {
        val w = size.width * 0.72f
        val h = size.height * 0.52f
        val left = (size.width - w) / 2f
        val top = (size.height - h) / 2f
        drawRoundRect(
            color = TaplayPrimary,
            topLeft = Offset(left, top), size = Size(w, h),
            cornerRadius = CornerRadius(18f), style = Stroke(width = 8f)
        )
        drawRoundRect(
            color = TaplayCyan.copy(alpha = 0.8f),
            topLeft = Offset(left + w * 0.55f, top + h * 0.18f),
            size = Size(w * 0.38f, h * 0.60f),
            cornerRadius = CornerRadius(12f), style = Stroke(width = 6f)
        )
        drawCircle(color = TaplayCyan, radius = 10f, center = Offset(left + w * 0.74f, top + h * 0.48f))
    }
}

// ── Icon: Game Controller (Slide 3)
@Composable
fun ControllerIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "ctrl")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.90f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "ctrlPulse"
    )
    Canvas(modifier = Modifier.size(160.dp).scale(pulse)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val bw = size.width * 0.74f
        val bh = size.height * 0.42f
        drawRoundRect(
            color = TaplayPrimary,
            topLeft = Offset(cx - bw / 2, cy - bh / 2), size = Size(bw, bh),
            cornerRadius = CornerRadius(bh / 2), style = Stroke(width = 7f)
        )
        // Left joystick
        drawCircle(color = TaplayCyan, radius = 15f, center = Offset(cx - bw * 0.27f, cy + 4f), style = Stroke(width = 5f))
        // Right action dots
        listOf(
            Offset(cx + bw * 0.22f, cy - 12f),
            Offset(cx + bw * 0.32f, cy + 2f),
            Offset(cx + bw * 0.22f, cy + 16f)
        ).forEach { drawCircle(color = TaplayCyan, radius = 5f, center = it) }
        // D-pad
        drawLine(TaplayTextSecondary, Offset(cx - bw * 0.09f, cy - 6f), Offset(cx + bw * 0.05f, cy - 6f), 5f, StrokeCap.Round)
        drawLine(TaplayTextSecondary, Offset(cx - bw * 0.02f, cy - 14f), Offset(cx - bw * 0.02f, cy + 2f), 5f, StrokeCap.Round)
    }
}