package com.example.bitcoinwallet.app.app

import android.app.Application
import com.example.bitcoinwallet.app.di.component.AppComponent
import com.example.bitcoinwallet.app.di.component.DaggerAppComponent

class App : Application() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.factory().create(this)
    }
}
