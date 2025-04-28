package com.example.bitcoinwallet.features.main.presentation.states

sealed class TransactionEventState {
    data class Success(val txId: String) : TransactionEventState()
    data class Failure(val message: String) : TransactionEventState()
}