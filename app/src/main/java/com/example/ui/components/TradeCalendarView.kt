package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DaySummary
import com.example.util.DateUtils
import java.util.Locale

@Composable
fun TradeCalendarView(
    year: Int,
    month: Int,
    selectedDate: String,
    daySummaries: Map<Int, DaySummary>,
    onDaySelected: (Int) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthYearText = remember(year, month) {
        DateUtils.formatMonthYearThai(year, month)
    }

    val calendarCells = remember(year, month) {
        DateUtils.getMonthCalendarCells(year, month)
    }

    val selectedDayNumber = remember(selectedDate, year, month) {
        val parts = selectedDate.split("-")
        if (parts.size == 3 &&
            parts[0].toIntOrNull() == year &&
            parts[1].toIntOrNull() == month
        ) {
            parts[2].toIntOrNull()
        } else null
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .testTag("trade_calendar_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Month Navigation Row matching screenshot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Arrow Button - Dark filled rounded button
                IconButton(
                    onClick = onPreviousMonth,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .testTag("calendar_prev_month")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month",
                        tint = Color.White
                    )
                }

                Text(
                    text = monthYearText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    ),
                    textAlign = TextAlign.Center
                )

                // Right Arrow Button - Dark filled rounded button
                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .testTag("calendar_next_month")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Weekday Headers: อา, จ, อ, พ, พฤ, ศ, ส
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DateUtils.THAI_WEEKDAYS_SHORT.forEach { dayName ->
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B),
                            fontSize = 13.sp
                        ),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Grid of Days (chunks of 7)
            val rows = calendarCells.chunked(7)
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rows.forEach { rowCells ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 0 until 7) {
                            val dayNumber = rowCells.getOrNull(i)
                            if (dayNumber != null) {
                                val isSelected = dayNumber == selectedDayNumber
                                val summary = daySummaries[dayNumber]

                                CalendarDayCell(
                                    dayNumber = dayNumber,
                                    isSelected = isSelected,
                                    summary = summary,
                                    onSelect = { onDaySelected(dayNumber) },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                // Empty spacer cell
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(0.85f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    isSelected: Boolean,
    summary: DaySummary?,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasTrades = summary != null && summary.tradeCount > 0
    val isProfit = hasTrades && summary.netProfit > 0
    val isLoss = hasTrades && summary.netProfit < 0

    // Background color based on performance or selection
    val backgroundColor = when {
        isSelected -> Color(0xFFF8FAFC)
        isProfit -> Color(0xFFF0FDF4) // Soft green tint
        isLoss -> Color(0xFFFEF2F2)   // Soft red tint
        else -> Color(0xFFF8FAFC)
    }

    // Border: Selected gets blue border (like screenshot), otherwise subtle gray
    val borderColor = when {
        isSelected -> Color(0xFF4F46E5) // Accent blue border
        isProfit -> Color(0xFF86EFAC)
        isLoss -> Color(0xFFFCA5A5)
        else -> Color(0xFFE2E8F0)
    }

    val borderWidth = if (isSelected) 2.dp else 1.dp

    Box(
        modifier = modifier
            .aspectRatio(0.82f)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect)
            .padding(4.dp)
            .testTag("calendar_day_$dayNumber")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Day number at top-left
            Text(
                text = "$dayNumber",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected || hasTrades) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                    color = if (isSelected) Color(0xFF4F46E5) else Color(0xFF0F172A)
                )
            )

            // Trade Indicator at bottom if any trades exist
            if (hasTrades) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val pnlText = if (summary.netProfit >= 0) {
                        String.format(Locale.US, "+%.0f", summary.netProfit)
                    } else {
                        String.format(Locale.US, "%.0f", summary.netProfit)
                    }
                    Text(
                        text = pnlText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (summary.netProfit >= 0) Color(0xFF059669) else Color(0xFFDC2626)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${summary.tradeCount} ord",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            color = Color(0xFF64748B)
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}
