package com.example.sistem_keuanan

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class TransactionAdapter(
    private var items: List<ListItem>,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_DATE = 0
        const val TYPE_TRANSACTION = 1
    }

    // Sealed class untuk dua tipe item di list
    sealed class ListItem {
        data class DateHeader(val label: String) : ListItem()
        data class TransactionItem(val transaction: Transaction) : ListItem()
    }

    fun updateItems(newItems: List<ListItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is ListItem.DateHeader      -> TYPE_DATE
        is ListItem.TransactionItem -> TYPE_TRANSACTION
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_DATE -> DateViewHolder(
                inflater.inflate(R.layout.item_date_separator, parent, false)
            )
            else -> TransactionViewHolder(
                inflater.inflate(R.layout.item_transaction, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.DateHeader      -> (holder as DateViewHolder).bind(item)
            is ListItem.TransactionItem -> (holder as TransactionViewHolder).bind(item.transaction)
        }
    }

    // ── ViewHolder: Date separator ──
    inner class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvLabel: TextView = view.findViewById(R.id.tv_date_label)
        fun bind(item: ListItem.DateHeader) {
            tvLabel.text = item.label
        }
    }

    // ── ViewHolder: Transaction item ──
    inner class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val card: MaterialCardView = view.findViewById(R.id.card_transaction)
        private val dot: View             = view.findViewById(R.id.dot_color)
        private val tvCategory: TextView  = view.findViewById(R.id.tv_item_category)
        private val tvNote: TextView      = view.findViewById(R.id.tv_item_note)
        private val tvAmount: TextView    = view.findViewById(R.id.tv_item_amount)

        private val fmt: DecimalFormat by lazy {
            val syms = DecimalFormatSymbols().apply {
                groupingSeparator = '.'; decimalSeparator = ','
            }
            DecimalFormat("#,###", syms)
        }

        fun bind(t: Transaction) {
            val ctx = itemView.context

            tvCategory.text = t.category
            tvNote.text = t.note.ifBlank { if (t.isExpense) "Pengeluaran" else "Pemasukan" }

            val colorRes = if (t.isExpense) R.color.expense_red else R.color.income_green
            val color = ctx.resources.getColor(colorRes, null)

            dot.backgroundTintList = ColorStateList.valueOf(color)

            val prefix = if (t.isExpense) "-Rp " else "+Rp "
            tvAmount.text = "$prefix${fmt.format(t.amount)}"
            tvAmount.setTextColor(color)

            card.setOnClickListener { onItemClick(t) }
        }
    }
}
