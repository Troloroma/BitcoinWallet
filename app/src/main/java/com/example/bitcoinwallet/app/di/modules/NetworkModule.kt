package com.example.bitcoinwallet.app.di.modules

import com.example.bitcoinwallet.BuildConfig
import com.example.bitcoinwallet.data.provider.NetworkProvider
import com.example.bitcoinwallet.network.api.Api
import dagger.Module
import dagger.Provides

@Module
class NetworkModule {

    @Provides
    fun provideNetworkProvider() = NetworkProvider(host = BuildConfig.HOST_API)

    @Provides
    fun provideApi(
        networkProvider: NetworkProvider
    ): Api = networkProvider.provideRetrofit(Api::class.java)
}