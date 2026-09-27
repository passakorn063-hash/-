package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TradeOrderEntity
import com.example.ui.components.TradeCalendarView
import com.example.ui.components.TradeEntryForm
import com.example.ui.components.TradeImageDialog
import com.example.ui.components.TradeOrderCard
import com.example.ui.components.TradeStatsHeader
import com.example.util.DateUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeJournalScreen(
    viewModel: TradeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var editingTrade by remember { mutableStateOf<TradeOrderEntity?>(null) }
    var tradeToDelete by remember { mutableStateOf<TradeOrderEntity?>(null) }
    var selectedImageForDialog by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = วันนี้ (จด/ดู), 1 = ประวัติทั้งหมด

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("trade_journal_scroll_list"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Top Header Banner & 4 Stat Cards
            item {
                TradeStatsHeader(
                    uiState = uiState,
                    onAddSampleData = { viewModel.insertSampleDataIfEmpty() }
                )
            }

            // 2. Interactive Thai Calendar matching screenshot
            item {
                TradeCalendarView(
                    year = uiState.selectedYear,
                    month = uiState.selectedMonth,
                    selectedDate = uiState.selectedDate,
                    daySummaries = uiState.daySummaries,
                    onDaySelected = { day ->
                        viewModel.onDateSelected(day)
                        editingTrade = null
                    },
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() }
                )
            }

            // 3. Selected Day Header Bar
            item {
                val fullDateText = remember(uiState.selectedDate) {
                    DateUtils.formatDateToThaiFull(uiState.selectedDate)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                        .testTag("selected_day_header_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "📅",
                                fontSize = 18.sp
                            )
                            Text(
                                text = fullDateText,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color(0xFF0F172A)
                                )
                            )
                        }

                        // Daily summary badge
                        val dayTrades = uiState.selectedDayTrades
                        if (dayTrades.isNotEmpty()) {
                            val dayNetProfit = dayTrades.sumOf { it.profit }
                            val isProfitable = dayNetProfit >= 0
                            val pnlStr = if (isProfitable) {
                                String.format(Locale.US, "+$%.2f", dayNetProfit)
                            } else {
                                String.format(Locale.US, "-$%.2f", Math.abs(dayNetProfit))
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isProfitable) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${dayTrades.size} ออเดอร์ ($pnlStr)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isProfitable) Color(0xFF15803D) else Color(0xFFB91C1C)
                                )
                            }
                        }
                    }
                }
            }

            // 4. If this day has recorded trades, list them
            if (uiState.selectedDayTrades.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "ออเดอร์ของวันนี้ (${uiState.selectedDayTrades.size})",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155),
                                fontSize = 14.sp
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                items(
                    items = uiState.selectedDayTrades,
                    key = { it.id }
                ) { trade ->
                    TradeOrderCard(
                        trade = trade,
                        onEdit = {
                            editingTrade = it
                        },
                        onDelete = {
                            tradeToDelete = it
                        },
                        onViewImage = { imagePath ->
                            selectedImageForDialog = imagePath
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )
                }
            }

            // 5. Trade Entry Form (matching screenshot position right below selected day header)
            item {
                TradeEntryForm(
                    selectedDate = uiState.selectedDate,
                    editingTrade = editingTrade,
                    onSaveTrade = { id, symbol, action, lotSize, entryPrice, exitPrice, profit, imageUri, notes ->
                        viewModel.saveTrade(
                            id = id,
                            symbol = symbol,
                            action = action,
                            lotSize = lotSize,
                            entryPrice = entryPrice,
                            exitPrice = exitPrice,
                            profit = profit,
                            imageUri = imageUri,
                            notes = notes
                        )
                        editingTrade = null
                    },
                    onCancelEdit = { editingTrade = null },
                    onCopyImageToStorage = { uri -> viewModel.copyImageToStorage(uri) },
                    onViewImage = { imagePath -> selectedImageForDialog = imagePath },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Delete Confirmation Dialog
    if (tradeToDelete != null) {
        AlertDialog(
            onDismissRequest = { tradeToDelete = null },
            title = {
                Text(
                    text = "ลบออเดอร์นี้หรือไม่?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "คุณแน่ใจหรือไม่ว่าต้องการลบออเดอร์ ${tradeToDelete?.symbol} (${tradeToDelete?.action}) นี้ การลบจะไม่สามารถกู้คืนได้"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        tradeToDelete?.let { viewModel.deleteTrade(it) }
                        if (editingTrade?.id == tradeToDelete?.id) {
                            editingTrade = null
                        }
                        tradeToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("ลบ", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { tradeToDelete = null }
                ) {
                    Text("ยกเลิก")
                }
            }
        )
    }

    // Full screen image preview dialog
    if (selectedImageForDialog != null) {
        TradeImageDialog(
            imagePath = selectedImageForDialog!!,
            title = "รูปชาร์ตจุดเข้า / หมายเหตุ",
            onDismiss = { selectedImageForDialog = null }
        )
    }
}
