package ru.kryu.currencyexchange.presentation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.kryu.currencyexchange.App
import ru.kryu.currencyexchange.databinding.ActivityMainBinding
import ru.kryu.currencyexchange.di.MainViewModelFactory
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: MainViewModelFactory
    private val viewModel: MainViewModel by viewModels { viewModelFactory }

    private lateinit var binding: ActivityMainBinding
    private lateinit var fromAccountAdapter: AccountAdapter
    private lateinit var toAccountAdapter: AccountAdapter
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as App).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupRecyclerViews()
        observeViewModel()
        setupExchangeButton()
    }

    private fun setupRecyclerViews() {
        fromAccountAdapter = AccountAdapter(true) { currency, amount ->
            viewModel.onAmountEntered(currency, amount)
        }
        toAccountAdapter = AccountAdapter(false)

        binding.recyclerAccountsFrom.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = fromAccountAdapter
        }
        val fromSnapHelper = PagerSnapHelper()
        fromSnapHelper.attachToRecyclerView(binding.recyclerAccountsFrom)


        binding.recyclerAccountsTo.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = toAccountAdapter
        }
        val toSnapHelper = PagerSnapHelper()
        toSnapHelper.attachToRecyclerView(binding.recyclerAccountsTo)
    }

    private fun observeViewModel() {
        compositeDisposable.addAll(
            viewModel.exchangeRateText
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ rateText ->
                    binding.exchangeRate.text = rateText
                }, Throwable::printStackTrace),

            viewModel.convertedAmount
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ convertedAmount ->
                    toAccountAdapter.updateConversionRate(convertedAmount)
                }, Throwable::printStackTrace),

            viewModel.balances
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ balances ->
                    val accountsList = balances.entries.toList()
                    fromAccountAdapter.submitList(accountsList.map { it.key to it.value })
                    toAccountAdapter.submitList(accountsList.map { it.key to it.value })
                }, Throwable::printStackTrace),

            viewModel.getExchangeResult()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ message ->
                    showExchangeResultDialog(message)
                }, Throwable::printStackTrace)
        )
    }

    private fun setupExchangeButton() {
        binding.btnExchange.setOnClickListener {
            viewModel.exchange()
        }
    }

    private fun showExchangeResultDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Обмен валют")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}
