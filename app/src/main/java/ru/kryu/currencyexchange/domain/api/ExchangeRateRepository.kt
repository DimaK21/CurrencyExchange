package ru.kryu.currencyexchange.domain.api

import kotlinx.coroutines.flow.StateFlow
import ru.kryu.currencyexchange.domain.model.Currency

interface ExchangeRateRepository {
    fun getExchangeRates(): StateFlow<Map<Currency, Double>>
}