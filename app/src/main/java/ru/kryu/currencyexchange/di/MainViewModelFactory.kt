package ru.kryu.currencyexchange.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.kryu.currencyexchange.domain.api.BalanceRepository
import ru.kryu.currencyexchange.domain.api.CurrencyConverter
import ru.kryu.currencyexchange.domain.api.ExchangeRateRepository
import ru.kryu.currencyexchange.presentation.MainViewModel
import javax.inject.Inject
import javax.inject.Provider

class MainViewModelFactory @Inject constructor(
    private val exchangeRateRepository: Provider<ExchangeRateRepository>,
    private val balanceRepository: Provider<BalanceRepository>,
    private val currencyConverter: Provider<CurrencyConverter>,
    private val context: Provider<Context>,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                exchangeRateRepository.get(),
                balanceRepository.get(),
                currencyConverter.get(),
                context.get(),
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}