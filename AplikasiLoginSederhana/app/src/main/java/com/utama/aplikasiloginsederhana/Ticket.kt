package com.utama.aplikasiloginsederhana

data class Ticket(
    val id: Int,
    val eventName: String,
    val ticketType: String,
    val seatNumber: String,
    val purchaseDate: String,
    val price: Int
) {
    fun getFormattedPrice(): String {
        return "Rp ${price}"
    }
}
