package ru.kryu.currencyexchange.data.network

import retrofit2.Response
import retrofit2.http.GET

interface ExchangeRateApi {
    @GET("latest.js")
    fun getLatestRates(): Response<ExchangeRateResponse>
}