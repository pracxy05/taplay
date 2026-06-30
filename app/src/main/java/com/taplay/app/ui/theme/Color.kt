package com.taplay.app.ui.theme

import androidx.compose.ui.graphics.Color

// ─── TAPLAY COLOR SYSTEM ───────────────────────────────────────────
// Inspired by: PlayStation (deep navy/indigo), Xbox (clean dark surfaces),
// Nintendo Switch (vibrant neon accent), Xpass (electric violet)
// Philosophy: One dark base. One hero accent. Precise color moments.
// ───────────────────────────────────────────────────────────────────

// ── BACKGROUNDS (PlayStation DNA — deep space)
val TaplayBgDeep        = Color(0xFF080612)   // Deepest bg — almost black violet
val TaplayBgDark        = Color(0xFF0D0B1E)   // Main background
val TaplaySurface       = Color(0xFF13102A)   // Card surface
val TaplaySurface2      = Color(0xFF1C1840)   // Elevated card / modal
val TaplaySurface3      = Color(0xFF252050)   // Highlighted surface / selected

// ── PRIMARY ACCENT (Xpass + Nintendo — electric violet)
val TaplayPrimary       = Color(0xFF7C3AED)   // Hero violet — CTAs, active states
val TaplayPrimaryLight  = Color(0xFF9F67F5)   // Hover / pressed state
val TaplayPrimaryDim    = Color(0xFF4A1E9E)   // Subtle violet for backgrounds
val TaplayPrimaryGlow   = Color(0x337C3AED)   // 20% violet — glow effects

// ── SECONDARY ACCENT (Xbox — electric cyan)
val TaplayCyan          = Color(0xFF00D4FF)   // Neon cyan — highlights, NFC ring
val TaplayCyanDim       = Color(0xFF0099BB)   // Muted cyan
val TaplayCyanGlow      = Color(0x2200D4FF)   // 13% cyan — subtle glow

// ── SUCCESS (Nintendo green — approved / session active)
val TaplayGreen         = Color(0xFF00E676)   // Bright approved green
val TaplayGreenDim      = Color(0xFF00A854)   // Muted success
val TaplayGreenGlow     = Color(0x2200E676)   // 13% green — glow

// ── ERROR / DANGER
val TaplayError         = Color(0xFFFF4D6D)   // Denied / insufficient balance
val TaplayErrorDim      = Color(0xFFCC2244)   // Muted error
val TaplayErrorGlow     = Color(0x22FF4D6D)   // 13% red — glow

// ── WARNING
val TaplayWarning       = Color(0xFFFFB800)   // Low balance warning
val TaplayWarningDim    = Color(0xFFCC8800)   // Muted warning

// ── TEXT (clean hierarchy)
val TaplayTextPrimary   = Color(0xFFF0EEFF)   // Slightly warm white — easy on eyes
val TaplayTextSecondary = Color(0xFFB0A8D0)   // Muted purple-white
val TaplayTextMuted     = Color(0xFF6B6388)   // Faint — timestamps, labels
val TaplayTextInverse   = Color(0xFF0D0B1E)   // Dark text on light buttons

// ── BORDERS & DIVIDERS
val TaplayBorder        = Color(0xFF2A2550)   // Subtle card border
val TaplayBorderBright  = Color(0xFF4A4580)   // Active / focused border
val TaplayDivider       = Color(0xFF1E1A3A)   // Section divider

// ── GRADIENTS (use as brush start/end pairs)
val TaplayGradientStart = Color(0xFF7C3AED)   // Violet
val TaplayGradientMid   = Color(0xFF4B2BBF)   // Deep indigo
val TaplayGradientEnd   = Color(0xFF00D4FF)   // Cyan

// Wallet card gradient (PlayStation card vibes)
val TaplayCardStart     = Color(0xFF2D1B7A)   // Deep purple
val TaplayCardEnd       = Color(0xFF0F3460)   // Deep navy

// NFC tap ring gradient
val TaplayNfcRingStart  = Color(0xFF00D4FF)   // Cyan
val TaplayNfcRingEnd    = Color(0xFF7C3AED)   // Violet

// ── TRANSPARENT / OVERLAYS
val TaplayScrim         = Color(0xCC080612)   // 80% dark — modal backdrop
val TaplayOverlay       = Color(0x80080612)   // 50% dark — image overlay