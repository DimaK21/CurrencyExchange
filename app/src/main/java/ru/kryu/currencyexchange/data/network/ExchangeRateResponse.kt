package ru.kryu.currencyexchange.data.network

import com.google.gson.annotations.SerializedName

data class ExchangeRateResponse(
    @SerializedName("base")
    val base: String,
    @SerializedName("rates")
    val rates: Rates
) {
    data class Rates(
        @SerializedName("GBP")
        val gbp: Double?,
        @SerializedName("USD")
        val usd: Double?,
        @SerializedName("EUR")
        val eur: Double?,
    )
}


