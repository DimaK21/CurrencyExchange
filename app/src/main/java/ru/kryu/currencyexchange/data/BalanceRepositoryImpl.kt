package ru.kryu.currencyexchange.data

import android.content.SharedPreferences
import io.reactivex.Completable
import io.reactivex.Single
import ru.kryu.currencyexchange.domain.BalanceRepository
import javax.inject.Inject

class BalanceRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : BalanceRepository {

    private val defaultBalances = mapOf("USD" to 100.0, "EUR" to 100.0, "GBP" to 100.0)

    override fun getBalances(): Single<Map<String, Double>> {
        return Single.fromCallable {
            defaultBalances.mapValues { (currency, defaultValue) ->
                sharedPreferences.getFloat(currency, defaultValue.toFloat()).toDouble()
            }
        }
    }

    override fun updateBalance(currency: String, amount: Double): Completable {
        return Completable.fromAction {
            sharedPreferences.edit().putFloat(currency, amount.toFloat()).apply()
        }
    }
}