package com.example.sistem_keuanan

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class TransactionDetailActivity : AppCompatActivity() {

    private lateinit var transactionStore: TransactionStore

    private val editLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaction_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val transaction = intent.getSerializableExtra("transaction") as? Transaction ?: run {
            finish(); return
        }

        transactionStore = TransactionStore(this)

        bindData(transaction)

        findViewById<android.widget.ImageView>(R.id.btn_back).setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btn_save).setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("transaction", transaction)
            editLauncher.launch(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        findViewById<MaterialButton>(R.id.btn_delete).setOnClickListener {
            transactionStore.deleteSync(transaction.id)
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun bindData(t: Transaction) {
        val symbols = DecimalFormatSymbols().apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val fmt = DecimalFormat("#,###", symbols)
        val formattedAmount = "Rp ${fmt.format(t.amount)}"

        val tvAmount = findViewById<TextView>(R.id.tv_amount)
        if (t.isExpense) {
            tvAmount.text = "-$formattedAmount ↓"
            tvAmount.setBackgroundResource(R.drawable.amount_card_expense)
        } else {
            tvAmount.text = "+$formattedAmount ↑"
            tvAmount.setBackgroundResource(R.drawable.amount_card_income)
        }

        val categoryEmoji = mapOf(
            "Makanan" to "🍔", "Belanja" to "🛍", "Transport" to "🚗",
            "Rumah" to "🏠", "Kesehatan" to "💊", "Hiburan" to "🎮",
            "Pendidikan" to "📚", "Tagihan" to "📄"
        )

        findViewById<TextView>(R.id.tv_category_icon).text = categoryEmoji[t.category] ?: "✏️"
        findViewById<TextView>(R.id.tv_category).text = t.category
        // Deskripsi = note yang diisi user
        val desc = t.note.ifBlank { "-" }
        findViewById<TextView>(R.id.tv_description).text = desc
        findViewById<TextView>(R.id.tv_date).text = t.date
        findViewById<TextView>(R.id.tv_note).text = desc

        val typeIcon = if (t.isExpense) "💸" else "💰"
        val typeLabel = if (t.isExpense) "Pengeluaran" else "Pemasukan"
        findViewById<TextView>(R.id.tv_type_icon).text = typeIcon
        findViewById<TextView>(R.id.tv_type).text = typeLabel
    }
}
