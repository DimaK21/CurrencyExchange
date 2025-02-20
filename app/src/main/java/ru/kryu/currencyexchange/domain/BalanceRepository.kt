package ru.kryu.currencyexchange.domain

import io.reactivex.Completable
import io.reactivex.Single

interface BalanceRepository {
    fun getBalances(): Single<Map<String, Double>>
    fun updateBalance(currency: String, amount: Double): Single<Boolean>
}