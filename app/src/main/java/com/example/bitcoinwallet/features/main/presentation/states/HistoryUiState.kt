package com.example.bitcoinwallet.features.main.presentation.states

import com.example.bitcoinwallet.features.main.presentation.model.TxHistoryItem

data class HistoryUiState(
    val items: List<TxHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)