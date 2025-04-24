package com.example.bitcoinwallet.domain

import com.example.bitcoinwallet.features.main.domain.MainInteractor
import com.example.bitcoinwallet.features.main.domain.MainRepository

class MainInteractorImpl(
    val mainRepository: MainRepository
) : MainInteractor {

}