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
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupRecyclerViews()
        subscribeViewModel()
        setupExchangeButton()
    }

    private fun setupRecyclerViews() {
        fromAccountAdapter = AccountAdapter(true) { currency, amount ->
            viewModel.onAmountEntered(amount)
        }
        toAccountAdapter = AccountAdapter(false)

        setupRecyclerView(binding.recyclerAccountsFrom, fromAccountAdapter) { position ->
            viewModel.updatePositionFrom(position)
        }
        setupRecyclerView(binding.recyclerAccountsTo, toAccountAdapter) { position ->
            viewModel.updatePositionTo(position)
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
                binding.exchangeRate.text = "1${
                    state.currencyList.getOrNull(state.positionFrom) ?: ""
                } = ${state.exchangeRate}${state.currencyList.getOrNull(state.positionTo) ?: ""}"
            }
        }
    }

    private fun setupExchangeButton() {
        viewModel.exchange()
    }

    private fun showExchangeResultDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Обмен валют")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
