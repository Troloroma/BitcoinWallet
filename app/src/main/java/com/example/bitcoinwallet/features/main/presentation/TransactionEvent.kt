package com.example.bitcoinwallet.features.main.presentation

sealed class TransactionEvent {
    data class Success(val txId: String) : TransactionEvent()
    data class Failure(val message: String) : TransactionEvent()
}