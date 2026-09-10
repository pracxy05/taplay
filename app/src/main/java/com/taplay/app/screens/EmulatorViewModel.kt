package com.taplay.app.screens

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

    init {
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                try {
                    val response = apiService.getMachineSessionStatus("MACH-PHONE-2")
                    if (response.isSuccessful) {
                        _sessionStatus.value = response.body()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(1000) // Poll every 1 second
            }
        }
    }
}