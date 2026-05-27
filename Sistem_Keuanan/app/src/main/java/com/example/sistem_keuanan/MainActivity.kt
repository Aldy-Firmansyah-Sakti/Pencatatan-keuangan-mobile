package com.example.sistem_keuanan

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import android.os.VibrationEffect
import android.os.Vibrator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class MainActivity : AppCompatActivity() {

    private lateinit var transactionStore: TransactionStore

    private val detailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { loadRecentTransactions() }

    private val addLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { loadRecentTransactions() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        transactionStore = TransactionStore(this)

        findViewById<MaterialButton>(R.id.add_transaction_button)?.apply {
            bringToFront()
            ViewCompat.setElevation(this, 12f)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupNavigation()
        updateSummary()
        loadRecentTransactions()
    }

    override fun onResume() {
        super.onResume()
        findViewById<MaterialButton>(R.id.add_transaction_button)?.apply {
            bringToFront()
            ViewCompat.setElevation(this, 12f)
        }
        updateSummary()
        loadRecentTransactions()
    }

    private fun updateSummary() {
        val all = transactionStore.getAllSync()
        val fmt = buildFormatter()

        val cal = java.util.Calendar.getInstance()
        val thisMonth = cal.get(java.util.Calendar.MONTH)
        val thisYear  = cal.get(java.util.Calendar.YEAR)
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id"))

        val thisMonthTx = all.filter {
            try {
                val d = sdf.parse(it.date) ?: return@filter false
                val c = java.util.Calendar.getInstance().apply { time = d }
                c.get(java.util.Calendar.MONTH) == thisMonth &&
                c.get(java.util.Calendar.YEAR)  == thisYear
            } catch (_: Exception) { false }
        }

        val totalIncome  = thisMonthTx.filter { !it.isExpense }.sumOf { it.amount }
        val totalExpense = thisMonthTx.filter {  it.isExpense }.sumOf { it.amount }
        val balance      = all.filter { !it.isExpense }.sumOf { it.amount } -
                           all.filter {  it.isExpense }.sumOf { it.amount }

        try {
            findViewById<android.widget.TextView>(R.id.tv_total_balance)?.text = "Rp ${fmt.format(balance)}"
            findViewById<android.widget.TextView>(R.id.tv_home_income)?.text   = "Rp ${fmt.format(totalIncome)}"
            findViewById<android.widget.TextView>(R.id.tv_home_expense)?.text  = "Rp ${fmt.format(totalExpense)}"
        } catch (_: Exception) {}
    }

    private fun buildFormatter(): java.text.DecimalFormat {
        val syms = java.text.DecimalFormatSymbols().apply {
            groupingSeparator = '.'; decimalSeparator = ','
        }
        return java.text.DecimalFormat("#,###", syms)
    }

    private fun loadRecentTransactions() {
        val container = findViewById<LinearLayout>(R.id.recent_transaction_list) ?: return
        container.removeAllViews()

        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id"))
        val todayStr = sdf.format(java.util.Date())

        // Semua transaksi hari ini, terbaru di atas
        val list = transactionStore.getAllSync()
            .filter { it.date == todayStr }
            .sortedByDescending { it.id }

        val dp = resources.displayMetrics.density
        val symbols = DecimalFormatSymbols().apply {
            groupingSeparator = '.'; decimalSeparator = ','
        }
        val fmt = DecimalFormat("#,###", symbols)

        if (list.isEmpty()) {
            val tv = TextView(this).apply {
                text = "Belum ada transaksi hari ini."
                textSize = 13f
                setTextColor(0xFFAAAAAA.toInt())
                gravity = android.view.Gravity.CENTER
                setPadding(0, (16 * dp).toInt(), 0, 0)
            }
            container.addView(tv)
            return
        }

        val categoryDrawable = mapOf(
            "Makanan" to R.drawable.makanan,
            "Belanja" to R.drawable.belanja,
            "Transport" to R.drawable.transportasi,
            "Transportasi" to R.drawable.transportasi,
            "Rumah" to R.drawable.rumah,
            "Kesehatan" to R.drawable.kesehatan,
            "Hiburan" to R.drawable.hiburan,
            "Pendidikan" to R.drawable.pendidikan,
            "Tagihan" to R.drawable.tagihan,
            "Kustom" to R.drawable.bebas
        )

        for (t in list) {
            val card = MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.topMargin = (8 * dp).toInt() }
                radius = 20 * dp
                cardElevation = 4 * dp
                strokeColor = resources.getColor(R.color.transparent_black, null)
                strokeWidth = (1 * dp).toInt()
                isClickable = true
                isFocusable = true
            }

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding((16 * dp).toInt(), (16 * dp).toInt(), (16 * dp).toInt(), (16 * dp).toInt())
            }

            val icon = android.widget.ImageView(this).apply {
                setImageResource(categoryDrawable[t.category] ?: R.drawable.bebas)
                layoutParams = LinearLayout.LayoutParams((44 * dp).toInt(), (44 * dp).toInt()).also {
                    it.marginEnd = (12 * dp).toInt()
                }
                background = resources.getDrawable(R.drawable.circle_background, null)
                backgroundTintList = android.content.res.ColorStateList.valueOf(
                    if (t.isExpense) resources.getColor(R.color.expense_red, null)
                    else resources.getColor(R.color.income_green, null)
                )
                scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
                setPadding((8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt(), (8 * dp).toInt())
            }

            val textCol = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tvTitle = TextView(this).apply {
                text = t.category
                textSize = 14f
                setTextColor(resources.getColor(R.color.black, null))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            val tvSub = TextView(this).apply {
                text = t.note.ifBlank { t.date }
                textSize = 12f
                setTextColor(resources.getColor(R.color.black, null))
                setPadding(0, (4 * dp).toInt(), 0, 0)
            }
            textCol.addView(tvTitle)
            textCol.addView(tvSub)

            val tvAmount = TextView(this).apply {
                val prefix = if (t.isExpense) "-Rp " else "+Rp "
                text = "$prefix${fmt.format(t.amount)}"
                textSize = 13f
                setTextColor(
                    if (t.isExpense) resources.getColor(R.color.expense_red, null)
                    else resources.getColor(R.color.income_green, null)
                )
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            row.addView(icon)
            row.addView(textCol)
            row.addView(tvAmount)
            card.addView(row)

            card.setOnClickListener {
                val intent = Intent(this, TransactionDetailActivity::class.java)
                intent.putExtra("transaction", t)
                detailLauncher.launch(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }

            container.addView(card)
        }
    }

    private fun setupNavigation() {
        try {
            val vibrator = getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator

            // FAB button untuk tambah transaksi
            val addButton = findViewById<MaterialButton>(R.id.add_transaction_button)
            addButton.setOnClickListener {
                doVibrate(vibrator, 30)
                playButtonClickAnimation(addButton)
                addLauncher.launch(Intent(this, AddTransactionActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }

            // Bottom Navigation
            val bottomNav = findViewById<LinearLayout>(R.id.bottom_nav)
            
            // Set active state for current page (Home - index 0)
            setNavItemActive(bottomNav, 0)
            
            // Home (index 0) - already on home page
            bottomNav.getChildAt(0).setOnClickListener { view ->
                doVibrate(vibrator, 20)
                playButtonClickAnimation(view)
                // Already on home page, do nothing
            }
            
            // Transactions (index 1)
            bottomNav.getChildAt(1).setOnClickListener { view ->
                doVibrate(vibrator, 20)
                playButtonClickAnimation(view)
                startActivity(Intent(this, TransactionActivity::class.java))
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            
            // Reports (index 3)
            bottomNav.getChildAt(3).setOnClickListener { view ->
                doVibrate(vibrator, 20)
                playButtonClickAnimation(view)
                startActivity(Intent(this, ReportActivity::class.java))
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            
            // Profile (index 4)
            bottomNav.getChildAt(4).setOnClickListener { view ->
                doVibrate(vibrator, 20)
                playButtonClickAnimation(view)
                startActivity(Intent(this, ProfileActivity::class.java))
                finish()
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
