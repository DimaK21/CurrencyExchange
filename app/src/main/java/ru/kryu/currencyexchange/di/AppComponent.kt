package ru.kryu.currencyexchange.di

import dagger.Component
import ru.kryu.currencyexchange.presentation.MainActivity
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(activity: MainActivity)
}