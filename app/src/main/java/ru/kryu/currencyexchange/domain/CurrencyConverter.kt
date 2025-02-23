package ru.kryu.currencyexchange.domain

import ru.kryu.currencyexchange.domain.model.Currency

interface CurrencyConverter {
    fun convert(amount: Double, from: Currency, to: Currency): Double
}