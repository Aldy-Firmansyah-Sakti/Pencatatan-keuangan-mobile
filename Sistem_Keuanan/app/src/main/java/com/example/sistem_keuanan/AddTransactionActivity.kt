package com.example.sistem_keuanan

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar
import android.os.VibrationEffect
import android.os.Vibrator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {
    private var selectedCategory = "Makanan"
    private var isExpense = true
    private var editingTransaction: Transaction? = null
    private lateinit var transactionStore: TransactionStore

    // All category buttons tracked for highlight reset
    private val categoryButtonIds = listOf(
        R.id.button_food, R.id.button_shopping, R.id.button_transport,
        R.id.button_house, R.id.button_health, R.id.button_entertainment,
        R.id.button_education, R.id.button_bills, R.id.button_custom
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_add_transaction)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check if editing existing transaction
        editingTransaction = intent.getSerializableExtra("transaction") as? Transaction

        transactionStore = TransactionStore(this)

        try {
            setupUI()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupUI() {
        val vibrator = getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
        val dateInput = findViewById<TextInputEditText>(R.id.date_input)
        val noteInput = findViewById<TextInputEditText>(R.id.note_input)
        val nominalInput = findViewById<TextInputEditText>(R.id.nominal_input)

        // Title
        val editing = editingTransaction
        if (editing != null) {
            findViewById<TextView>(R.id.tv_title).text = "Edit Transaksi"
        }

        // Expense/Income Toggle
        val expenseBtn = findViewById<TextView>(R.id.button_expense)
        val incomeBtn = findViewById<TextView>(R.id.button_income)

        fun selectExpense() {
            isExpense = true
            expenseBtn.setBackgroundResource(R.drawable.toggle_item_selected_expense)
            expenseBtn.setTextColor(resources.getColor(R.color.expense_red, null))
            expenseBtn.elevation = 6f * resources.displayMetrics.density
            incomeBtn.setBackgroundResource(R.drawable.toggle_item_unselected)
            incomeBtn.setTextColor(resources.getColor(android.R.color.darker_gray, null))
            incomeBtn.elevation = 0f
            animateToggleSelect(expenseBtn)
            animateToggleDeselect(incomeBtn)
            doVibrate(vibrator, 30)
        }

        fun selectIncome() {
            isExpense = false
            incomeBtn.setBackgroundResource(R.drawable.toggle_item_selected_income)
            incomeBtn.setTextColor(resources.getColor(R.color.income_green, null))
            incomeBtn.elevation = 6f * resources.displayMetrics.density
            expenseBtn.setBackgroundResource(R.drawable.toggle_item_unselected)
            expenseBtn.setTextColor(resources.getColor(android.R.color.darker_gray, null))
            expenseBtn.elevation = 0f
            animateToggleSelect(incomeBtn)
            animateToggleDeselect(expenseBtn)
            doVibrate(vibrator, 30)
        }

        if (editing != null && !editing.isExpense) selectIncome() else selectExpense()

        expenseBtn.setOnClickListener { selectExpense() }
        incomeBtn.setOnClickListener { selectIncome() }

        // Date Picker — open calendar on click
        val openDatePicker = {
            val cal = Calendar.getInstance()
            // Pre-fill from existing date if editing
            editing?.date?.let { d ->
                try {
                    val parsed = SimpleDateFormat("dd MMM yyyy", Locale("id")).parse(d)
                    if (parsed != null) cal.time = parsed
                } catch (_: Exception) {}
            }
            DatePickerDialog(this, { _, y, m, d ->
                val formatted = SimpleDateFormat("dd MMM yyyy", Locale("id"))
                    .format(Calendar.getInstance().apply { set(y, m, d) }.time)
                dateInput.setText(formatted)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        dateInput.setOnClickListener { openDatePicker() }
        dateInput.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) openDatePicker() }
        findViewById<android.view.ViewGroup>(R.id.date_layout).setOnClickListener { openDatePicker() }

        // Category buttons
        setupCategoryButtons()

        // Pre-fill if editing
        if (editing != null) {
            val symbols = java.text.DecimalFormatSymbols().apply {
                groupingSeparator = '.'; decimalSeparator = ','
            }
            val fmt = java.text.DecimalFormat("#,###", symbols)
            nominalInput.setText(fmt.format(editing.amount))
            nominalInput.setSelection(nominalInput.text?.length ?: 0)
            dateInput.setText(editing.date)
            noteInput.setText(editing.note)
            selectedCategory = editing.category
            highlightCategoryButton(editing.category)
        }

        // Nominal formatting
        nominalInput.addTextChangedListener(object : android.text.TextWatcher {
            private var isEditing = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (isEditing || s == null) return
                isEditing = true
                try {
                    val raw = s.toString().replace("[^\\d]".toRegex(), "")
                    if (raw.isNotEmpty()) {
                        val number = raw.toLongOrNull() ?: 0L
                        val syms = DecimalFormatSymbols().apply {
                            groupingSeparator = '.'
                            decimalSeparator = ','
                        }
                        val formatter = (java.text.NumberFormat.getInstance(Locale("id")) as DecimalFormat).apply {
                            decimalFormatSymbols = syms
                            isGroupingUsed = true
                            groupingSize = 3
                            maximumFractionDigits = 0
                        }
                        val formatted = formatter.format(number)
                        if (formatted != s.toString()) {
                            s.replace(0, s.length, formatted)
                            nominalInput.setSelection(formatted.length.coerceAtMost(nominalInput.text?.length ?: formatted.length))
                        }
                    } else if (s.isNotEmpty()) {
                        s.clear()
                    }
                } catch (e: Exception) {
                    // Fallback when formatting fails; keep plain digits only
                    val digitsOnly = s.toString().replace("[^\\d]".toRegex(), "")
                    if (digitsOnly != s.toString()) {
                        s.replace(0, s.length, digitsOnly)
                    }
                } finally {
                    isEditing = false
                }
            }
        })

        // Save Button
        findViewById<MaterialButton>(R.id.save_button).setOnClickListener {
            val nominalStr = nominalInput.text.toString().replace(".", "").replace(",", "").trim()
            val amount = nominalStr.toLongOrNull() ?: 0L
            val dateText = dateInput.text.toString().ifBlank {
                SimpleDateFormat("dd MMM yyyy", Locale("id")).format(java.util.Date())
            }
            val noteText = noteInput.text.toString()

            if (amount > 0) {
                val transaction = Transaction(
                    id = editing?.id ?: System.currentTimeMillis(),
                    amount = amount,
                    isExpense = isExpense,
                    category = selectedCategory,
                    note = noteText,
                    date = dateText
                )
                if (editing != null) {
                    transactionStore.deleteSync(editing.id)
                }
                transactionStore.addSync(transaction)
            }
            setResult(RESULT_OK)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Cancel Button
        findViewById<MaterialButton>(R.id.cancel_button).setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Back button di header
        findViewById<android.widget.ImageView>(R.id.btn_back).setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun setupCategoryButtons() {
        val presets = listOf(
            R.id.button_food to "Makanan",
            R.id.button_shopping to "Belanja",
            R.id.button_transport to "Transport",
            R.id.button_house to "Rumah",
            R.id.button_health to "Kesehatan",
            R.id.button_entertainment to "Hiburan",
            R.id.button_education to "Pendidikan",
            R.id.button_bills to "Tagihan"
        )

        for ((id, name) in presets) {
            findViewById<MaterialButton>(id).setOnClickListener {
                selectedCategory = name
                highlightCategoryButton(name)
            }
        }

        // Custom category — show input dialog
        findViewById<MaterialButton>(R.id.button_custom).setOnClickListener {
            val input = EditText(this).apply {
                hint = "Nama kategori"
                setSingleLine()
            }
            AlertDialog.Builder(this)
                .setTitle("Kategori Kustom")
                .setView(input)
                .setPositiveButton("OK") { _, _ ->
                    val custom = input.text.toString().trim()
                    if (custom.isNotEmpty()) {
                        selectedCategory = custom
                        // Show the custom name on the button
                        findViewById<MaterialButton>(R.id.button_custom).text = custom
                        highlightCategoryButton(custom)
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun highlightCategoryButton(categoryName: String) {
        val presetMap = mapOf(
            "Makanan" to R.id.button_food, "Belanja" to R.id.button_shopping,
            "Transport" to R.id.button_transport, "Rumah" to R.id.button_house,
            "Kesehatan" to R.id.button_health, "Hiburan" to R.id.button_entertainment,
            "Pendidikan" to R.id.button_education, "Tagihan" to R.id.button_bills
        )
        // Reset all
        for (id in categoryButtonIds) {
            val btn = findViewById<MaterialButton>(id)
            btn.setBackgroundColor(resources.getColor(R.color.white, null))
            btn.setTextColor(resources.getColor(R.color.black, null))
        }
        // Highlight matched
        val targetId = presetMap[categoryName] ?: R.id.button_custom
        val btn = findViewById<MaterialButton>(targetId)
        btn.setBackgroundColor(resources.getColor(R.color.primary_green, null))
        btn.setTextColor(resources.getColor(R.color.white, null))
    }

    private fun animateToggleSelect(view: android.view.View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.93f, 1.04f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.93f, 1.04f, 1f)
        scaleX.duration = 220; scaleY.duration = 220
        AnimatorSet().apply { playTogether(scaleX, scaleY); start() }
    }

    private fun animateToggleDeselect(view: android.view.View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f)
        scaleX.duration = 180; scaleY.duration = 180
        AnimatorSet().apply { playTogether(scaleX, scaleY); start() }
    }

    private fun doVibrate(vibrator: Vibrator?, duration: Long) {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(duration)
                }
            }
        } catch (_: Exception) {}
    }
}
