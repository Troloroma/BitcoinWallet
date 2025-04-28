package com.example.bitcoinwallet.app.di.modules

import com.example.bitcoinwallet.data.keystore.KeyStoreManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


const val KEY = "keyStoreManager"

@Module
class KeyStoreModule {

    @Provides
    @Singleton
    fun provideKeyStoreManager(): KeyStoreManager = KeyStoreManager(keyAlias = KEY)
}