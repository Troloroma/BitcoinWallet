package com.example.bitcoinwallet.features.main.domain

import com.example.bitcoinwallet.common.Entity
import com.example.bitcoinwallet.network.dto.TransactionResponse
import com.example.bitcoinwallet.network.dto.UtxoResponse
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    suspend fun getUtxoList(address : String) : Flow<Entity<List<UtxoResponse>>>
    suspend fun sendCoins(hex: String) : Entity<String>
    suspend fun getTxHistory(address: String, lastTxId: String?): Flow<Entity<List<TransactionResponse>>>
    suspend fun getFeeEstimates() : Map<Int, Double>
}