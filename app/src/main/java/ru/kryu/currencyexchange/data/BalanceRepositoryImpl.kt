package ru.kryu.currencyexchange.data

import android.content.SharedPreferences
import io.reactivex.Single
import ru.kryu.currencyexchange.domain.BalanceRepository
import javax.inject.Inject

class BalanceRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : BalanceRepository {

    override fun getBalances(): Single<Map<String, Double>> {
        return Single.fromCallable {
            val balances = mutableMapOf<String, Double>()
            listOf("USD", "EUR", "GBP").forEach { currency ->
                val balanceString = sharedPreferences.getString(currency, null)
                balances[currency] =
                    balanceString?.toDoubleOrNull() ?: 100.0
            }
            balances
        }
    }

    override fun updateBalance(currency: String, amount: Double): Single<Boolean> {
        return Single.fromCallable {
            val editor = sharedPreferences.edit()
            editor.putString(currency, amount.toString())
            editor.apply()
        }
            .map { true }
            .onErrorReturn { false }
    }
}