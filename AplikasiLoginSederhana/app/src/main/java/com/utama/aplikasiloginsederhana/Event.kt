package com.utama.aplikasiloginsederhana
data class Event(
    val id: Int,
    val name: String,
    val date: String,
    val location: String,
    val price: Int
) {
    fun getFormattedPrice(): String {
        return if (price == 0) "Gratis" else "Rp ${price}"
    }
}