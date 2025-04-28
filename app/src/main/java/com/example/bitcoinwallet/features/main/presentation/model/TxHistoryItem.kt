package com.example.bitcoinwallet.features.main.presentation.model

data class TxHistoryItem(
    val txId: String,
    val timestamp: Long,
    val confirmed: Boolean,
    val direction: Direction,
    val counterparty: String,
    val amount: Long
)

enum class Direction { IN, OUT }