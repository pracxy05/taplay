package com.taplay.app.data.repository

import android.content.Context
import android.util.Log
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

    private val candidateHosts = listOf(
        "127.0.0.1:8080",
        "192.168.0.114:8080",
        "192.168.1.158:8080",
        "192.168.1.162:8080"
    )

    private val okHttpClient: okhttp3.OkHttpClient by lazy {
        okhttp3.OkHttpClient.Builder()
            .connectTimeout(3, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val req = chain.request()
                var lastException: java.io.IOException? = null
                for (host in candidateHosts) {
                    val hostName = host.substringBefore(":")
                    val hostPort = host.substringAfter(":").toIntOrNull() ?: 8080
                    val newUrl = req.url().newBuilder().host(hostName).port(hostPort).build()
                    val newReq = req.newBuilder().url(newUrl).build()
                    try {
                        val response = chain.proceed(newReq)
                        if (response.isSuccessful || response.code() < 500) {
                            return@addInterceptor response
                        }
                        response.close()
                    } catch (e: java.io.IOException) {
                        lastException = e
                    }
                }
                throw lastException ?: java.io.IOException("No backend hosts reachable: $candidateHosts")
            }
            .build()
    }

    val apiService: TaplayApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.1.162:8080/")
            .client(okHttpClient)
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
                Log.w("WalletRepository", "getWallet failed: ${response.code()}")
                getFallbackWallet()
            }
        } catch (e: Exception) {
            Log.e("WalletRepository", "getWallet exception: ${e.localizedMessage}", e)
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
                Log.w("WalletRepository", "topUpWallet failed: ${response.code()}")
                val current = getFallbackWallet()
                val newBal = current.balance + amount
                saveLocalBalance(newBal)
                current.copy(balance = newBal, credits = (newBal / 10).toInt())
            }
        } catch (e: Exception) {
            Log.e("WalletRepository", "topUpWallet exception: ${e.localizedMessage}", e)
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
                Log.w("WalletRepository", "getTransactions failed: ${response.code()}")
                getFallbackTransactions()
            }
        } catch (e: Exception) {
            Log.e("WalletRepository", "getTransactions exception: ${e.localizedMessage}", e)
            getFallbackTransactions()
        }
    }

    suspend fun startTransaction(tagId: String): TransactionResponse {
        return try {
            Log.d("WalletRepository", "Sending startTransaction for tagId: $tagId")
            val req = StartTransactionRequest(tagId = tagId, userId = 1)
            val response = apiService.startTransaction(req)
            
            if (response.isSuccessful && response.body() != null) {
                val res = response.body()!!
                Log.d("WalletRepository", "SUCCESS from Spring Boot: $res")
                if (res.authorized) {
                    saveLocalBalance(res.remainingBalance)
                }
                res
            } else {
                Log.e("WalletRepository", "Server error code: ${response.code()}")
                mockStartTransaction(tagId)
            }
        } catch (e: Exception) {
            Log.e("WalletRepository", "NETWORK ERROR hitting Spring Boot: ${e.localizedMessage}", e)
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
                message = "Transaction Authorized via Local Tag $tagId",
                transactionId = "TXN-${System.currentTimeMillis()}",
                sessionDuration = 300,
                creditsDeducted = 10,
                remainingBalance = newBal,
                machineName = "Air Hockey Blitz",
                machineId = "MACH-PHONE-2"
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
            machineId = "MACH-PHONE-2",
            amount = 10.0,
            credits = 10,
            type = "GAME_SESSION",
            timestamp = "Just Now",
            status = "SUCCESS"
        )
    )
}