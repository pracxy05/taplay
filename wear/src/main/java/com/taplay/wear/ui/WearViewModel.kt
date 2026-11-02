package com.taplay.wear.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taplay.wear.data.WearRepository
import com.taplay.wear.data.WearSessionState
import com.taplay.wear.data.WearSessionStatus
import com.taplay.wear.data.WearUserInfo
import com.taplay.wear.nfc.TaplayWearApduService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// ── UI State ──────────────────────────────────────────────────────────────

enum class WearScreenState {
    IDLE,           // No session — show "TAP TO PLAY"
    ACTIVE,         // Session running — show countdown + controls
    PAUSED,         // Session paused by user
    CONNECTING,     // Transient — while fetching from backend
    ERROR           // Backend unreachable
}

data class WearUiState(
    val screen: WearScreenState = WearScreenState.IDLE,
    val machineId: String = TaplayWearApduService.MACHINE_ID,
    val userName: String = "—",
    val balance: Double = 0.0,
    val credits: Int = 0,
    val gameName: String = "—",
    val sessionDuration: Int = 0,
    val timeRemaining: Int = 0,
    val tapCount: Int = 0,
    val lastError: String? = null,
    val backendUrl: String = WearRepository.baseUrl,
    val isPaused: Boolean = false
)

// ── ViewModel ─────────────────────────────────────────────────────────────

class WearViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WearUiState())
    val uiState: StateFlow<WearUiState> = _uiState

    private var pollJob: Job? = null

    init {
        // Mirror the NFC tap counter from the HCE singleton into UI state
        viewModelScope.launch {
            WearSessionState.tapCount.collect { count ->
                _uiState.update { it.copy(tapCount = count) }
            }
        }

        // Fetch initial user profile
        fetchUserProfile()

        // Start polling the backend
        startPolling()
    }

    // ── Profile ───────────────────────────────────────────────────────────

    fun fetchUserProfile() {
        viewModelScope.launch {
            WearRepository.getUserProfile()
                .onSuccess { info ->
                    _uiState.update {
                        it.copy(
                            userName = info.username,
                            balance = info.balance,
                            credits = info.credits
                        )
                    }
                }
                .onFailure { /* silently ignore — balance stays at 0 */ }
        }
    }

    // ── Session Polling ───────────────────────────────────────────────────

    fun startPolling(intervalMs: Long = 2_000L) {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                pollSession()
                delay(intervalMs)
            }
        }
    }

    private suspend fun pollSession() {
        WearRepository.getSessionStatus()
            .onSuccess { status ->
                applySessionStatus(status)
            }
            .onFailure { err ->
                _uiState.update {
                    it.copy(
                        screen = WearScreenState.ERROR,
                        lastError = "Backend unreachable"
                    )
                }
            }
    }

    private fun applySessionStatus(status: WearSessionStatus) {
        val currentState = _uiState.value

        val newScreen = when {
            currentState.isPaused                  -> WearScreenState.PAUSED
            status.sessionStatus == "ACTIVE"       -> WearScreenState.ACTIVE
            else                                   -> WearScreenState.IDLE
        }

        _uiState.update {
            it.copy(
                screen = newScreen,
                gameName = status.gameName ?: it.gameName,
                sessionDuration = status.sessionDuration,
                timeRemaining = status.timeRemaining,
                lastError = null
            )
        }
    }

    // ── Session Controls ──────────────────────────────────────────────────

    fun pauseSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPaused = true, screen = WearScreenState.PAUSED) }
            WearRepository.pauseSession()
        }
    }

    fun resumeSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPaused = false, screen = WearScreenState.ACTIVE) }
            WearRepository.resumeSession()
        }
    }

    fun endSession() {
        viewModelScope.launch {
            WearRepository.endSession()
            _uiState.update {
                it.copy(
                    screen = WearScreenState.IDLE,
                    isPaused = false,
                    timeRemaining = 0,
                    gameName = "—"
                )
            }
        }
    }

    // ── Settings ──────────────────────────────────────────────────────────

    fun updateBackendUrl(url: String) {
        WearRepository.baseUrl = url
        _uiState.update { it.copy(backendUrl = url) }
    }

    override fun onCleared() {
        super.onCleared()
        pollJob?.cancel()
    }
}
