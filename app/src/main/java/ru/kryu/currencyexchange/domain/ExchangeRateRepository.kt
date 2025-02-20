package ru.kryu.currencyexchange.domain

import io.reactivex.Observable

interface ExchangeRateRepository {
    fun getExchangeRates(currencies: List<String>): Observable<Map<String, Double>>
}