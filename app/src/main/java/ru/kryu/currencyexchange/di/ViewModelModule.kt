package ru.kryu.currencyexchange.di

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.kryu.currencyexchange.domain.api.BalanceRepository
import ru.kryu.currencyexchange.domain.api.CurrencyConverter
import ru.kryu.currencyexchange.domain.api.ExchangeRateRepository
import javax.inject.Provider

@Module
object ViewModelModule {
    @Provides
    fun provideCurrencyExchangeViewModelFactory(
        exchangeRateRepository: Provider<ExchangeRateRepository>,
        balanceRepository: Provider<BalanceRepository>,
        currencyConverter: Provider<CurrencyConverter>,
        context: Provider<Context>,
    ): MainViewModelFactory {
        return MainViewModelFactory(
            exchangeRateRepository,
            balanceRepository,
            currencyConverter,
            context
        )
    }
}