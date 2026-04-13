package com.utama.aplikasiloginsederhana

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class TicketAdapter(
    private val tickets: List<Ticket>,
    private val onItemClick: (Ticket) -> Unit
) : RecyclerView.Adapter<TicketAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view as MaterialCardView
        val tvEventName: TextView = view.findViewById(R.id.tvTicketEventName)
        val tvTicketType: TextView = view.findViewById(R.id.tvTicketType)
        val tvSeatNumber: TextView = view.findViewById(R.id.tvSeatNumber)
        val tvPurchaseDate: TextView = view.findViewById(R.id.tvPurchaseDate)
        val tvPrice: TextView = view.findViewById(R.id.tvTicketPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ticket, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ticket = tickets[position]
        holder.tvEventName.text = ticket.eventName
        holder.tvTicketType.text = "🎫 ${ticket.ticketType}"
        holder.tvSeatNumber.text = "🪑 ${ticket.seatNumber}"
        holder.tvPurchaseDate.text = "📅 ${ticket.purchaseDate}"
        holder.tvPrice.text = "💳 ${ticket.getFormattedPrice()}"

        holder.card.setOnClickListener {
            onItemClick(ticket)
        }
    }

    override fun getItemCount(): Int = tickets.size
}
