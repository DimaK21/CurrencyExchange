package ru.kryu.currencyexchange.domain

import kotlinx.coroutines.flow.Flow
import ru.kryu.currencyexchange.domain.model.Currency

interface BalanceRepository {
    fun getBalances(): Flow<Map<Currency, Double>>
    suspend fun updateBalance(
        currencyFrom: Currency,
        amountFrom: Double,
        currencyTo: Currency,
        amountTo: Double
    )
}