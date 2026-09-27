package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.TradeOrderEntity
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeEntryForm(
    selectedDate: String,
    editingTrade: TradeOrderEntity?,
    onSaveTrade: (
        id: Long,
        symbol: String,
        action: String,
        lotSize: Double,
        entryPrice: Double?,
        exitPrice: Double?,
        profit: Double,
        imageUri: String?,
        notes: String
    ) -> Unit,
    onCancelEdit: () -> Unit,
    onCopyImageToStorage: (Uri) -> String?,
    onViewImage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var symbol by remember { mutableStateOf("") }
    var action by remember { mutableStateOf("BUY") }
    var entryPriceText by remember { mutableStateOf("") }
    var exitPriceText by remember { mutableStateOf("") }
    var lotSizeText by remember { mutableStateOf("0.01") }
    var profitText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var currentImageUri by remember { mutableStateOf<String?>(null) }
    var actionDropdownExpanded by remember { mutableStateOf(false) }

    val popularPairs = listOf("XAUUSD", "EURUSD", "GBPUSD", "USDJPY", "BTCUSD", "US30", "NAS100")

    // Update form when editingTrade changes
    LaunchedEffect(editingTrade) {
        if (editingTrade != null) {
            symbol = editingTrade.symbol
            action = editingTrade.action
            entryPriceText = editingTrade.entryPrice?.toString() ?: ""
            exitPriceText = editingTrade.exitPrice?.toString() ?: ""
            lotSizeText = editingTrade.lotSize.toString()
            profitText = String.format(Locale.US, "%.2f", editingTrade.profit)
            notes = editingTrade.notes
            currentImageUri = editingTrade.imageUri
        } else {
            symbol = ""
            action = "BUY"
            entryPriceText = ""
            exitPriceText = ""
            lotSizeText = "0.01"
            profitText = ""
            notes = ""
            currentImageUri = null
        }
    }

    // Photo picker launcher (using Android Photo Picker for privacy & Play policy)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = onCopyImageToStorage(uri)
            if (savedPath != null) {
                currentImageUri = savedPath
            } else {
                currentImageUri = uri.toString()
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            .testTag("trade_entry_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Form title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (editingTrade == null) "➕ จดบันทึกออเดอร์" else "✏️ แก้ไขออเดอร์",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 17.sp
                    )
                )

                if (editingTrade != null) {
                    OutlinedButton(
                        onClick = onCancelEdit,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("cancel_edit_button")
                    ) {
                        Text("ยกเลิก", fontSize = 12.sp)
                    }
                }
            }

            // 1. คู่เงิน (Symbol)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "คู่เงิน",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B),
                        fontSize = 14.sp
                    )
                )

                OutlinedTextField(
                    value = symbol,
                    onValueChange = { symbol = it.uppercase() },
                    placeholder = { Text("เช่น XAUUSD", color = Color(0xFF94A3B8)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_symbol"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4F46E5),
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    )
                )

                // Quick symbol chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    popularPairs.forEach { pair ->
                        val isSelected = symbol == pair
                        FilterChip(
                            selected = isSelected,
                            onClick = { symbol = pair },
                            label = { Text(pair, fontSize = 12.sp) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEEF2FF),
                                selectedLabelColor = Color(0xFF4F46E5)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color(0xFF4F46E5) else Color(0xFFE2E8F0)
                            )
                        )
                    }
                }
            }

            // 2. ฝั่ง (BUY / SELL dropdown / selector)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "ฝั่ง",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B),
                        fontSize = 14.sp
                    )
                )

                ExposedDropdownMenuBox(
                    expanded = actionDropdownExpanded,
                    onExpandedChange = { actionDropdownExpanded = !actionDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = action,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = actionDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("select_action"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4F46E5),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = actionDropdownExpanded,
                        onDismissRequest = { actionDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "BUY (ซื้อ)",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            },
                            onClick = {
                                action = "BUY"
                                actionDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "SELL (ขาย)",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB91C1C)
                                )
                            },
                            onClick = {
                                action = "SELL"
                                actionDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // 3. Entry Price, Exit Price, Lot Size Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ราคาเข้า
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "ราคาเข้า",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B),
                            fontSize = 13.sp
                        )
                    )
                    OutlinedTextField(
                        value = entryPriceText,
                        onValueChange = { entryPriceText = it },
                        placeholder = { Text("0.00", color = Color(0xFF94A3B8)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_entry_price"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4F46E5),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        )
                    )
                }

                // ราคาออก
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "ราคาออก",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B),
                            fontSize = 13.sp
                        )
                    )
                    OutlinedTextField(
                        value = exitPriceText,
                        onValueChange = { exitPriceText = it },
                        placeholder = { Text("0.00", color = Color(0xFF94A3B8)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_exit_price"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4F46E5),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        )
                    )
                }

                // Lot Size
                Column(
                    modifier = Modifier.weight(0.9f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "ขนาด Lot",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B),
                            fontSize = 13.sp
                        )
                    )
                    OutlinedTextField(
                        value = lotSizeText,
                        onValueChange = { lotSizeText = it },
                        placeholder = { Text("0.01", color = Color(0xFF94A3B8)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_lot_size"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4F46E5),
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        )
                    )
                }
            }

            // 4. กำไร / ขาดทุน ($)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "กำไร / ขาดทุน ($ หรือ ฿)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B),
                            fontSize = 14.sp
                        )
                    )

                    // Auto-calculate helper if prices are given
                    val ep = entryPriceText.toDoubleOrNull()
                    val xp = exitPriceText.toDoubleOrNull()
                    val lot = lotSizeText.toDoubleOrNull() ?: 0.01
                    if (ep != null && xp != null) {
                        Text(
                            text = "คำนวณจากราคา",
                            color = Color(0xFF4F46E5),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable {
                                    val diff = if (action == "BUY") (xp - ep) else (ep - xp)
                                    // Estimate: XAUUSD ~ 100 multiplier per lot per point, or direct diff * 100000 * lot for forex
                                    val calcProfit = if (symbol.contains("XAU")) {
                                        diff * 100.0 * lot
                                    } else {
                                        diff * 100000.0 * lot
                                    }
                                    profitText = String.format(Locale.US, "%.2f", calcProfit)
                                }
                                .padding(4.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = profitText,
                    onValueChange = { profitText = it },
                    placeholder = { Text("เช่น 150.00 หรือ -50.00", color = Color(0xFF94A3B8)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_profit"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4F46E5),
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    )
                )

                // Quick profit adjustment chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quickAmounts = listOf("+50", "+100", "+200", "-50", "-100", "0")
                    quickAmounts.forEach { amountStr ->
                        SuggestionChip(
                            onClick = { profitText = amountStr.replace("+", "") },
                            label = {
                                Text(
                                    amountStr,
                                    fontSize = 11.sp,
                                    color = if (amountStr.startsWith("+")) Color(0xFF15803D)
                                    else if (amountStr.startsWith("-")) Color(0xFFB91C1C)
                                    else Color(0xFF475569),
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = SuggestChipColors(amountStr)
                        )
                    }
                }
            }

            // 5. แนบรูปจุดเข้า / ชาร์ตแท่งเทียน
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "รูปจุดเข้า / ชาร์ตแท่งเทียน (หมายเหตุเป็นภาพ)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B),
                        fontSize = 14.sp
                    )
                )

                if (currentImageUri.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("pick_chart_image_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFF8FAFC)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFCBD5E1))
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Pick Image",
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "เลือกรูปจุดเข้า / ภาพชาร์ตจากเครื่อง",
                            color = Color(0xFF4F46E5),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    // Preview of attached chart screenshot
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val file = if (currentImageUri!!.startsWith("/")) File(currentImageUri!!) else currentImageUri!!
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            AsyncImage(
                                model = file,
                                contentDescription = "Attached chart preview",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onViewImage(currentImageUri!!) },
                                contentScale = ContentScale.Crop
                            )
                            Column {
                                Text(
                                    text = "แนบรูปชาร์ตแล้ว",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF15803D)
                                )
                                Text(
                                    text = "แตะที่รูปเพื่อดูขนาดเต็ม",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Row {
                            IconButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Change Image",
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = { currentImageUri = null },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Image",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 6. หมายเหตุ / จุดเข้า (Notes)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "หมายเหตุ / จุดเข้า",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B),
                        fontSize = 14.sp
                    )
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = {
                        Text(
                            "เช่น เข้าตามแนวรับ H1, มี Divergence RSI, ชน Supply zone",
                            color = Color(0xFF94A3B8)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_notes"),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4F46E5),
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    )
                )
            }

            // Submit Button
            val isFormValid = symbol.isNotBlank() && profitText.isNotBlank()
            Button(
                onClick = {
                    if (isFormValid) {
                        val parsedProfit = profitText.toDoubleOrNull() ?: 0.0
                        val parsedLot = lotSizeText.toDoubleOrNull() ?: 0.01
                        val parsedEntry = entryPriceText.toDoubleOrNull()
                        val parsedExit = exitPriceText.toDoubleOrNull()

                        onSaveTrade(
                            editingTrade?.id ?: 0L,
                            symbol,
                            action,
                            parsedLot,
                            parsedEntry,
                            parsedExit,
                            parsedProfit,
                            currentImageUri,
                            notes
                        )

                        // Reset fields
                        symbol = ""
                        action = "BUY"
                        entryPriceText = ""
                        exitPriceText = ""
                        lotSizeText = "0.01"
                        profitText = ""
                        notes = ""
                        currentImageUri = null
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_trade_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0F172A),
                    disabledContainerColor = Color(0xFF94A3B8)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (editingTrade == null) "💾 บันทึกออเดอร์" else "💾 อัปเดตออเดอร์",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun SuggestChipColors(amountStr: String) = SuggestionChipDefaults.suggestionChipColors(
    containerColor = if (amountStr.startsWith("+")) Color(0xFFDCFCE7)
    else if (amountStr.startsWith("-")) Color(0xFFFEE2E2)
    else Color(0xFFF1F5F9)
)
