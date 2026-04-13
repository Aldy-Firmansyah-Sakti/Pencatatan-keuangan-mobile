package com.utama.aplikasiloginsederhana

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class TicketListActivity : AppCompatActivity() {
    // Data statis (hardcoded)
    private val ticketList = listOf(
        Ticket(1, "Seminar AI & Masa Depan", "VIP", "A-001", "10 Mei 2026", 75000),
        Ticket(2, "Workshop Kotlin Android", "Regular", "B-015", "18 Mei 2026", 50000),
        Ticket(3, "Web Developer Gathering", "Standard", "C-042", "22 Mei 2026", 35000),
        Ticket(4, "UI/UX Design Bootcamp", "Premium", "D-008", "28 Mei 2026", 100000),
        Ticket(5, "Tech Career Fair 2026", "Free", "E-056", "8 Juni 2026", 0)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_list)

        val rvTickets = findViewById<RecyclerView>(R.id.rvTickets)
        rvTickets.layoutManager = LinearLayoutManager(this)
        val adapter = TicketAdapter(ticketList) { ticket ->
            Toast.makeText(this, "Tiket: ${ticket.eventName} - ${ticket.seatNumber}", Toast.LENGTH_SHORT).show()
        }
        rvTickets.adapter = adapter

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_ticket
        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_event -> {
                    val intent = Intent(this, EventListActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_ticket -> {
                    true
                }
                else -> false
            }
        }
    }
}
