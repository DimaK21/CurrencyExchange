package ru.kryu.currencyexchange.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import ru.kryu.currencyexchange.databinding.ItemAccountBinding

class AccountAdapter(
    private val isFromAccount: Boolean,
    private val onAmountEntered: ((String, Double) -> Unit)? = null,
) : RecyclerView.Adapter<AccountAdapter.AccountViewHolder>() {

    private var accounts: List<Pair<String, Double>> = listOf()
    private var convertedAmount: Double = 0.0

    fun submitList(newAccounts: List<Pair<String, Double>>) {
        this.accounts = newAccounts
        notifyDataSetChanged()
    }

    fun updateConversionRate(newConvertedAmount: Double) {
        this.convertedAmount = newConvertedAmount
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = ItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val (currency, balance) = accounts[position]
        holder.bind(currency, balance)
    }

    override fun getItemCount(): Int = accounts.size

    inner class AccountViewHolder(private val binding: ItemAccountBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(currency: String, balance: Double) {
            binding.tvCurrency.text = currency
            binding.tvBalance.text = "Баланс: %.2f".format(balance)

            binding.etAmount.visibility = if (isFromAccount) View.VISIBLE else View.GONE

            if (isFromAccount) {
                binding.etAmount.doAfterTextChanged { text ->
                    val amount = text.toString().toDoubleOrNull() ?: 0.0
                    onAmountEntered?.invoke(currency, amount)
                }
            } else {
                binding.tvBalance.text = "Баланс: %.2f (+%.2f)".format(balance, convertedAmount)
            }
        }
    }
}