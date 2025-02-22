package ru.kryu.currencyexchange.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.kryu.currencyexchange.domain.ExchangeRateRepository
import ru.kryu.currencyexchange.domain.model.Currency
import javax.inject.Inject

class ExchangeRateRepositoryImpl @Inject constructor(
    private val networkClient: NetworkClient,
) : ExchangeRateRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val exchangeRatesFlow: StateFlow<Map<Currency, Double>> =
        networkClient.getExchangeRates()
            .map { value ->
                mapOf(
                    Currency.EUR to (value.rates.eur ?: 0.0),
                    Currency.USD to (value.rates.usd ?: 0.0),
                    Currency.GBP to (value.rates.gbp ?: 0.0),
                )
            }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = emptyMap()
            )

    override fun getExchangeRates(): StateFlow<Map<Currency, Double>> = exchangeRatesFlow
}