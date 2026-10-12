package com.taplay.app.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taplay.app.data.api.TaplayApiService
import com.taplay.app.data.model.MachineSessionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmulatorViewModel(
    private val apiService: TaplayApiService
) : ViewModel() {

    private val _sessionStatus = MutableStateFlow<MachineSessionStatus?>(null)
    val sessionStatus: StateFlow<MachineSessionStatus?> = _sessionStatus.asStateFlow()

    private val targetMachineId = "MACH-PHONE-2"

    init {
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                try {
                    val response = apiService.getMachineSessionStatus(targetMachineId)
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        Log.d("EmulatorViewModel", "Poll success: state=${body.sessionStatus}, remaining=${body.timeRemaining}s")
                        _sessionStatus.value = body
                    } else {
                        Log.w("EmulatorViewModel", "Poll warning response code: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("EmulatorViewModel", "Poll exception: ${e.localizedMessage}")
                }
                delay(1000) // Poll every 1 second
            }
        }
    }
}