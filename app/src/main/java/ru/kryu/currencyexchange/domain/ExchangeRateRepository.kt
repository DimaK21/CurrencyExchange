package ru.kryu.currencyexchange.domain

import io.reactivex.Observable

interface ExchangeRateRepository {
    fun getExchangeRates(): Observable<Map<String, Double>>
}