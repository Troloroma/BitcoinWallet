package com.example.bitcoinwallet.app.di.modules

import com.example.bitcoinwallet.domain.MainInteractorImpl
import com.example.bitcoinwallet.features.main.domain.MainInteractor
import com.example.bitcoinwallet.features.main.domain.MainRepository
import dagger.Module
import dagger.Provides

@Module(includes = [DataModule::class])
class DomainModule {

    @Provides
    fun provideMainInteractor(
        mainRepository: MainRepository
    ): MainInteractor = MainInteractorImpl(
        mainRepository
    )
}