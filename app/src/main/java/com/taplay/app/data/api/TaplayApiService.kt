package com.taplay.app.data.api

import com.taplay.app.data.model.ArcadeMachine
import com.taplay.app.data.model.MachineSessionStatus
import com.taplay.app.data.model.StartTransactionRequest
import com.taplay.app.data.model.TopUpRequest
import com.taplay.app.data.model.Transaction
import com.taplay.app.data.model.TransactionResponse
import com.taplay.app.data.model.UserWallet
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TaplayApiService {

    @GET("api/v1/user/wallet")
    suspend fun getWallet(@Query("userId") userId: Long = 1): Response<UserWallet>

    @POST("api/v1/user/topup")
    suspend fun topUpWallet(@Body request: TopUpRequest): Response<UserWallet>

    @GET("api/v1/user/transactions")
    suspend fun getTransactions(@Query("userId") userId: Long = 1): Response<List<Transaction>>

    @GET("api/v1/machines/{tagId}")
    suspend fun getMachineByTagId(@Path("tagId") tagId: String): Response<ArcadeMachine>

    @POST("api/v1/transaction/start")
    suspend fun startTransaction(@Body request: StartTransactionRequest): Response<TransactionResponse>

    @GET("api/v1/transaction/status")
    suspend fun getMachineSessionStatus(@Query("machineId") machineId: String): Response<MachineSessionStatus>

    @POST("api/v1/transaction/end")
    suspend fun endTransaction(
        @Query("transactionId") transactionId: String? = null,
        @Query("machineId") machineId: String? = null
    ): Response<Boolean>
}

