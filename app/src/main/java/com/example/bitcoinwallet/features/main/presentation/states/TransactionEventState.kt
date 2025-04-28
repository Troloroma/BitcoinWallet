package com.example.bitcoinwallet.features.main.presentation.states


sealed class TransactionEventState {
    data object Handled : TransactionEventState()
    data class Triggered(val event: TransactionEvent) : TransactionEventState()
}

data class TransactionEvent(
    val type: EventType,
    val message: String? = null,
    val txId: String? = null
) {
    sealed class EventType {
        data object Success : EventType()
        data object Failure : EventType()
    }
}