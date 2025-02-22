package ru.kryu.currencyexchange.domain

import android.util.Log
import ru.kryu.currencyexchange.domain.model.Currency
import javax.inject.Inject

class CurrencyConverterImpl @Inject constructor(
    exchangeRateRepository: ExchangeRateRepository
) : CurrencyConverter {

    private val exchangeRates = exchangeRateRepository.getExchangeRates()

    private fun getExchangeRate(from: Currency, to: Currency): Double? {
        val rates = exchangeRates.value
        if (rates.isEmpty()) {
            Log.e("CurrencyConverterImpl", "Курсы валют ещё не загружены")
            return null
        }

        val fromRate = rates[from]
        val toRate = rates[to]

        if (fromRate == null || toRate == null) {
            Log.e("CurrencyConverterImpl", "Курс не найден: $from -> $to")
            return null
        }

        return if (toRate == 0.0) {
            Log.e("CurrencyConverterImpl", "Попытка деления на 0: $from -> $to")
            null
        } else {
            fromRate / toRate
        }
    }

    override fun convert(amount: Double, from: Currency, to: Currency): Double? {
        val rate = getExchangeRate(from, to)
        return if (rate != null) amount * rate else null
    }
}