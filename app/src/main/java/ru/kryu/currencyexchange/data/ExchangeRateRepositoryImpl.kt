package ru.kryu.currencyexchange.data

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import ru.kryu.currencyexchange.data.network.ExchangeRateApi
import ru.kryu.currencyexchange.domain.ExchangeRateRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ExchangeRateRepositoryImpl @Inject constructor(
    private val api: ExchangeRateApi
) : ExchangeRateRepository {

    override fun getExchangeRates(): Observable<Map<String, Double>> {
        return Observable.interval(0, 30, TimeUnit.SECONDS)
            .flatMapSingle {
                api.getLatestRates()
                    .map { response ->
                        mapOf(
                            "USD" to response.rates["USD"]!!,
                            "EUR" to response.rates["EUR"]!!,
                            "GBP" to response.rates["GBP"]!!
                        )
                    }
                    .subscribeOn(Schedulers.io())
            }
            .retry()
            .distinctUntilChanged()
    }
}