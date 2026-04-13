package com.utama.aplikasiloginsederhana

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class EventAdapter(
    private val events: List<Event>,
    private val onItemClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view as MaterialCardView
        val tvName: TextView = view.findViewById(R.id.tvEventName)
        val tvDate: TextView = view.findViewById(R.id.tvEventDate)
        val tvLocation: TextView =
            view.findViewById(R.id.tvEventLocation)
        val tvPrice: TextView = view.findViewById(R.id.tvEventPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType:
    Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val event = events[position]
        holder.tvName.text = event.name
        holder.tvDate.text = "📅 ${event.date}"
        holder.tvLocation.text = "📍 ${event.location}"
        holder.tvPrice.text = "💰 ${event.getFormattedPrice()}"

        // Warna harga: hijau untuk gratis, biru untuk berbayar
        if (event.price == 0) {

            holder.tvPrice.setTextColor(holder.itemView.context.getColor(android.
            R.color.holo_green_dark))
        } else {

            holder.tvPrice.setTextColor(holder.itemView.context.getColor(android.
            R.color.black))
        }

        holder.card.setOnClickListener {
            onItemClick(event)
        }
    }

    override fun getItemCount(): Int = events.size
}