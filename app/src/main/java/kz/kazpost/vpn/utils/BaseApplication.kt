package kz.kazpost.vpn.utils

import android.app.Application
import kz.kazpost.vpn.di.appModule
import kz.kazpost.vpn.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level

class BaseApplication: Application(){

    override fun onCreate() {
        super.onCreate()
        startKoin{
            androidLogger(Level.DEBUG)
            androidContext(this@BaseApplication)
            modules(listOf(appModule, networkModule))
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        stopKoin()
    }
}