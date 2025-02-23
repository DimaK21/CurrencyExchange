package ru.kryu.currencyexchange.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.kryu.currencyexchange.domain.BalanceRepository
import ru.kryu.currencyexchange.domain.CurrencyConverter
import ru.kryu.currencyexchange.domain.ExchangeRateRepository
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val exchangeRateRepository: ExchangeRateRepository,
    private val balanceRepository: BalanceRepository,
    private val currencyConverter: CurrencyConverter,
) : ViewModel() {

    private val _state = MutableStateFlow(ScreenState())
    val state: StateFlow<ScreenState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            balanceRepository.getBalances().collect { map ->
                _state.update { it.copy(balances = map) }
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
        val amount = stateValue.balances[fromCurrency] ?: 0.0
        val rate = stateValue.exchangeRates[fromCurrency] ?: return
        if (fromCurrency != null && toCurrency != null) {
            val convertedAmount = amount * rate
            if (amount <= 0 || amount > stateValue.balances[fromCurrency]!! || rate == 0.0) {
                return // TODO: показать диалог об ошибке
            } else {
                viewModelScope.launch {
                    balanceRepository.updateBalance(
                        fromCurrency,
                        -amount,
                        toCurrency,
                        convertedAmount
                    )
                }
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

    fun onAmountEntered(amount: Double) {
        _state.update { it.copy(enteredAmount = amount) }
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
}
