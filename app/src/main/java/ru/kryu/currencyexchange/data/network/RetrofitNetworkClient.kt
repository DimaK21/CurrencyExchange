package ru.kryu.currencyexchange.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retryWhen
import ru.kryu.currencyexchange.data.NetworkClient
import javax.inject.Inject

class RetrofitNetworkClient @Inject constructor(
    private val exchangeRateApi: ExchangeRateApi
) : NetworkClient {

    override fun getExchangeRates(): Flow<ExchangeRateResponse> = flow {
        while (true) {
            val response = exchangeRateApi.getLatestRates()
            response.body()?.let { emit(it) }
            delay(30_000)
        }
    }.retryWhen { cause, _ ->
        cause is Exception
    }.flowOn(Dispatchers.IO)
}