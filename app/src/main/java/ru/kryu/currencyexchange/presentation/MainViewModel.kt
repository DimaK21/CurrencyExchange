package ru.kryu.currencyexchange.presentation

import androidx.lifecycle.ViewModel
import ru.kryu.currencyexchange.domain.BalanceRepository
import ru.kryu.currencyexchange.domain.ExchangeRateRepository
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val exchangeRateRepository: ExchangeRateRepository,
    private val balanceRepository: BalanceRepository,
) : ViewModel() {
}