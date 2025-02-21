package ru.kryu.currencyexchange.data

import kotlinx.coroutines.flow.Flow
import ru.kryu.currencyexchange.data.network.ExchangeRateResponse

interface NetworkClient {
    fun getExchangeRates(): Flow<ExchangeRateResponse>
}