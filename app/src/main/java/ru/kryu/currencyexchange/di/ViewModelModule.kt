package ru.kryu.currencyexchange.di

import dagger.Module
import dagger.Provides
import ru.kryu.currencyexchange.domain.BalanceRepository
import ru.kryu.currencyexchange.domain.ExchangeRateRepository
import javax.inject.Provider

@Module
object ViewModelModule {
    @Provides
    fun provideCurrencyExchangeViewModelFactory(
        exchangeRateRepository: Provider<ExchangeRateRepository>,
        balanceRepository: Provider<BalanceRepository>
    ): MainViewModelFactory {
        return MainViewModelFactory(exchangeRateRepository, balanceRepository)
    }
}