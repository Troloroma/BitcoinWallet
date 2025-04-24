package com.example.bitcoinwallet.app.di.modules

import com.example.bitcoinwallet.data.repository.MainRepositoryImpl
import com.example.bitcoinwallet.features.main.domain.MainRepository
import com.example.bitcoinwallet.network.api.Api
import dagger.Module
import dagger.Provides

@Module(includes = [NetworkModule::class])
class DataModule {
    @Provides
    fun provideMainRepository(
        api: Api
    ) : MainRepository = MainRepositoryImpl(
        api
    )
}