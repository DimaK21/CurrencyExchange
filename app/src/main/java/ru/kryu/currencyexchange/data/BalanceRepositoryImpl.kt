package ru.kryu.currencyexchange.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.kryu.currencyexchange.domain.BalanceRepository
import ru.kryu.currencyexchange.domain.model.Currency
import javax.inject.Inject

class BalanceRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : BalanceRepository {

    override fun getBalances(): Flow<Map<Currency, Double>> {
        return dataStore.data.map { preferences ->
            Currency.entries.associateWith { currency ->
                preferences[currency.name.toPreferencesKey()] ?: 100.0
            }
        }
    }

    private fun String.toPreferencesKey(): Preferences.Key<Double> =
        doublePreferencesKey(this)


    override suspend fun updateBalance(
        currencyFrom: Currency,
        amountFrom: Double,
        currencyTo: Currency,
        amountTo: Double
    ) {
        dataStore.edit { preferences ->
            val fromKey = doublePreferencesKey(currencyFrom.name)
            val toKey = doublePreferencesKey(currencyTo.name)
            val currentFromBalance = preferences[fromKey] ?: 100.0
            val currentToBalance = preferences[toKey] ?: 100.0
            preferences[fromKey] = currentFromBalance - amountFrom
            preferences[toKey] = currentToBalance + amountTo
        }
    }
}