package ru.kryu.currencyexchange

import android.app.Application
import ru.kryu.currencyexchange.di.AppComponent
import ru.kryu.currencyexchange.di.DaggerAppComponent

class App : Application(){
    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(this)
    }
}