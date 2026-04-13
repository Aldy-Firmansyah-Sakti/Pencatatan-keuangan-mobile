package com.example.sistem_keuanan

import java.io.Serializable

data class Transaction(
    val id: Long = System.currentTimeMillis(),
    val amount: Long,
    val isExpense: Boolean,
    val category: String,
    val note: String,
    val date: String
) : Serializable
