package ru.kryu.currencyexchange.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.kryu.currencyexchange.presentation.MainActivity
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, ViewModelModule::class])
interface AppComponent {
    fun inject(activity: MainActivity)
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }
}