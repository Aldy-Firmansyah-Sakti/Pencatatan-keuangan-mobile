package com.example.sistem_keuanan

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: Long = System.currentTimeMillis(),
    val amount: Long,
    val isExpense: Boolean,
    val category: String,
    val note: String,
    val date: String
) : Serializable
