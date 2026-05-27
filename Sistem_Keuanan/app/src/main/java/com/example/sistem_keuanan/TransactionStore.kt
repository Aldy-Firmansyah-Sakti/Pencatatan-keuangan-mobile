package com.example.sistem_keuanan

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject

class TransactionStore(private val context: Context) {

    private val dao = AppDatabase.getDatabase(context).transactionDao()

    fun getAll(): Flow<List<Transaction>> = dao.getAll()

    fun getAllSync(): List<Transaction> = try {
        runBlocking { getAll().first() }
    } catch (e: Exception) {
        emptyList()
    }

    fun addSync(t: Transaction) {
        try { runBlocking { dao.insert(t) } } catch (_: Exception) {}
    }

    fun deleteSync(id: Long) {
        try {
            runBlocking {
                val t = dao.getById(id)
                if (t != null) dao.delete(t)
            }
        } catch (_: Exception) {}
    }

    fun clearAllSync() {
        try { runBlocking { dao.deleteAll() } } catch (_: Exception) {}
    }

    fun exportData(): String {
        val arr = JSONArray()
        for (t in getAllSync()) {
            arr.put(JSONObject().apply {
                put("id", t.id)
                put("amount", t.amount)
                put("isExpense", t.isExpense)
                put("category", t.category)
                put("note", t.note)
                put("date", t.date)
            })
        }
        return arr.toString()
    }

    fun importData(json: String) {
        try {
            val arr = JSONArray(json)
            clearAllSync()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                addSync(Transaction(
                    id = obj.getLong("id"),
                    amount = obj.getLong("amount"),
                    isExpense = obj.getBoolean("isExpense"),
                    category = obj.getString("category"),
                    note = obj.getString("note"),
                    date = obj.getString("date")
                ))
            }
        } catch (_: Exception) {}
    }
}
