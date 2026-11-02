package com.taplay.app

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
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

    private var emulatorViewModelRef: EmulatorViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TaplayTheme {
                // Explicitly check package suffix
                val isEmulatorApp = BuildConfig.APPLICATION_ID.endsWith(".emulator")

                if (isEmulatorApp) {
                    val walletRepository = remember { WalletRepository(applicationContext) }
                    val emulatorViewModel = remember {
                        EmulatorViewModel(walletRepository.apiService).also {
                            emulatorViewModelRef = it
                        }
                    }

                    EmulatorScreen(viewModel = emulatorViewModel)
                } else {
                    AppNavigation()
                }
            }
        }

        handleNfcIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNfcIntent(intent)
    }

    private fun handleNfcIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        Log.d("MainActivity", "handleNfcIntent action: $action")
        if (action == NfcAdapter.ACTION_TECH_DISCOVERED ||
            action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            action == NfcAdapter.ACTION_NDEF_DISCOVERED
        ) {
            val tag = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }

            Log.i("MainActivity", "NFC tag tapped outside reader mode: tag=$tag")
            emulatorViewModelRef?.onNfcTagDetected("CARD-WATCH-01")
        }
    }
}