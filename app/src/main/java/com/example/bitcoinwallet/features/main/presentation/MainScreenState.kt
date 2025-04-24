package com.example.bitcoinwallet.features.main.presentation

sealed class MainScreenState {
    data object Loading : MainScreenState()
    data object Error : MainScreenState()
    data object Success : MainScreenState()
}