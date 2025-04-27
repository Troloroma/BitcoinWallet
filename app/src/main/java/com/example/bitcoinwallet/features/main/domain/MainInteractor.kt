package com.example.bitcoinwallet.features.main.domain

import com.example.bitcoinwallet.common.Entity
import com.example.bitcoinwallet.features.main.presentation.model.BalanceEntity
import kotlinx.coroutines.flow.Flow

interface MainInteractor {
    suspend fun getAddress(): Entity<String>
    suspend fun getBalance(address: String): Flow<Entity<BalanceEntity>>
    suspend fun sendCoins(amountBtcToSend: String, addressToSend: String): Entity<String>
}