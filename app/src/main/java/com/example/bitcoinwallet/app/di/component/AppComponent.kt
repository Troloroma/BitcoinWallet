package com.example.bitcoinwallet.app.di.component

import android.app.Application
import com.example.bitcoinwallet.MainActivity
import com.example.bitcoinwallet.app.di.modules.DataModule
import com.example.bitcoinwallet.app.di.modules.DomainModule
import com.example.bitcoinwallet.app.di.modules.KeyStoreModule
import com.example.bitcoinwallet.app.di.modules.NetworkModule
import com.example.bitcoinwallet.app.di.modules.ViewModelModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        DataModule::class,
        DomainModule::class,
        NetworkModule::class,
        ViewModelModule::class,
        KeyStoreModule::class
    ]
)
interface AppComponent {

    fun injectMainActivity(mainActivity: MainActivity)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance app: Application
        ): AppComponent
    }
}