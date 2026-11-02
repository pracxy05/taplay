package com.taplay.wear.data

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// ── DTOs (mirror the backend response shapes) ─────────────────────────────

data class WearSessionStatus(
    val machineId: String = "",
    val sessionStatus: String = "IDLE",   // "IDLE" | "ACTIVE" | "EXPIRED"
    val sessionDuration: Int = 0,
    val timeRemaining: Int = 0,
    val lastTransactionId: String? = null,
    val userName: String? = null,
    val gameName: String? = null
)

data class WearUserInfo(
    val id: Long = 1,
    val username: String = "—",
    val balance: Double = 0.0,
    val credits: Int = 0
)

data class WearTransactionResponse(
    val authorized: Boolean = false,
    val message: String = "",
    val transactionId: String? = null,
    val sessionDuration: Int = 0,
    val creditsDeducted: Int = 0,
    val remainingBalance: Double = 0.0,
    val machineName: String = "",
    val machineId: String = ""
)

// ── Retrofit interface ────────────────────────────────────────────────────

interface WearApiService {

    /** Poll the active session for the watch machine ID. */
    @GET("/api/v1/transaction/status")
    suspend fun getSessionStatus(
        @Query("machineId") machineId: String = "CARD-WATCH-01"
    ): WearSessionStatus

    /** Fetch the wallet owner profile (user ID 1 = demo user). */
    @GET("/api/v1/user/profile")
    suspend fun getUserProfile(
        @Query("userId") userId: Long = 1L
    ): WearUserInfo

    /** End the current session. */
    @POST("/api/v1/transaction/end")
    suspend fun endSession(
        @Query("machineId") machineId: String = "CARD-WATCH-01"
    ): Boolean

    /** Pause the current session (new backend endpoint). */
    @POST("/api/v1/session/pause")
    suspend fun pauseSession(
        @Query("machineId") machineId: String = "CARD-WATCH-01"
    ): Map<String, Any>

    /** Resume a paused session. */
    @POST("/api/v1/session/resume")
    suspend fun resumeSession(
        @Query("machineId") machineId: String = "CARD-WATCH-01"
    ): Map<String, Any>
}
