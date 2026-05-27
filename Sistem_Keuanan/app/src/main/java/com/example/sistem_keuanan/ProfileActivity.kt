package com.example.sistem_keuanan

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import android.app.AlertDialog
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.google.android.material.button.MaterialButton
import android.os.VibrationEffect
import android.os.Vibrator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator

class ProfileActivity : AppCompatActivity() {
    private lateinit var transactionStore: TransactionStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_profile)

        findViewById<MaterialButton>(R.id.add_transaction_button)?.apply {
            bringToFront()
            ViewCompat.setElevation(this, 12f)
        }

        transactionStore = TransactionStore(this)

        setupNavigation()
        setupBackup()
    }

    override fun onResume() {
        super.onResume()
        findViewById<MaterialButton>(R.id.add_transaction_button)?.apply {
            bringToFront()
            ViewCompat.setElevation(this, 12f)
        }
    }

    private fun setupBackup() {
        findViewById<MaterialButton>(R.id.export_button).setOnClickListener {
            exportData()
        }
        findViewById<MaterialButton>(R.id.import_button).setOnClickListener {
            importData()
        }
    }

    private fun exportData() {
        // Dialog untuk nama file
        val input = EditText(this).apply {
            hint = "Nama file backup"
            setText("backup_keuangan")
            setSingleLine()
        }
        AlertDialog.Builder(this)
            .setTitle("Nama File Export")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val rawName = input.text.toString().trim().ifBlank { "backup_keuangan" }
                val fileName = if (rawName.endsWith(".json")) rawName else "$rawName.json"
                exportLauncher.launch(fileName)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // Launcher untuk pilih lokasi simpan file (user bebas pilih folder)
    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri ?: return@registerForActivityResult
        try {
            val data = transactionStore.exportData()
            contentResolver.openOutputStream(uri)?.use { it.write(data.toByteArray()) }
            Toast.makeText(this, "Data berhasil diekspor", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal export: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun importData() {
        importLauncher.launch("application/json")
    }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@registerForActivityResult
        try {
            val data = contentResolver.openInputStream(uri)
                ?.bufferedReader()?.use { it.readText() } ?: ""
            if (data.isBlank()) {
                Toast.makeText(this, "File kosong atau tidak valid", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            transactionStore.importData(data)
            Toast.makeText(this, "Data berhasil diimpor", Toast.LENGTH_SHORT).show()
            // Kembali ke Home agar beranda langsung refresh
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal import: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupNavigation() {
        try {
            val vibrator = getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
            val bottomNav = findViewById<LinearLayout>(R.id.bottom_nav)
            
            // Set active state for current page (Profile - index 4)
            setNavItemActive(bottomNav, 4)
            
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
            
            // Reports navigation
            bottomNav.getChildAt(3).setOnClickListener { view ->
                doVibrate(vibrator, 20)
                playButtonClickAnimation(view)
                startActivity(Intent(this, ReportActivity::class.java))
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            
            // Profile (current page)
            bottomNav.getChildAt(4).setOnClickListener { view ->
                doVibrate(vibrator, 20)
                playButtonClickAnimation(view)
                // Already on profile page
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