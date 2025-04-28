package com.example.bitcoinwallet.app.di.modules

import com.example.bitcoinwallet.domain.interactors.MainInteractorImpl
import com.example.bitcoinwallet.features.main.domain.MainInteractor
import com.example.bitcoinwallet.features.main.domain.MainRepository
import com.example.bitcoinwallet.features.main.domain.WalletRepository
import dagger.Module
import dagger.Provides

@Module(includes = [DataModule::class])
class DomainModule {

    @Provides
    fun provideMainInteractor(
        mainRepository: MainRepository,
        walletRepository: WalletRepository
    ): MainInteractor = MainInteractorImpl(
        mainRepository,
        walletRepository
    )
}