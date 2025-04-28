package com.example.bitcoinwallet.features.main.presentation.states

import com.example.bitcoinwallet.features.main.presentation.model.MainEntity

sealed class MainScreenState {
    data object Loading : MainScreenState()
    data class Success(val data: MainEntity) : MainScreenState()
    data class Error(val message: String) : MainScreenState()
}