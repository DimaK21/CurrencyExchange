package ru.kryu.currencyexchange.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.kryu.currencyexchange.domain.ExchangeRateRepository
import ru.kryu.currencyexchange.domain.model.Currency
import javax.inject.Inject

class ExchangeRateRepositoryImpl @Inject constructor(
    private val networkClient: NetworkClient,
) : ExchangeRateRepository {

    override fun getExchangeRates(): Flow<Map<Currency, Double>> {
        return networkClient.getExchangeRates()
            .map { value ->
                mapOf(
                    Currency.EUR to (value.rates.eur ?: 0.0),
                    Currency.USD to (value.rates.usd ?: 0.0),
                    Currency.GBP to (value.rates.gbp ?: 0.0),
                )
            }
    }
}