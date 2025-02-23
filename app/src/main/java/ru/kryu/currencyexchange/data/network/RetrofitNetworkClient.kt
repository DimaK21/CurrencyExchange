package ru.kryu.currencyexchange.data.network

import android.util.Log
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
            response.body()?.let {
                Log.e("RetrofitNetworkClient", response.body().toString())
                emit(it)
            }
            delay(30_000)
        }
    }.retryWhen { cause, _ ->
        Log.e("RetrofitNetworkClient", "retry exchangeRateApi")
        delay(10_000)
        cause is Exception
    }.flowOn(Dispatchers.IO)
}