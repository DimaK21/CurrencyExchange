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
    private var selectedCurrency: String? = null
    private var convertedAmount: Double = 0.0

    fun submitList(newAccounts: List<Pair<String, Double>>, selectedCurrency: String?, convertedAmount: Double = 0.0) {
        this.accounts = newAccounts
        this.selectedCurrency = selectedCurrency
        this.convertedAmount = convertedAmount
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = ItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val (currency, balance) = accounts[position]
        holder.bind(currency, balance, currency == selectedCurrency)
    }

    override fun getItemCount(): Int = accounts.size

    inner class AccountViewHolder(private val binding: ItemAccountBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(currency: String, balance: Double, isSelected: Boolean) {
            binding.tvCurrency.text = currency
            binding.tvBalance.text = "Баланс: %.2f".format(balance)

            if (isFromAccount) {
                binding.etAmount.visibility = View.VISIBLE
                binding.etAmount.doAfterTextChanged { text ->
                    val amount = text.toString().toDoubleOrNull() ?: 0.0
                    onAmountEntered?.invoke(currency, amount)
                }
            } else {
                binding.etAmount.visibility = View.GONE
                if (isSelected) {
                    binding.tvBalance.text = "Баланс: %.2f (+%.2f)".format(balance, convertedAmount)
                }
            }
        }
    }
}
