package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessProfileEntity
import com.example.data.model.QuotationStatus
import com.example.data.model.QuotationWithDetails
import com.example.pdf.PdfGenerator
import com.example.ui.components.EmptyState
import com.example.ui.components.QuotationStatusBadge
import com.example.ui.components.formatCurrency
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.navigation.ModalSheetState
import com.example.ui.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QuotationsScreen(
    quotations: List<QuotationWithDetails>,
    statusFilter: QuotationStatus?,
    onStatusFilterChange: (QuotationStatus?) -> Unit,
    profile: BusinessProfileEntity?,
    language: AppLanguage,
    onOpenModal: (ModalSheetState) -> Unit,
    onConvertQuotation: (QuotationWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val s = AppStrings[language]
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val filtered = quotations.filter { item ->
        statusFilter == null || item.quotation.status == statusFilter
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onOpenModal(ModalSheetState.CreateQuotation) },
                containerColor = PrimaryBlue,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = s.newQuotation)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Status Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { onStatusFilterChange(null) },
                    label = { Text("${s.all} (${quotations.size})") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f))
                )
                QuotationStatus.values().forEach { status ->
                    val count = quotations.count { it.quotation.status == status }
                    val label = AppStrings.getQuotationStatusLabel(status, language)
                    FilterChip(
                        selected = statusFilter == status,
                        onClick = {
                            if (statusFilter == status) onStatusFilterChange(null)
                            else onStatusFilterChange(status)
                        },
                        label = { Text("$label ($count)") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f))
                    )
                }
            }

            if (filtered.isEmpty()) {
                EmptyState(
                    title = s.noQuotationsFound,
                    message = s.noQuotationsDesc,
                    icon = Icons.Default.Description
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.quotation.id }) { itemWithDetails ->
                        val quote = itemWithDetails.quotation
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenModal(ModalSheetState.QuotationDetails(quote.id)) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = quote.quotationNumber,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = quote.customerName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    QuotationStatusBadge(status = quote.status, language = language)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${s.issueDate}: ${dateFormat.format(Date(quote.issueDate))}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${s.validUntil}: ${dateFormat.format(Date(quote.validUntil))}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = formatCurrency(quote.grandTotal, quote.currency),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (quote.status != QuotationStatus.CONVERTED) {
                                        Button(
                                            onClick = { onConvertQuotation(itemWithDetails) },
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(s.convertToInvoice, fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            if (profile != null) {
                                                val file = PdfGenerator.generateQuotationPdf(context, itemWithDetails, profile, language)
                                                PdfGenerator.sharePdf(context, file, "${s.quotations} ${quote.quotationNumber}")
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Share,
                                            contentDescription = s.sharePdf,
                                            tint = PrimaryBlue
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
