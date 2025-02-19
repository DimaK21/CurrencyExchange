package ru.kryu.currencyexchange.data.network

import io.reactivex.Single
import retrofit2.http.GET

interface ExchangeRateApi {
    @GET("latest.js")
    fun getLatestRates(): Single<ExchangeRateResponse>
}