package com.taplay.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.taplay.app.BuildConfig
import com.taplay.app.data.repository.WalletRepository
import com.taplay.app.navigation.AppNavigation
import com.taplay.app.screens.EmulatorScreen
import com.taplay.app.screens.EmulatorViewModel
import com.taplay.app.ui.theme.TaplayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaplayTheme {
                // Explicitly check package suffix
                val isEmulatorApp = BuildConfig.APPLICATION_ID.endsWith(".emulator")

                if (isEmulatorApp) {
                    val walletRepository = remember { WalletRepository(applicationContext) }
                    val emulatorViewModel = remember { EmulatorViewModel(walletRepository.apiService) }

                    EmulatorScreen(viewModel = emulatorViewModel)
                } else {
                    AppNavigation()
                }
            }
        }
    }
}