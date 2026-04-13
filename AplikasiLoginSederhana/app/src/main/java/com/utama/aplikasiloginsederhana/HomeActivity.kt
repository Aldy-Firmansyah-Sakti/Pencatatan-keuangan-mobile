package com.utama.aplikasiloginsederhana

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val tvUsername = findViewById<TextView>(R.id.tvUsername)

        // Ambil username dari SharedPreferences
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", "Guest") ?: "Guest"
        tvUsername.text = username

        val cardUpcomingEvent =
            findViewById<CardView>(R.id.cardUpcomingEvent)
        cardUpcomingEvent.setOnClickListener {
            Toast.makeText(this, "Seminar Teknologi AI - 10 April 2026", Toast.LENGTH_SHORT).show()
        }
        val cardEvent = findViewById<CardView>(R.id.cardEvent)
        cardEvent.setOnClickListener {
            val intent = Intent(this, EventListActivity::class.java)
            startActivity(intent)
        }
        val cardTicket = findViewById<CardView>(R.id.cardTicket)
        cardTicket.setOnClickListener {
            val intent = Intent(this, TicketListActivity::class.java)
            startActivity(intent)
        }
        val bottomNav =
            findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_event -> {
                    startActivity(Intent(this, EventListActivity::class.java))
                    true
                }
                R.id.nav_ticket -> {
                    startActivity(Intent(this, TicketListActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
