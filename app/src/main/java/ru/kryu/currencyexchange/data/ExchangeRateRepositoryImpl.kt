package ru.kryu.currencyexchange.data

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.kryu.currencyexchange.data.network.ExchangeRateApi
import ru.kryu.currencyexchange.domain.ExchangeRateRepository
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ExchangeRateRepositoryImpl @Inject constructor(
    private val api: ExchangeRateApi,
) : ExchangeRateRepository {

    private val cachedRates = ConcurrentHashMap<String, Double>()
    private var lastFetchTime = 0L
    private val cacheExpirationTime = TimeUnit.MINUTES.toMillis(5)

    override fun getExchangeRates(currencies: List<String>): Observable<Map<String, Double>> {
        return Observable.interval(0, 30, TimeUnit.SECONDS)
            .flatMapSingle {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastFetchTime < cacheExpirationTime && cachedRates.isNotEmpty()) {
                    Single.just(cachedRates)
                } else {
                    api.getLatestRates()
                        .map { response ->
                            val filteredRates = mutableMapOf<String, Double>()
                            currencies.forEach { currency ->
                                response.rates[currency]?.let { rate ->
                                    filteredRates[currency] = rate
                                }
                            }
                            cachedRates.clear()
                            cachedRates.putAll(filteredRates)
                            lastFetchTime = currentTime
                            filteredRates
                        }
                        .map { it.toMap() }
                        .subscribeOn(Schedulers.io())
                        .doOnError { throwable ->
                            println("Error fetching exchange rates: ${throwable.message}")
                        }
                }
            }
            .retry()
            .distinctUntilChanged()
    }
}