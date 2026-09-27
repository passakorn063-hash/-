package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.JournalUiState
import java.util.Locale

@Composable
fun TradeStatsHeader(
    uiState: JournalUiState,
    onAddSampleData: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Banner Card - Dark navy style like in the screenshot
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("header_banner_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0F172A) // Sleek dark slate navy
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF38BDF8), Color(0xFF2563EB))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📒",
                            fontSize = 24.sp
                        )
                    }

                    Column {
                        Text(
                            text = "Forex Trade Journal",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.3.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "ปฏิทินบันทึกการเทรดของฉัน",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF94A3B8)
                            )
                        )
                    }
                }

                if (uiState.allTrades.isEmpty()) {
                    TextButton(
                        onClick = onAddSampleData,
                        modifier = Modifier.testTag("load_sample_data_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Sample Data",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ตัวอย่าง",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Stats Cards Row - 4 Metrics: Monthly Profit, Win Rate, Trade Count, All-Time Profit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "กำไรรวมเดือนนี้",
                value = formatProfitLoss(uiState.monthlyProfit),
                valueColor = getProfitColor(uiState.monthlyProfit),
                modifier = Modifier.weight(1f),
                testTag = "stat_monthly_profit"
            )

            StatCard(
                title = "Win Rate",
                value = String.format(Locale.US, "%.1f%%", uiState.monthlyWinRate),
                valueColor = Color(0xFF0F172A),
                modifier = Modifier.weight(1f),
                testTag = "stat_win_rate"
            )

            StatCard(
                title = "จำนวนเทรด",
                value = "${uiState.monthlyTradeCount}",
                valueColor = Color(0xFF0F172A),
                modifier = Modifier.weight(1f),
                testTag = "stat_trade_count"
            )

            StatCard(
                title = "กำไรรวมทั้งหมด",
                value = formatProfitLoss(uiState.totalProfitAllTime),
                valueColor = getProfitColor(uiState.totalProfitAllTime),
                modifier = Modifier.weight(1f),
                testTag = "stat_total_profit"
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Card(
        modifier = modifier
            .testTag(testTag)
            .height(86.dp)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                ),
                maxLines = 1
            )
        }
    }
}

private fun formatProfitLoss(amount: Double): String {
    return if (amount >= 0.0) {
        String.format(Locale.US, "+%.2f", amount)
    } else {
        String.format(Locale.US, "%.2f", amount)
    }
}

private fun getProfitColor(amount: Double): Color {
    return when {
        amount > 0.0 -> Color(0xFF10B981) // Crisp modern green
        amount < 0.0 -> Color(0xFFEF4444) // Crisp modern red
        else -> Color(0xFF10B981)
    }
}
