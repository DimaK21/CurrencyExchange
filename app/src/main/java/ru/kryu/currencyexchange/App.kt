package ru.kryu.currencyexchange

import android.app.Application
import ru.kryu.currencyexchange.di.AppComponent
import ru.kryu.currencyexchange.di.DaggerAppComponent

class App : Application(){
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.create()
    }
}