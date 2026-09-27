package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trade_orders")
data class TradeOrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String,      // Format: "YYYY-MM-DD" e.g. "2026-09-28"
    val timestamp: Long = System.currentTimeMillis(),
    val symbol: String,          // e.g. "XAUUSD", "EURUSD"
    val action: String,          // "BUY" or "SELL"
    val lotSize: Double = 0.01,  // e.g. 0.10
    val entryPrice: Double? = null,
    val exitPrice: Double? = null,
    val profit: Double = 0.0,    // Net profit or loss (e.g. +150.0 or -50.0)
    val imageUri: String? = null,// Local file URI string for chart entry screenshot
    val notes: String = ""       // Reason/notes for the trade entry
)
