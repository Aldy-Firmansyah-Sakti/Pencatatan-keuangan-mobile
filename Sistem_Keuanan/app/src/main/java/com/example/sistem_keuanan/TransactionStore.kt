package com.example.sistem_keuanan

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object TransactionStore {
    private const val PREF_NAME = "transactions"
    private const val KEY_LIST = "list"

    fun getAll(context: Context): MutableList<Transaction> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_LIST, "[]") ?: "[]"
        val arr = JSONArray(json)
        val list = mutableListOf<Transaction>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(Transaction(
                id = obj.getLong("id"),
                amount = obj.getLong("amount"),
                isExpense = obj.getBoolean("isExpense"),
                category = obj.getString("category"),
                note = obj.getString("note"),
                date = obj.getString("date")
            ))
        }
        return list
    }

    fun add(context: Context, t: Transaction) {
        val list = getAll(context)
        list.add(0, t)
        save(context, list)
    }

    fun delete(context: Context, id: Long) {
        val list = getAll(context).filter { it.id != id }
        save(context, list)
    }

    private fun save(context: Context, list: List<Transaction>) {
        val arr = JSONArray()
        for (t in list) {
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("amount", t.amount)
            obj.put("isExpense", t.isExpense)
            obj.put("category", t.category)
            obj.put("note", t.note)
            obj.put("date", t.date)
            arr.put(obj)
        }
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_LIST, arr.toString()).apply()
    }
}
