package ru.kryu.currencyexchange.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.kryu.currencyexchange.domain.BalanceRepository
import ru.kryu.currencyexchange.domain.ExchangeRateRepository
import ru.kryu.currencyexchange.presentation.MainViewModel
import javax.inject.Inject
import javax.inject.Provider

class MainViewModelFactory @Inject constructor(
    private val exchangeRateRepository: Provider<ExchangeRateRepository>,
    private val balanceRepository: Provider<BalanceRepository>
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                exchangeRateRepository.get(),
                balanceRepository.get()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}