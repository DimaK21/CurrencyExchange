package ru.kryu.currencyexchange.presentation

import ru.kryu.currencyexchange.domain.model.Currency

data class ScreenState(
    val exchangeRates: Map<Currency, Double> = emptyMap(),
    val balances: Map<Currency, Double> = emptyMap(),
    val positionFrom: Int = 0,
    val positionTo: Int = 0,
    val currencyList: List<Currency> = emptyList(),
    val exchangeRate: Double = 0.0,
    val enteredAmount: Double = 0.0,
    val convertedAmount: Double = 0.0,
)
