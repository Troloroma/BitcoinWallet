package com.example.bitcoinwallet.features.main.domain

import com.example.bitcoinwallet.common.Entity
import com.example.bitcoinwallet.network.dto.UtxoResponse
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    suspend fun getUtxoList(address : String) : Flow<Entity<List<UtxoResponse>>>
    suspend fun sendCoins(hex: String) : Entity<String>
}