package com.utama.aplikasiloginsederhana

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class EventListActivity : AppCompatActivity() {
    // Data statis (hardcoded)
    private val eventList = listOf(
        Event(1, "Seminar AI & Masa Depan", "15 Mei 2026", "Hotel Grand Slipi", 50000),
        Event(2, "Workshop Kotlin Android", "20 Mei 2026", "Kampus Teknik", 75000),
        Event(3, "Web Developer Gathering", "25 Mei 2026", "Co working Space", 0),
        Event(4, "UI/UX Design Bootcamp", "1 Juni 2026", "Online (Zoom)", 100000),
        Event(5, "Tech Career Fair 2026", "10 Juni 2026", "Convention Center", 0)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_list)

        val rvEvents = findViewById<RecyclerView>(R.id.rvEvents)
        rvEvents.layoutManager = LinearLayoutManager(this)
        val adapter = EventAdapter(eventList) { event ->
            Toast.makeText(this, "Anda memilih: ${event.name}", Toast.LENGTH_SHORT).show()
        }
        rvEvents.adapter = adapter

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_event
        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_event -> {
                    true
                }
                R.id.nav_ticket -> {
                    val intent = Intent(this, TicketListActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
