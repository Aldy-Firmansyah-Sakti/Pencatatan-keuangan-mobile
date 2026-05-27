package com.example.sistem_keuanan

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import android.os.VibrationEffect
import android.os.Vibrator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator

class ReportActivity : AppCompatActivity() {
    private lateinit var transactionStore: TransactionStore
    private var selectedMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
    private var selectedYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report)

        transactionStore = TransactionStore(this)

        findViewById<MaterialButton>(R.id.add_transaction_button)?.apply {
            bringToFront()
            ViewCompat.setElevation(this, 12f)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.report_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupNavigation()
        setupFilters()
        updateSummary()
    }

    override fun onResume() {
        super.onResume()
        findViewById<MaterialButton>(R.id.add_transaction_button)?.apply {
            bringToFront()
            ViewCompat.setElevation(this, 12f)
        }
        updateSummary()
    }

    private fun updateSummary() {
        val all = transactionStore.getAllSync()
        val syms = java.text.DecimalFormatSymbols().apply {
            groupingSeparator = '.'; decimalSeparator = ','
        }
        val fmt = java.text.DecimalFormat("#,###", syms)

        val monthLabel = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale("id"))
            .format(java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.YEAR, selectedYear)
                set(java.util.Calendar.MONTH, selectedMonth)
                set(java.util.Calendar.DAY_OF_MONTH, 1)
            }.time)
        findViewById<android.widget.TextView>(R.id.tv_selected_month).text = monthLabel

        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id"))
        val thisMonthTx = all.filter {
            try {
                val d = sdf.parse(it.date) ?: return@filter false
                val c = java.util.Calendar.getInstance().apply { time = d }
                c.get(java.util.Calendar.MONTH) == selectedMonth &&
                c.get(java.util.Calendar.YEAR)  == selectedYear
            } catch (_: Exception) { false }
        }

        val income  = thisMonthTx.filter { !it.isExpense }.sumOf { it.amount }
        val expense = thisMonthTx.filter { it.isExpense }.sumOf { it.amount }

        val categoryTotals = listOfNotNull(
            if (income > 0) CategoryPieChartView.CategorySlice("Pemasukan", income, false) else null,
            if (expense > 0) CategoryPieChartView.CategorySlice("Pengeluaran", expense, true) else null
        )

        findViewById<CategoryPieChartView>(R.id.pie_chart_view).setChartData(categoryTotals)

        val topCategoriesOld = if (categoryTotals.isEmpty()) "Tidak ada data saat ini"
            else categoryTotals.joinToString(", ") { it.label }

        findViewById<android.widget.TextView>(R.id.tv_report_income).text  = "Rp ${fmt.format(income)}"
        findViewById<android.widget.TextView>(R.id.tv_report_expense).text = "Rp ${fmt.format(expense)}"

        // ── Performa Minggu Ini ──
        val weekPerformanceText = findViewById<android.widget.TextView>(R.id.tv_week_performance)
        val tvWeekCurrent = findViewById<android.widget.TextView>(R.id.tv_week_current)
        val tvWeekLast    = findViewById<android.widget.TextView>(R.id.tv_week_last)
        val progressWeek  = findViewById<android.widget.ProgressBar>(R.id.progress_week)

        val currentWeekRange  = getWeekRange(java.util.Calendar.getInstance())
        val previousWeekRange = getWeekRange(java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -7) })

        val currentWeekExpense = all
            .mapNotNull { t -> parseReportDate(t.date)?.let { t to it.time } }
            .filter { it.second in currentWeekRange.first.timeInMillis..currentWeekRange.second.timeInMillis && it.first.isExpense }
            .sumOf { it.first.amount }

        val lastWeekExpense = all
            .mapNotNull { t -> parseReportDate(t.date)?.let { t to it.time } }
            .filter { it.second in previousWeekRange.first.timeInMillis..previousWeekRange.second.timeInMillis && it.first.isExpense }
            .sumOf { it.first.amount }

        tvWeekCurrent.text = "Rp ${fmt.format(currentWeekExpense)}"
        tvWeekLast.text    = "Rp ${fmt.format(lastWeekExpense)}"

        val performanceText: String
        val performanceColor: Int
        when {
            currentWeekExpense == 0L && lastWeekExpense == 0L -> {
                performanceText  = "Tidak ada data"
                performanceColor = R.color.black
            }
            lastWeekExpense == 0L -> {
                performanceText  = "Baru mulai minggu ini"
                performanceColor = R.color.income_green
            }
            else -> {
                val diff = ((currentWeekExpense - lastWeekExpense).toDouble() / lastWeekExpense * 100).toInt()
                performanceText  = if (diff >= 0) "↑ +$diff% dari minggu lalu" else "↓ $diff% dari minggu lalu"
                performanceColor = if (diff <= 0) R.color.income_green else R.color.expense_red
            }
        }
        weekPerformanceText.text = performanceText
        weekPerformanceText.setTextColor(resources.getColor(performanceColor, null))

        val maxVal = maxOf(currentWeekExpense, lastWeekExpense, 1L)
        progressWeek.progress = ((currentWeekExpense.toDouble() / maxVal) * 100).toInt()

        // ── Kategori Teratas Pengeluaran ──
        val categoryContainer = findViewById<android.widget.LinearLayout>(R.id.tv_top_categories)
        categoryContainer.removeAllViews()

        val topCats = thisMonthTx
            .filter { it.isExpense }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { t -> t.amount } }
            .entries.sortedByDescending { it.value }.take(5)

        if (topCats.isEmpty()) {
            val tv = android.widget.TextView(this).apply {
                text = "Belum ada pengeluaran bulan ini"
                textSize = 12f
                setTextColor(0xFF888888.toInt())
            }
            categoryContainer.addView(tv)
        } else {
            val dp = resources.displayMetrics.density
            topCats.forEachIndexed { index, entry ->
                val row = android.widget.LinearLayout(this).apply {
                    orientation = android.widget.LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER_VERTICAL
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.topMargin = (6 * dp).toInt() }
                }
                val tvNum = android.widget.TextView(this).apply {
                    text = "${index + 1}."
                    textSize = 13f
                    setTextColor(resources.getColor(R.color.primary_green, null))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    minWidth = (28 * dp).toInt()
                }
                val tvName = android.widget.TextView(this).apply {
                    text = entry.key
                    textSize = 13f
                    setTextColor(resources.getColor(R.color.black, null))
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                    )
                }
                val tvAmt = android.widget.TextView(this).apply {
                    text = "Rp ${fmt.format(entry.value)}"
                    textSize = 13f
                    setTextColor(resources.getColor(R.color.expense_red, null))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                row.addView(tvNum); row.addView(tvName); row.addView(tvAmt)
                categoryContainer.addView(row)
            }
        }
    }

    private fun parseReportDate(dateString: String): java.util.Date? {
        return try {
            java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id")).parse(dateString)
        } catch (_: Exception) {
            null
        }
    }

    private fun getWeekRange(calendar: java.util.Calendar): Pair<java.util.Calendar, java.util.Calendar> {
        val start = calendar.clone() as java.util.Calendar
        start.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        start.set(java.util.Calendar.HOUR_OF_DAY, 0)
        start.set(java.util.Calendar.MINUTE, 0)
        start.set(java.util.Calendar.SECOND, 0)
        start.set(java.util.Calendar.MILLISECOND, 0)

        val end = start.clone() as java.util.Calendar
        end.add(java.util.Calendar.DAY_OF_YEAR, 6)
        end.set(java.util.Calendar.HOUR_OF_DAY, 23)
        end.set(java.util.Calendar.MINUTE, 59)
        end.set(java.util.Calendar.SECOND, 59)
        end.set(java.util.Calendar.MILLISECOND, 999)

        return Pair(start, end)
    }

    private fun setupFilters() {
        findViewById<MaterialButton>(R.id.month_picker_button).setOnClickListener {
            val calendar = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.YEAR, selectedYear)
                set(java.util.Calendar.MONTH, selectedMonth)
            }
            android.app.DatePickerDialog(this, { _, year, month, _ ->
                selectedYear = year
                selectedMonth = month
                updateSummary()
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupNavigation() {
        try {
            val vibrator = getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
            val bottomNav = findViewById<LinearLayout>(R.id.bottom_nav)
            
            // Set active state for current page (Reports - index 3)
            setNavItemActive(bottomNav, 3)
            
            // Home navigation
            bottomNav.getChildAt(0).setOnClickListener { view ->
                doVibrate(vibrator, 20)
                playButtonClickAnimation(view)
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            
            // Transactions navigation
            bottomNav.getChildAt(1).setOnClickListener { view ->
                doVibrate(vibrator, 20)
                playButtonClickAnimation(view)
                startActivity(Intent(this, TransactionActivity::class.java))
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            
            // Reports (current page)
            bottomNav.getChildAt(3).setOnClickListener { view ->
                doVibrate(vibrator, 20)
                playButtonClickAnimation(view)
                // Already on reports page
            }
            
            // Profile navigation
            bottomNav.getChildAt(4).setOnClickListener { view ->
                doVibrate(vibrator, 20)
                playButtonClickAnimation(view)
                startActivity(Intent(this, ProfileActivity::class.java))
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }

            // FAB button
            findViewById<MaterialButton>(R.id.add_transaction_button).setOnClickListener { view ->
                doVibrate(vibrator, 30)
                playButtonClickAnimation(view)
                startActivity(Intent(this, AddTransactionActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setNavItemActive(bottomNav: LinearLayout, activeIndex: Int) {
        for (i in 0 until bottomNav.childCount) {
            val item = bottomNav.getChildAt(i) as? LinearLayout ?: continue
            val isActive = i == activeIndex
            item.isSelected = isActive
            for (j in 0 until item.childCount) {
                item.getChildAt(j).isSelected = isActive
            }
        }
    }

    private fun playButtonClickAnimation(view: android.view.View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.92f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.92f, 1f)
        val alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.7f, 1f)
        val rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, -3f, 0f)

        scaleX.duration = 250
        scaleY.duration = 250
        alpha.duration = 250
        rotation.duration = 250

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY, alpha, rotation)
        animatorSet.start()
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
        } catch (e: Exception) {
            // Vibrator failed safely, continue without vibration
        }
    }
}