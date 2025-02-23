package ru.kryu.currencyexchange.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.kryu.currencyexchange.R
import ru.kryu.currencyexchange.domain.api.BalanceRepository
import ru.kryu.currencyexchange.domain.api.CurrencyConverter
import ru.kryu.currencyexchange.domain.api.ExchangeRateRepository
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val exchangeRateRepository: ExchangeRateRepository,
    private val balanceRepository: BalanceRepository,
    private val currencyConverter: CurrencyConverter,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ScreenState())
    val state: StateFlow<ScreenState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            balanceRepository.getBalances().collect { map ->
                _state.update {
                    it.copy(
                        balances = map,
                        currencyList = map.keys.toList()
                    )
                }
                recalculate()
            }
        }
        viewModelScope.launch {
            exchangeRateRepository.getExchangeRates().collect { map ->
                _state.update { it.copy(exchangeRates = map) }
            }
        }
    }

    fun exchange() {
        val stateValue = _state.value
        val fromCurrency = stateValue.currencyList.getOrNull(stateValue.positionFrom)
        val toCurrency = stateValue.currencyList.getOrNull(stateValue.positionTo)
        val amount = stateValue.enteredAmounts[fromCurrency?.name] ?: 0.0

        if (fromCurrency == null || toCurrency == null || fromCurrency == toCurrency) {
            _state.update { it.copy(message = context.getString(R.string.select_different_currencies)) }
            return
        }

        val rate = currencyConverter.convert(1.0, fromCurrency, toCurrency)
        if (rate == 0.0) {
            _state.update { it.copy(message = context.getString(R.string.exchange_rate_not_available)) }
            return
        }

        val convertedAmount = amount * rate
        val balanceFrom = stateValue.balances[fromCurrency] ?: 0.0

        if (amount <= 0 || amount > balanceFrom) {
            _state.update { it.copy(message = context.getString(R.string.insufficient_funds)) }
            return
        }

        viewModelScope.launch {
            balanceRepository.updateBalance(
                fromCurrency,
                -amount,
                toCurrency,
                convertedAmount
            )
            _state.update {
                it.copy(
                    message = context.getString(
                        R.string.exchange_completed,
                        "%.2f".format(amount),
                        fromCurrency,
                        "%.2f".format(convertedAmount),
                        toCurrency
                    )
                )
            }
        }
    }

    fun updatePositionFrom(position: Int) {
        _state.update { it.copy(positionFrom = position) }
        recalculate()
    }

    fun updatePositionTo(position: Int) {
        _state.update { it.copy(positionTo = position) }
        recalculate()
    }

    fun onAmountEntered(currency: String, amount: Double) {
        _state.update { currentState ->
            currentState.copy(
                enteredAmounts = currentState.enteredAmounts.toMutableMap().apply {
                    put(currency, amount)
                },
                enteredAmount = amount
            )
        }
        recalculate()
    }

    private fun recalculate() {
        val stateValue = _state.value
        val fromCurrency = stateValue.currencyList.getOrNull(stateValue.positionFrom)
        val toCurrency = stateValue.currencyList.getOrNull(stateValue.positionTo)
        val amount = stateValue.enteredAmount
        if (fromCurrency != null && toCurrency != null) {
            val exchangeRate = currencyConverter.convert(1.0, fromCurrency, toCurrency)
            val convertedAmount = currencyConverter.convert(amount, fromCurrency, toCurrency)
            _state.update {
                it.copy(
                    convertedAmount = convertedAmount,
                    exchangeRate = exchangeRate
                )
            }
        } else {
            _state.update { it.copy(convertedAmount = 0.0, exchangeRate = 0.0) }
        }
    }

    fun clearErrorMessage() {
        _state.update { it.copy(message = null) }
    }
}