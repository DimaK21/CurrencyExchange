package ru.kryu.currencyexchange.domain

import kotlinx.coroutines.flow.Flow
import ru.kryu.currencyexchange.domain.model.Currency

interface ExchangeRateRepository {
    fun getExchangeRates(): Flow<Map<Currency, Double>>
}