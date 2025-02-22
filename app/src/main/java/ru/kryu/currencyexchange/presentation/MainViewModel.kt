package ru.kryu.currencyexchange.presentation

import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import ru.kryu.currencyexchange.domain.BalanceRepository
import ru.kryu.currencyexchange.domain.CurrencyConverter
import ru.kryu.currencyexchange.domain.ExchangeRateRepository
import javax.inject.Inject
import kotlin.math.roundToInt

class MainViewModel @Inject constructor(
    private val exchangeRateRepository: ExchangeRateRepository,
    private val balanceRepository: BalanceRepository,
    private val currencyConverter: CurrencyConverter,
) : ViewModel() {

    private val disposables = CompositeDisposable()

    private val selectedFromCurrency = BehaviorSubject.createDefault("USD")
    private val selectedToCurrency = BehaviorSubject.createDefault("EUR")
    private val enteredAmount = BehaviorSubject.createDefault(0.0)
    private val exchangeResult = BehaviorSubject.createDefault("")

    private val balancesSubject = BehaviorSubject.create<Map<String, Double>>()
    val balances: Observable<Map<String, Double>> = balancesSubject.hide()
    private val list = listOf("USD","EUR","GBP")

    init {
        disposables.add(
            balanceRepository.getBalances()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ balancesSubject.onNext(it) }, Throwable::printStackTrace)
        )
    }

    fun getExchangeResult(): Observable<String> = exchangeResult.hide()

    val exchangeRateText: Observable<String> = Observable.combineLatest(
        selectedFromCurrency,
        selectedToCurrency,
        exchangeRateRepository.getExchangeRates(list)
    ) { from, to, rates ->
        "1 $from = ${rates[to] ?: 1.0} $to"
    }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    val convertedAmount: Observable<Double> = Observable.combineLatest(
        enteredAmount,
        selectedFromCurrency,
        selectedToCurrency,
        exchangeRateRepository.getExchangeRates(list)
    ) { amount, from, to, rates ->
        val rate = rates[to] ?: 1.0
        (amount * rate).roundToInt().toDouble()
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
        disposables.add(
            Single.zip(
                balanceRepository.getBalances().subscribeOn(Schedulers.io()),
                exchangeRateRepository.getExchangeRates(list).firstOrError().subscribeOn(Schedulers.io()),
                BiFunction { balances: Map<String, Double>, rates: Map<String, Double> ->
                    val fromCurrency = selectedFromCurrency.blockingFirst()
                    val toCurrency = selectedToCurrency.blockingFirst()
                    val amount = enteredAmount.blockingFirst()
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
                .subscribe({ message -> exchangeResult.onNext(message) }, Throwable::printStackTrace)
        )
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }
}
