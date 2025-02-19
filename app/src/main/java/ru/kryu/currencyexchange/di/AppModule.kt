package ru.kryu.currencyexchange.di

import dagger.Module
import dagger.Provides
import ru.kryu.currencyexchange.data.BalanceRepositoryImpl
import ru.kryu.currencyexchange.data.ExchangeRateRepositoryImpl
import ru.kryu.currencyexchange.domain.BalanceRepository
import ru.kryu.currencyexchange.domain.ExchangeRateRepository
import javax.inject.Singleton

@Module
object AppModule {

    @Provides
    @Singleton
    fun provideExchangeRateRepository(): ExchangeRateRepository = ExchangeRateRepositoryImpl()

    @Provides
    @Singleton
    fun provideBalanceRepository(): BalanceRepository = BalanceRepositoryImpl()
}