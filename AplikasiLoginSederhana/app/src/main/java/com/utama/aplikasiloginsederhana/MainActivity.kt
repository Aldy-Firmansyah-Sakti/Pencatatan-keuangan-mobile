package com.utama.aplikasiloginsederhana

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val tvUsername = findViewById<TextView>(R.id.tvUsername)
        val username = intent.getStringExtra("username")

        tvWelcome.text = "Selamat datang"
        tvUsername.text = username ?: "-"
    }
}
