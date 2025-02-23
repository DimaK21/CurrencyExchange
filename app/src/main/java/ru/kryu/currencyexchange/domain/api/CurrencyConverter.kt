package ru.kryu.currencyexchange.domain.api

import ru.kryu.currencyexchange.domain.model.Currency

interface CurrencyConverter {
    fun convert(amount: Double, from: Currency, to: Currency): Double
}