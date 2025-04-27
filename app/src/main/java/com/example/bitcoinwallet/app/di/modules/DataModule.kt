package com.example.bitcoinwallet.app.di.modules

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.bitcoinwallet.data.repository.KeyStoreManager
import com.example.bitcoinwallet.data.repository.MainRepositoryImpl
import com.example.bitcoinwallet.data.repository.WalletRepositoryImpl
import com.example.bitcoinwallet.features.main.domain.MainRepository
import com.example.bitcoinwallet.features.main.domain.WalletRepository
import com.example.bitcoinwallet.network.api.Api
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [NetworkModule::class])
class DataModule {
    @Provides
    fun provideMainRepository(
        api: Api
    ): MainRepository = MainRepositoryImpl(
        api
    )

    @Provides
    fun provideWalletRepository(
        keyStoreManager: KeyStoreManager,
        dataStore: DataStore<Preferences>
    ): WalletRepository = WalletRepositoryImpl(
        keyStoreManager,
        dataStore
    )

    @Provides
    @Singleton
    fun providePreferencesDataStore(app: Application): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { app.preferencesDataStoreFile("wallet_prefs.pb") }
        )
}