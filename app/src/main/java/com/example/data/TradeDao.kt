package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Query("SELECT * FROM trade_orders ORDER BY timestamp DESC")
    fun getAllTrades(): Flow<List<TradeOrderEntity>>

    @Query("SELECT * FROM trade_orders WHERE dateString = :date ORDER BY timestamp ASC")
    fun getTradesForDate(date: String): Flow<List<TradeOrderEntity>>

    @Query("SELECT * FROM trade_orders WHERE dateString LIKE :yearMonth || '%' ORDER BY dateString ASC, timestamp ASC")
    fun getTradesForMonth(yearMonth: String): Flow<List<TradeOrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeOrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrades(trades: List<TradeOrderEntity>)

    @Update
    suspend fun updateTrade(trade: TradeOrderEntity)

    @Delete
    suspend fun deleteTrade(trade: TradeOrderEntity)

    @Query("DELETE FROM trade_orders WHERE id = :id")
    suspend fun deleteTradeById(id: Long)

    @Query("DELETE FROM trade_orders")
    suspend fun deleteAllTrades()
}
