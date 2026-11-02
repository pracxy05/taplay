package com.taplay.app.data.model

data class UserWallet(
    val userId: Long = 1,
    val username: String = "Praharsh",
    val email: String = "praharsh@taplay.app",
    val balance: Double = 540.0,
    val credits: Int = 54,
    val totalTokens: Int = 120,
    val memberRank: String = "#18 Pro Gamer",
    val favoriteGame: String = "Air Hockey Blitz"
)

data class Transaction(
    val id: String,
    val title: String,
    val machineId: String,
    val amount: Double,
    val credits: Int,
    val type: String, // "GAME_SESSION", "TOP_UP", "REWARD"
    val timestamp: String,
    val status: String // "SUCCESS", "PENDING", "FAILED"
)

data class ArcadeMachine(
    val machineId: String,
    val name: String,
    val tagId: String,
    val costCredits: Int,
    val costAmount: Double,
    val genre: String,
    val difficulty: String,
    val emoji: String
)

// Typealias so TapScreen can use ArcadeGame interchangeably with ArcadeMachine
typealias ArcadeGame = ArcadeMachine

data class StartTransactionRequest(
    val tagId: String,
    val userId: Long = 1,
    val timestamp: Long = System.currentTimeMillis()
)

data class TransactionResponse(
    val authorized: Boolean,
    val message: String,
    val transactionId: String? = null,
    val sessionDuration: Int = 300, // seconds
    val creditsDeducted: Int = 10,
    val remainingBalance: Double = 0.0,
    val machineName: String? = "Air Hockey Blitz",
    val machineId: String? = "MACH-A14"
)

data class TopUpRequest(
    val userId: Long = 1,
    val amount: Double,
    val paymentMethod: String = "RAZORPAY_TEST",
    val paymentId: String = "pay_test_" + System.currentTimeMillis()
)

data class MachineSessionStatus(
    val machineId: String,
    val sessionStatus: String = "IDLE", // "IDLE", "ACTIVE", "EXPIRED"
    val sessionDuration: Int = 0,
    val timeRemaining: Int = 0,
    val lastTransactionId: String? = null,
    val userName: String? = null,
    val gameName: String? = null
)
