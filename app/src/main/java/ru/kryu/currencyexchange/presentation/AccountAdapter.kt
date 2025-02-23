package ru.kryu.currencyexchange.presentation

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.kryu.currencyexchange.databinding.ItemAccountBinding

class AccountAdapter(
    private val isFromAccount: Boolean,
    private val onAmountEntered: ((String, Double) -> Unit)? = null
) : ListAdapter<Pair<String, Double>, AccountAdapter.AccountViewHolder>(AccountDiffCallback()) {

    private var convertedAmount: Double = 0.0
    private var enteredAmounts: Map<String, Double> = emptyMap()

    fun updateConversionRate(newConvertedAmount: Double) {
        convertedAmount = newConvertedAmount
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    fun updateEnteredAmounts(newEnteredAmounts: Map<String, Double>) {
        enteredAmounts = newEnteredAmounts
        Handler(Looper.getMainLooper()).post {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = ItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val (currency, balance) = getItem(position)
        holder.bind(currency, balance, enteredAmounts[currency] ?: 0.0)
    }

    inner class AccountViewHolder(private val binding: ItemAccountBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(currency: String, balance: Double, enteredAmount: Double) {
            binding.tvCurrency.text = currency
            binding.tvBalance.text = "Баланс: %.2f".format(balance)

            if (isFromAccount) {
                binding.etAmount.setText(enteredAmount.toString())
                binding.etAmount.doAfterTextChanged { Unit }
                binding.etAmount.doAfterTextChanged { text ->
                    val amount = text.toString().toDoubleOrNull() ?: 0.0
                    onAmountEntered?.invoke(currency, amount)
                }
            } else {
                binding.etAmount.isEnabled = false
                binding.tvBalance.text = "Баланс: %.2f (+%.2f)".format(balance, convertedAmount)
            }
        }
    }
}

class AccountDiffCallback : DiffUtil.ItemCallback<Pair<String, Double>>() {
    override fun areItemsTheSame(
        oldItem: Pair<String, Double>,
        newItem: Pair<String, Double>
    ): Boolean {
        return oldItem.first == newItem.first
    }

    override fun areContentsTheSame(
        oldItem: Pair<String, Double>,
        newItem: Pair<String, Double>
    ): Boolean {
        return oldItem == newItem
    }
}