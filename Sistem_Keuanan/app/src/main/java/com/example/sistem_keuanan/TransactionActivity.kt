package com.example.sistem_keuanan

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import android.os.VibrationEffect
import android.os.Vibrator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransactionActivity : AppCompatActivity() {

    private var activeFilter = 0       // 0=semua, 1=pengeluaran, 2=pemasukan
    private var filterDate: String? = null

    private lateinit var adapter: TransactionAdapter

    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { if (it.resultCode == RESULT_OK) loadTransactions() }

    private val addLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { if (it.resultCode == RESULT_OK) loadTransactions() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaction)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.transaction_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupFilters()
        setupNavigation()
        loadTransactions()
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(emptyList()) { transaction ->
            val intent = Intent(this, TransactionDetailActivity::class.java)
            intent.putExtra("transaction", transaction)
            detailLauncher.launch(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        findViewById<RecyclerView>(R.id.recycler_transactions).apply {
            layoutManager = LinearLayoutManager(this@TransactionActivity)
            adapter = this@TransactionActivity.adapter
            setHasFixedSize(false)
        }
    }

    private fun setupFilters() {
        val chipAll     = findViewById<TextView>(R.id.filter_all)
        val chipExpense = findViewById<TextView>(R.id.filter_expense)
        val chipIncome  = findViewById<TextView>(R.id.filter_income)
        val chipDate    = findViewById<TextView>(R.id.filter_date)

        fun setTypeActive(index: Int) {
            activeFilter = index
            listOf(chipAll, chipExpense, chipIncome).forEachIndexed { i, chip ->
                if (i == index) {
                    chip.setBackgroundResource(R.drawable.filter_chip_active)
                    chip.setTextColor(resources.getColor(R.color.white, null))
                    chip.setTypeface(null, android.graphics.Typeface.BOLD)
                } else {
                    chip.setBackgroundResource(R.drawable.filter_chip_inactive)
                    chip.setTextColor(resources.getColor(R.color.black, null))
                    chip.setTypeface(null, android.graphics.Typeface.NORMAL)
                }
            }
            loadTransactions()
        }

        chipAll.setOnClickListener     { setTypeActive(0) }
        chipExpense.setOnClickListener { setTypeActive(1) }
        chipIncome.setOnClickListener  { setTypeActive(2) }

        chipDate.setOnClickListener {
            if (filterDate != null) {
                filterDate = null
                chipDate.text = "📅 Tanggal"
                chipDate.setBackgroundResource(R.drawable.filter_chip_inactive)
                chipDate.setTextColor(resources.getColor(R.color.black, null))
                chipDate.setTypeface(null, android.graphics.Typeface.NORMAL)
                loadTransactions()
            } else {
                val cal = Calendar.getInstance()
                DatePickerDialog(this, { _, y, m, d ->
                    val picked = SimpleDateFormat("dd MMM yyyy", Locale("id"))
                        .format(Calendar.getInstance().apply { set(y, m, d) }.time)
                    filterDate = picked
                    chipDate.text = "✕ $picked"
                    chipDate.setBackgroundResource(R.drawable.filter_chip_active)
                    chipDate.setTextColor(resources.getColor(R.color.white, null))
                    chipDate.setTypeface(null, android.graphics.Typeface.BOLD)
                    loadTransactions()
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            }
        }

        chipDate.setOnLongClickListener {
            filterDate = null
            chipDate.text = "📅 Tanggal"
            chipDate.setBackgroundResource(R.drawable.filter_chip_inactive)
            chipDate.setTextColor(resources.getColor(R.color.black, null))
            chipDate.setTypeface(null, android.graphics.Typeface.NORMAL)
            loadTransactions()
            true
        }
    }

    private fun loadTransactions() {
        val all = TransactionStore.getAll(this)

        val byType = when (activeFilter) {
            1 -> all.filter { it.isExpense }
            2 -> all.filter { !it.isExpense }
            else -> all
        }
        val filtered = filterDate?.let { fd -> byType.filter { it.date == fd } } ?: byType
        val sorted   = filtered.sortedByDescending { it.id }

        val tvEmpty = findViewById<TextView>(R.id.tv_empty)
        val recycler = findViewById<RecyclerView>(R.id.recycler_transactions)

        if (sorted.isEmpty()) {
            recycler.visibility = View.GONE
            tvEmpty.visibility  = View.VISIBLE
            tvEmpty.text = when {
                filterDate != null -> "Tidak ada transaksi pada $filterDate."
                activeFilter == 1  -> "Tidak ada transaksi pengeluaran."
                activeFilter == 2  -> "Tidak ada transaksi pemasukan."
                else               -> "Belum ada transaksi.\nTambahkan transaksi pertamamu!"
            }
            return
        }

        recycler.visibility = View.VISIBLE
        tvEmpty.visibility  = View.GONE

        // Bangun list item dengan date separator
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id"))
        val grouped = sorted.groupBy { it.date }
        val sortedDates = grouped.keys.sortedByDescending {
            try { sdf.parse(it)?.time ?: 0L } catch (_: Exception) { 0L }
        }

        val items = mutableListOf<TransactionAdapter.ListItem>()
        for (dateKey in sortedDates) {
            items.add(TransactionAdapter.ListItem.DateHeader(formatDateLabel(dateKey)))
            grouped[dateKey]!!.forEach {
                items.add(TransactionAdapter.ListItem.TransactionItem(it))
            }
        }

        adapter.updateItems(items)
    }

    private fun formatDateLabel(dateStr: String): String {
        return try {
            val sdf  = SimpleDateFormat("dd MMM yyyy", Locale("id"))
            val date = sdf.parse(dateStr) ?: return dateStr
            fun cal(offset: Int) = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, offset)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
            }.time
            val dateMid = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
            }.time
            when (dateMid) {
                cal(0)  -> "Hari ini · $dateStr"
                cal(-1) -> "Kemarin · $dateStr"
                else    -> dateStr
            }
        } catch (_: Exception) { dateStr }
    }

    private fun setupNavigation() {
        try {
            val vibrator = getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
            val bottomNav = findViewById<LinearLayout>(R.id.bottom_nav)

            setNavItemActive(bottomNav, 1)

            bottomNav.getChildAt(0).setOnClickListener { view ->
                doVibrate(vibrator, 20); playButtonClickAnimation(view)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            bottomNav.getChildAt(1).setOnClickListener { view ->
                doVibrate(vibrator, 20); playButtonClickAnimation(view)
            }
            bottomNav.getChildAt(2).setOnClickListener { view ->
                doVibrate(vibrator, 20); playButtonClickAnimation(view)
                startActivity(Intent(this, ReportActivity::class.java))
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            bottomNav.getChildAt(3).setOnClickListener { view ->
                doVibrate(vibrator, 20); playButtonClickAnimation(view)
                startActivity(Intent(this, ProfileActivity::class.java))
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }

            findViewById<MaterialButton>(R.id.add_transaction_button).setOnClickListener { view ->
                doVibrate(vibrator, 30); playButtonClickAnimation(view)
                addLauncher.launch(Intent(this, AddTransactionActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun setNavItemActive(bottomNav: LinearLayout, activeIndex: Int) {
        for (i in 0 until bottomNav.childCount) {
            val item = bottomNav.getChildAt(i) as? LinearLayout ?: continue
            val isActive = i == activeIndex
            item.isSelected = isActive
            for (j in 0 until item.childCount) item.getChildAt(j).isSelected = isActive
        }
    }

    private fun playButtonClickAnimation(view: View) {
        val scaleX   = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.92f, 1f)
        val scaleY   = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.92f, 1f)
        val alpha    = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.7f, 1f)
        val rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, -3f, 0f)
        scaleX.duration = 250; scaleY.duration = 250
        alpha.duration  = 250; rotation.duration = 250
        AnimatorSet().apply { playTogether(scaleX, scaleY, alpha, rotation); start() }
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
