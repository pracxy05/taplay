package com.taplay.wear.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

/**
 * Lightweight singleton that bridges the HCE service (NFC callbacks) with the
 * Compose UI and the ViewModel.
 *
 * The HCE service runs on its own thread; StateFlow handles thread safety and
 * Compose collection automatically.
 */
object WearSessionState {

    // ── NFC tap counter ───────────────────────────────────────────────────
    private val _tapCount = MutableStateFlow(0)
    val tapCount: StateFlow<Int> = _tapCount

    // ── Last tap timestamp ────────────────────────────────────────────────
    private val _lastTapTime = MutableStateFlow<LocalDateTime?>(null)
    val lastTapTime: StateFlow<LocalDateTime?> = _lastTapTime

    /**
     * Called by [com.taplay.wear.nfc.TaplayWearApduService] every time the
     * watch successfully processes an NFC tap.
     */
    fun onNfcTapSent() {
        _tapCount.value += 1
        _lastTapTime.value = LocalDateTime.now()
    }
}
