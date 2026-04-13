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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.report_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupNavigation()
        updateSummary()
    }

    override fun onResume() {
        super.onResume()
        updateSummary()
    }

    private fun updateSummary() {
        val all = TransactionStore.getAll(this)
        val syms = java.text.DecimalFormatSymbols().apply {
            groupingSeparator = '.'; decimalSeparator = ','
        }
        val fmt = java.text.DecimalFormat("#,###", syms)

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

        val income  = thisMonthTx.filter { !it.isExpense }.sumOf { it.amount }
        val expense = thisMonthTx.filter {  it.isExpense }.sumOf { it.amount }

        findViewById<android.widget.TextView>(R.id.tv_report_income).text  = "Rp ${fmt.format(income)}"
        findViewById<android.widget.TextView>(R.id.tv_report_expense).text = "Rp ${fmt.format(expense)}"
    }

    private fun setupNavigation() {
        try {
            val vibrator = getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
            val bottomNav = findViewById<LinearLayout>(R.id.bottom_nav)
            
            // Set active state for current page (Reports - index 2)
            setNavItemActive(bottomNav, 2)
            
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
            bottomNav.getChildAt(2).setOnClickListener { view ->
                doVibrate(vibrator, 20)
                playButtonClickAnimation(view)
                // Already on reports page
            }
            
            // Profile navigation
            bottomNav.getChildAt(3).setOnClickListener { view ->
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