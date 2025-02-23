package ru.kryu.currencyexchange.presentation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
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

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as App).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerViews()
        subscribeViewModel()
        setupExchangeButton()
    }

    private fun setupRecyclerViews() {
        fromAccountAdapter = AccountAdapter(true, binding.recyclerAccountsFrom) { currency, amount ->
            viewModel.onAmountEntered(currency, amount)
        }
        toAccountAdapter = AccountAdapter(false, binding.recyclerAccountsFrom)

        setupRecyclerView(binding.recyclerAccountsFrom, fromAccountAdapter) { position ->
            viewModel.updatePositionFrom(position)
        }
        setupRecyclerView(binding.recyclerAccountsTo, toAccountAdapter) { position ->
            viewModel.updatePositionTo(position)
        }

        binding.recyclerAccountsFrom.setOnTouchListener { v, event ->
            v.performClick()
            v.clearFocus()
            currentFocus?.clearFocus()
            false
        }
    }

    private fun setupRecyclerView(
        recyclerView: RecyclerView,
        adapter: AccountAdapter,
        onScrolledToPosition: (Int) -> Unit
    ) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = adapter
        }
        PagerSnapHelper().attachToRecyclerView(recyclerView)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                if (position != RecyclerView.NO_POSITION) {
                    onScrolledToPosition(position)
                }
            }
        })
    }

    private fun subscribeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                fromAccountAdapter.submitList(state.balances.entries.map { it.key.name to it.value })
                toAccountAdapter.submitList(state.balances.entries.map { it.key.name to it.value })

                fromAccountAdapter.updateConversionRate(state.convertedAmount)
                toAccountAdapter.updateConversionRate(state.convertedAmount)

                fromAccountAdapter.updateEnteredAmounts(state.enteredAmounts)

                binding.exchangeRate.text = "1${
                    state.currencyList.getOrNull(state.positionFrom) ?: ""
                } = ${"%.2f".format(state.exchangeRate)}${state.currencyList.getOrNull(state.positionTo) ?: ""}"

                if (state.message != null) {
                    showExchangeResultDialog(state.message)
                    viewModel.clearErrorMessage()
                }
            }
        }
    }

    private fun setupExchangeButton() {
        binding.btnExchange.setOnClickListener {
            viewModel.exchange()

            binding.recyclerAccountsFrom.clearFocus()
            binding.recyclerAccountsTo.clearFocus()
            currentFocus?.clearFocus()
        }
    }

    private fun showExchangeResultDialog(message: String) {
        if (message.isBlank()) return
        AlertDialog.Builder(this)
            .setTitle("Обмен валют")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
