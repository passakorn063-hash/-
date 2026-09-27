package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TradeDatabase
import com.example.data.TradeOrderEntity
import com.example.data.TradeRepository
import com.example.util.DateUtils
import com.example.util.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class DaySummary(
    val dayNumber: Int,
    val tradeCount: Int,
    val netProfit: Double
)

data class JournalUiState(
    val selectedYear: Int,
    val selectedMonth: Int,
    val selectedDate: String,
    val monthlyProfit: Double = 0.0,
    val monthlyWinRate: Double = 0.0,
    val monthlyTradeCount: Int = 0,
    val totalProfitAllTime: Double = 0.0,
    val daySummaries: Map<Int, DaySummary> = emptyMap(),
    val selectedDayTrades: List<TradeOrderEntity> = emptyList(),
    val allTrades: List<TradeOrderEntity> = emptyList()
)

class TradeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TradeRepository

    private val calendar = Calendar.getInstance()
    private val _selectedYear = MutableStateFlow(calendar.get(Calendar.YEAR))
    private val _selectedMonth = MutableStateFlow(calendar.get(Calendar.MONTH) + 1)
    private val _selectedDate = MutableStateFlow(DateUtils.getTodayIsoString())

    init {
        val dao = TradeDatabase.getDatabase(application).tradeDao()
        repository = TradeRepository(dao)
    }

    val uiState: StateFlow<JournalUiState> = combine(
        _selectedYear,
        _selectedMonth,
        _selectedDate,
        repository.allTrades
    ) { year, month, selectedDate, allTrades ->
        val yearMonthPrefix = DateUtils.toYearMonthString(year, month)
        val monthlyTrades = allTrades.filter { it.dateString.startsWith(yearMonthPrefix) }

        val monthlyProfit = monthlyTrades.sumOf { it.profit }
        val monthlyWinCount = monthlyTrades.count { it.profit > 0 }
        val monthlyTradeCount = monthlyTrades.size
        val monthlyWinRate = if (monthlyTradeCount > 0) {
            (monthlyWinCount.toDouble() / monthlyTradeCount.toDouble()) * 100.0
        } else {
            0.0
        }

        val totalProfitAllTime = allTrades.sumOf { it.profit }

        // Group monthly trades by day
        val daySummaries = mutableMapOf<Int, DaySummary>()
        monthlyTrades.groupBy { it.dateString }.forEach { (dateStr, trades) ->
            val day = dateStr.split("-").getOrNull(2)?.toIntOrNull()
            if (day != null) {
                daySummaries[day] = DaySummary(
                    dayNumber = day,
                    tradeCount = trades.size,
                    netProfit = trades.sumOf { it.profit }
                )
            }
        }

        val selectedDayTrades = allTrades
            .filter { it.dateString == selectedDate }
            .sortedByDescending { it.timestamp }

        JournalUiState(
            selectedYear = year,
            selectedMonth = month,
            selectedDate = selectedDate,
            monthlyProfit = monthlyProfit,
            monthlyWinRate = monthlyWinRate,
            monthlyTradeCount = monthlyTradeCount,
            totalProfitAllTime = totalProfitAllTime,
            daySummaries = daySummaries,
            selectedDayTrades = selectedDayTrades,
            allTrades = allTrades
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = JournalUiState(
            selectedYear = calendar.get(Calendar.YEAR),
            selectedMonth = calendar.get(Calendar.MONTH) + 1,
            selectedDate = DateUtils.getTodayIsoString()
        )
    )

    fun onDateSelected(dayNumber: Int) {
        val dateStr = DateUtils.toIsoDateString(_selectedYear.value, _selectedMonth.value, dayNumber)
        _selectedDate.value = dateStr
    }

    fun previousMonth() {
        if (_selectedMonth.value == 1) {
            _selectedMonth.value = 12
            _selectedYear.value -= 1
        } else {
            _selectedMonth.value -= 1
        }
        updateSelectedDateToFirstDayOrPreserve()
    }

    fun nextMonth() {
        if (_selectedMonth.value == 12) {
            _selectedMonth.value = 1
            _selectedYear.value += 1
        } else {
            _selectedMonth.value += 1
        }
        updateSelectedDateToFirstDayOrPreserve()
    }

    private fun updateSelectedDateToFirstDayOrPreserve() {
        val currentDay = _selectedDate.value.split("-").getOrNull(2)?.toIntOrNull() ?: 1
        val maxDays = Calendar.getInstance().apply {
            set(Calendar.YEAR, _selectedYear.value)
            set(Calendar.MONTH, _selectedMonth.value - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }.getActualMaximum(Calendar.DAY_OF_MONTH)

        val targetDay = currentDay.coerceIn(1, maxDays)
        _selectedDate.value = DateUtils.toIsoDateString(_selectedYear.value, _selectedMonth.value, targetDay)
    }

    fun selectDateString(dateStr: String) {
        _selectedDate.value = dateStr
        val parts = dateStr.split("-")
        if (parts.size == 3) {
            parts[0].toIntOrNull()?.let { _selectedYear.value = it }
            parts[1].toIntOrNull()?.let { _selectedMonth.value = it }
        }
    }

    fun saveTrade(
        id: Long = 0,
        symbol: String,
        action: String,
        lotSize: Double,
        entryPrice: Double?,
        exitPrice: Double?,
        profit: Double,
        imageUri: String?,
        notes: String
    ) {
        viewModelScope.launch {
            val trade = TradeOrderEntity(
                id = id,
                dateString = _selectedDate.value,
                timestamp = if (id == 0L) System.currentTimeMillis() else System.currentTimeMillis(),
                symbol = symbol.trim().uppercase(),
                action = action,
                lotSize = lotSize,
                entryPrice = entryPrice,
                exitPrice = exitPrice,
                profit = profit,
                imageUri = imageUri,
                notes = notes.trim()
            )
            if (id == 0L) {
                repository.insert(trade)
            } else {
                repository.update(trade)
            }
        }
    }

    fun deleteTrade(trade: TradeOrderEntity) {
        viewModelScope.launch {
            repository.delete(trade)
        }
    }

    fun copyImageToStorage(uri: Uri): String? {
        return ImageUtils.saveImageToInternalStorage(getApplication(), uri)
    }

    fun insertSampleDataIfEmpty() {
        viewModelScope.launch {
            val year = _selectedYear.value
            val month = _selectedMonth.value
            val sampleTrades = listOf(
                TradeOrderEntity(
                    dateString = DateUtils.toIsoDateString(year, month, 1),
                    symbol = "XAUUSD",
                    action = "BUY",
                    lotSize = 0.50,
                    entryPrice = 2650.50,
                    exitPrice = 2665.00,
                    profit = 725.00,
                    notes = "Rejection Demand Zone H4 + FVG entry"
                ),
                TradeOrderEntity(
                    dateString = DateUtils.toIsoDateString(year, month, 1),
                    symbol = "EURUSD",
                    action = "SELL",
                    lotSize = 0.30,
                    entryPrice = 1.0850,
                    exitPrice = 1.0880,
                    profit = -90.00,
                    notes = "Stop out จากข่าว High Impact USD"
                ),
                TradeOrderEntity(
                    dateString = DateUtils.toIsoDateString(year, month, 4),
                    symbol = "BTCUSD",
                    action = "BUY",
                    lotSize = 0.20,
                    entryPrice = 64200.0,
                    exitPrice = 65800.0,
                    profit = 320.00,
                    notes = "Breakout 4H Resistance with high volume"
                ),
                TradeOrderEntity(
                    dateString = DateUtils.toIsoDateString(year, month, 15),
                    symbol = "GBPUSD",
                    action = "BUY",
                    lotSize = 0.40,
                    entryPrice = 1.3120,
                    exitPrice = 1.3185,
                    profit = 260.00,
                    notes = "London Session Sweep Liquidity"
                )
            )
            repository.insertAll(sampleTrades)
        }
    }
}
