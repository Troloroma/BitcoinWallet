package com.example.bitcoinwallet.navigation

sealed class Destinations(val route: String) {
    data object MainGraph : Destinations("main")

}