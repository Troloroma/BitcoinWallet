package com.example.bitcoinwallet.features.main.presentation.states

sealed class TransactionEvent {
    data class Success(val txId: String) : TransactionEvent()
    data class Failure(val message: String) : TransactionEvent()
}