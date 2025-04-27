package com.example.bitcoinwallet.features.main.domain

import com.example.bitcoinwallet.common.Entity
import com.example.bitcoinwallet.data.model.WalletModel

interface WalletRepository {
    suspend fun getWallet() : Entity<WalletModel>
}