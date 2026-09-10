package com.taplay.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.taplay.app.data.api.TaplayApiService
import com.taplay.app.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private val Context.dataStore by preferencesDataStore(name = "taplay_prefs")

class WalletRepository(private val context: Context) {

    private val KEY_BALANCE = doublePreferencesKey("wallet_balance")
    private val KEY_USERNAME = stringPreferencesKey("user_name")
    private val KEY_EMAIL = stringPreferencesKey("user_email")

    // Default base URL for Android emulator / local testing (10.0.2.2 points to host machine)
    private var baseUrl: String = "http://10.0.2.2:8080/"

    // Made public so it can be accessed by MainActivity / ViewModels directly
    val apiService: TaplayApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TaplayApiService::class.java)
    }

    val cachedBalance: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[KEY_BALANCE] ?: 540.0
    }

    val cachedUsername: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USERNAME] ?: "Praharsh"
    }

    suspend fun saveLocalBalance(balance: Double) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BALANCE] = balance
        }
    }

    suspend fun fetchWalletInfo(): UserWallet {
        return try {
            val response = apiService.getWallet(1)
            if (response.isSuccessful && response.body() != null) {
                val wallet = response.body()!!
                saveLocalBalance(wallet.balance)
                wallet
            } else {
                getFallbackWallet()
            }
        } catch (e: Exception) {
            getFallbackWallet()
        }
    }

    suspend fun topUpWallet(amount: Double): UserWallet {
        return try {
            val req = TopUpRequest(userId = 1, amount = amount)
            val response = apiService.topUpWallet(req)
            if (response.isSuccessful && response.body() != null) {
                val updated = response.body()!!
                saveLocalBalance(updated.balance)
                updated
            } else {
                val current = getFallbackWallet()
                val newBal = current.balance + amount
                saveLocalBalance(newBal)
                current.copy(balance = newBal, credits = (newBal / 10).toInt())
            }
        } catch (e: Exception) {
            val current = getFallbackWallet()
            val newBal = current.balance + amount
            saveLocalBalance(newBal)
            current.copy(balance = newBal, credits = (newBal / 10).toInt())
        }
    }

    suspend fun fetchTransactions(): List<Transaction> {
        return try {
            val response = apiService.getTransactions(1)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                getFallbackTransactions()
            }
        } catch (e: Exception) {
            getFallbackTransactions()
        }
    }

    suspend fun startTransaction(tagId: String): TransactionResponse {
        return try {
            val req = StartTransactionRequest(tagId = tagId, userId = 1)
            val response = apiService.startTransaction(req)
            if (response.isSuccessful && response.body() != null) {
                val res = response.body()!!
                if (res.authorized) {
                    saveLocalBalance(res.remainingBalance)
                }
                res
            } else {
                mockStartTransaction(tagId)
            }
        } catch (e: Exception) {
            mockStartTransaction(tagId)
        }
    }

    private suspend fun mockStartTransaction(tagId: String): TransactionResponse {
        val current = getFallbackWallet()
        val cost = 10.0
        return if (current.balance >= cost) {
            val newBal = current.balance - cost
            saveLocalBalance(newBal)
            TransactionResponse(
                authorized = true,
                message = "Transaction Authorized via Physical Tag $tagId",
                transactionId = "TXN-${System.currentTimeMillis()}",
                sessionDuration = 300,
                creditsDeducted = 10,
                remainingBalance = newBal,
                machineName = "Air Hockey Blitz",
                machineId = "MACH-#A14"
            )
        } else {
            TransactionResponse(
                authorized = false,
                message = "Insufficient wallet balance (Requires ₹10)",
                sessionDuration = 0,
                creditsDeducted = 0,
                remainingBalance = current.balance
            )
        }
    }

    private fun getFallbackWallet() = UserWallet(
        userId = 1,
        username = "Praharsh",
        email = "praharsh@taplay.app",
        balance = 540.0,
        credits = 54,
        totalTokens = 120,
        memberRank = "#18 Pro Gamer",
        favoriteGame = "Air Hockey Blitz"
    )

    private fun getFallbackTransactions(): List<Transaction> = listOf(
        Transaction(
            id = "TXN-9081",
            title = "Air Hockey Blitz",
            machineId = "MACH-#A14",
            amount = 10.0,
            credits = 10,
            type = "GAME_SESSION",
            timestamp = "Just Now",
            status = "SUCCESS"
        ),
        Transaction(
            id = "TXN-8812",
            title = "Wallet Top-Up (Razorpay)",
            machineId = "PAYMENT",
            amount = 200.0,
            credits = 200,
            type = "TOP_UP",
            timestamp = "Today, 02:15 PM",
            status = "SUCCESS"
        ),
        Transaction(
            id = "TXN-7612",
            title = "Cyber Racer DX",
            machineId = "MACH-#B02",
            amount = 15.0,
            credits = 15,
            type = "GAME_SESSION",
            timestamp = "Yesterday",
            status = "SUCCESS"
        ),
        Transaction(
            id = "TXN-6510",
            title = "Basketball Hoops",
            machineId = "MACH-#C07",
            amount = 8.0,
            credits = 8,
            type = "GAME_SESSION",
            timestamp = "2 Sep 2026",
            status = "SUCCESS"
        )
    )
}