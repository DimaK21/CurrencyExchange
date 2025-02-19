package ru.kryu.currencyexchange.presentation

import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import ru.kryu.currencyexchange.domain.BalanceRepository
import ru.kryu.currencyexchange.domain.ExchangeRateRepository
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val exchangeRateRepository: ExchangeRateRepository,
    private val balanceRepository: BalanceRepository,
) : ViewModel() {

    private val selectedFromCurrency = BehaviorSubject.createDefault("USD")
    private val selectedToCurrency = BehaviorSubject.createDefault("EUR")
    private val enteredAmount = BehaviorSubject.createDefault(0.0)
    private val exchangeResult = BehaviorSubject.create<String>()

    private val balancesSubject = BehaviorSubject.create<Map<String, Double>>()
    val balances: Observable<Map<String, Double>> = balancesSubject.hide()

    init {
        balanceRepository.getBalances()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { balancesSubject.onNext(it) }
    }

    fun getExchangeResult(): Observable<String> = exchangeResult.hide()

    val exchangeRateText: Observable<String> = Observable.combineLatest(
        selectedFromCurrency,
        selectedToCurrency,
        exchangeRateRepository.getExchangeRates()
    ) { from, to, rates ->
        "1 $from = ${rates[to] ?: 1.0} $to"
    }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    val convertedAmount: Observable<Double> = Observable.combineLatest(
        enteredAmount,
        selectedFromCurrency,
        selectedToCurrency,
        exchangeRateRepository.getExchangeRates()
    ) { amount, from, to, rates ->
        val rate = rates[to] ?: 1.0
        if (amount > 0) amount * rate else 0.0
    }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun onAmountEntered(currency: String, amount: Double) {
        enteredAmount.onNext(amount)
    }

    fun setFromCurrency(currency: String) {
        selectedFromCurrency.onNext(currency)
    }

    fun setToCurrency(currency: String) {
        selectedToCurrency.onNext(currency)
    }

    fun exchange() {
        Single.zip(
            balanceRepository.getBalances().subscribeOn(Schedulers.io()),
            exchangeRateRepository.getExchangeRates().subscribeOn(Schedulers.io()),
            { balances, rates ->
                val fromCurrency = selectedFromCurrency.value ?: "USD"
                val toCurrency = selectedToCurrency.value ?: "EUR"
                val amount = enteredAmount.value ?: 0.0
                val rate = rates[toCurrency] ?: 1.0
                val converted = amount * rate

                val fromBalance = balances[fromCurrency] ?: 0.0
                val toBalance = balances[toCurrency] ?: 0.0

                if (amount > fromBalance) {
                    "Недостаточно средств на счёте!"
                } else {
                    balanceRepository.updateBalance(fromCurrency, fromBalance - amount)
                    balanceRepository.updateBalance(toCurrency, toBalance + converted)
                    "Обмен выполнен:\n-$amount $fromCurrency\n+$converted $toCurrency"
                }
            }
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { message -> exchangeResult.onNext(message) }
    }
}