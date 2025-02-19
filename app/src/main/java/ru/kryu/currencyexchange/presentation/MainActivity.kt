package ru.kryu.currencyexchange.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ru.kryu.currencyexchange.App
import ru.kryu.currencyexchange.R
import ru.kryu.currencyexchange.databinding.ActivityMainBinding
import ru.kryu.currencyexchange.di.MainViewModelFactory
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject lateinit var viewModelFactory: MainViewModelFactory
    private val viewModel: MainViewModel by viewModels { viewModelFactory }

    private lateinit var binding: ActivityMainBinding
    private lateinit var fromAccountAdapter: AccountAdapter
    private lateinit var toAccountAdapter: AccountAdapter

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
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = fromAccountAdapter
        }

        binding.recyclerAccountsTo.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = toAccountAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.exchangeRateText
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { rateText ->
                binding.exchangeRate.text = rateText
            }

        viewModel.convertedAmount
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { convertedAmount ->
                toAccountAdapter.updateConversionRate(convertedAmount)
            }

        viewModel.balances
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { balances ->
                val accountsList = balances.entries.toList()
                fromAccountAdapter.submitList(accountsList)
                toAccountAdapter.submitList(accountsList)
            }

        viewModel.getExchangeResult()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { message ->
                showExchangeResultDialog(message)
            }
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
}
}