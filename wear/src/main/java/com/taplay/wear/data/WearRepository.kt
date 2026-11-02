package com.taplay.wear.data

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Singleton repository that manages the Retrofit instance with auto-failover.
 * Supports multiple candidate host addresses:
 * 1. Current Wi-Fi IP (192.168.1.162)
 * 2. Localhost reverse proxy (127.0.0.1:8080)
 * 3. Alternate Wi-Fi IPs
 */
object WearRepository {

    private const val TAG = "WearRepository"

    // Candidate backend endpoints (tried in order)
    private val candidateHosts = listOf(
        "127.0.0.1:8080",
        "192.168.0.114:8080",
        "192.168.1.158:8080",
        "192.168.1.162:8080"
    )

    @Volatile
    private var activeHost: String = candidateHosts.first()

    var baseUrl: String = "http://$activeHost/"
        set(value) {
            field = value
            rebuildClient()
        }

    private var api: WearApiService = buildApi()

    private fun buildApi(): WearApiService {
        val client = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .addInterceptor(AutoFailoverInterceptor())
            .build()

        return Retrofit.Builder()
            .baseUrl("http://$activeHost/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WearApiService::class.java)
    }

    private fun rebuildClient() {
        api = buildApi()
    }

    private class AutoFailoverInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val req = chain.request()

            // Try with activeHost first
            val currentList = listOf(activeHost) + candidateHosts.filter { it != activeHost }
            var lastException: IOException? = null

            for (host in currentList) {
                val hostName = host.substringBefore(":")
                val hostPort = host.substringAfter(":").toIntOrNull() ?: 8080
                val newUrl = req.url().newBuilder()
                    .host(hostName)
                    .port(hostPort)
                    .build()
                val newReq = req.newBuilder().url(newUrl).build()

                try {
                    val response = chain.proceed(newReq)
                    if (response.isSuccessful || response.code() < 500) {
                        if (activeHost != host) {
                            Log.i(TAG, "Switched activeHost to: $host")
                            activeHost = host
                        }
                        return response
                    }
                    response.close()
                } catch (e: IOException) {
                    lastException = e
                    Log.w(TAG, "Host $host unreachable: ${e.message}, trying next...")
                }
            }

            throw lastException ?: IOException("No backend hosts reachable: $candidateHosts")
        }
    }

    // ── Public API ────────────────────────────────────────────────────────

    suspend fun getSessionStatus(machineId: String = "CARD-WATCH-01"): Result<WearSessionStatus> =
        runCatching { api.getSessionStatus(machineId) }
            .onFailure { Log.e(TAG, "getSessionStatus failed", it) }

    suspend fun getUserProfile(): Result<WearUserInfo> =
        runCatching { api.getUserProfile() }
            .onFailure { Log.e(TAG, "getUserProfile failed", it) }

    suspend fun endSession(machineId: String = "CARD-WATCH-01"): Result<Boolean> =
        runCatching { api.endSession(machineId) }
            .onFailure { Log.e(TAG, "endSession failed", it) }

    suspend fun pauseSession(machineId: String = "CARD-WATCH-01"): Result<Map<String, Any>> =
        runCatching { api.pauseSession(machineId) }
            .onFailure { Log.e(TAG, "pauseSession failed", it) }

    suspend fun resumeSession(machineId: String = "CARD-WATCH-01"): Result<Map<String, Any>> =
        runCatching { api.resumeSession(machineId) }
            .onFailure { Log.e(TAG, "resumeSession failed", it) }
}
