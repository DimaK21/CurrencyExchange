package ru.kryu.currencyexchange.domain

import ru.kryu.currencyexchange.domain.model.Currency
import javax.inject.Inject

class CurrencyConverterImpl @Inject constructor(
    exchangeRateRepository: ExchangeRateRepository
) : CurrencyConverter {

    private val exchangeRates = exchangeRateRepository.getExchangeRates()

    private fun getExchangeRate(from: Currency, to: Currency): Double? {
        val rates = exchangeRates.value
        val fromRate = rates[from] ?: return null
        val toRate = rates[to] ?: return null

        return if (toRate == 0.0) {
            null
        } else {
            fromRate / toRate
        }
    }

    override fun convert(amount: Double, from: Currency, to: Currency): Double {
        val rate = getExchangeRate(from, to)
        return if (rate != null) amount * rate else 0.0
    }
}