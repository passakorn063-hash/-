package com.example.data

import kotlinx.coroutines.flow.Flow

class TradeRepository(private val tradeDao: TradeDao) {
    val allTrades: Flow<List<TradeOrderEntity>> = tradeDao.getAllTrades()

    fun getTradesForDate(date: String): Flow<List<TradeOrderEntity>> {
        return tradeDao.getTradesForDate(date)
    }

    fun getTradesForMonth(yearMonth: String): Flow<List<TradeOrderEntity>> {
        return tradeDao.getTradesForMonth(yearMonth)
    }

    suspend fun insert(trade: TradeOrderEntity): Long {
        return tradeDao.insertTrade(trade)
    }

    suspend fun insertAll(trades: List<TradeOrderEntity>) {
        tradeDao.insertTrades(trades)
    }

    suspend fun update(trade: TradeOrderEntity) {
        tradeDao.updateTrade(trade)
    }

    suspend fun delete(trade: TradeOrderEntity) {
        tradeDao.deleteTrade(trade)
    }

    suspend fun deleteById(id: Long) {
        tradeDao.deleteTradeById(id)
    }

    suspend fun clearAll() {
        tradeDao.deleteAllTrades()
    }
}
