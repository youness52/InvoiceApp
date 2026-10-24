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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.model.InvoiceStatus
import com.example.data.model.InvoiceWithDetails
import com.example.pdf.PdfGenerator
import com.example.ui.components.EmptyState
import com.example.ui.components.InvoiceStatusBadge
import com.example.ui.components.formatCurrency
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.navigation.ModalSheetState
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.StatusPaid
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoicesScreen(
    invoices: List<InvoiceWithDetails>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    statusFilter: InvoiceStatus?,
    onStatusFilterChange: (InvoiceStatus?) -> Unit,
    profile: BusinessProfileEntity?,
    language: AppLanguage,
    onOpenModal: (ModalSheetState) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val s = AppStrings[language]
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val filtered = invoices.filter { item ->
        val inv = item.invoice
        val matchesQuery = searchQuery.isBlank() ||
                inv.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                inv.customerName.contains(searchQuery, ignoreCase = true)
        val matchesStatus = statusFilter == null || inv.status == statusFilter
        matchesQuery && matchesStatus
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onOpenModal(ModalSheetState.CreateInvoice) },
                containerColor = PrimaryBlue,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = s.newInvoice)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(s.searchInvoiceHint) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = statusFilter == null,
                    onClick = { onStatusFilterChange(null) },
                    label = { Text("${s.all} (${invoices.size})") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f))
                )
                InvoiceStatus.values().forEach { status ->
                    val count = invoices.count { it.invoice.status == status }
                    val label = AppStrings.getStatusLabel(status, language)
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

            // Invoices List
            if (filtered.isEmpty()) {
                EmptyState(
                    title = s.noInvoicesFound,
                    message = if (searchQuery.isNotEmpty() || statusFilter != null)
                        s.searchInvoiceHint
                    else
                        s.noInvoicesDesc,
                    icon = Icons.Default.ReceiptLong
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.invoice.id }) { itemWithDetails ->
                        val inv = itemWithDetails.invoice
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenModal(ModalSheetState.InvoiceDetails(inv.id)) },
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
                                            text = inv.invoiceNumber,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = inv.customerName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    InvoiceStatusBadge(status = inv.status, language = language)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${s.issueDate}: ${dateFormat.format(Date(inv.issueDate))}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${s.dueDate}: ${dateFormat.format(Date(inv.dueDate))}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (inv.status == InvoiceStatus.OVERDUE) StatusOverdue else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = formatCurrency(inv.grandTotal, inv.currency),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (inv.balanceDue > 0) {
                                            Text(
                                                text = "${s.balanceDue}: ${formatCurrency(inv.balanceDue, inv.currency)}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = StatusOverdue
                                            )
                                        } else {
                                            Text(
                                                text = s.fullyPaid,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = StatusPaid
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (inv.balanceDue > 0) {
                                        Button(
                                            onClick = { onOpenModal(ModalSheetState.RecordPayment(inv.id)) },
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(s.recordPayment, fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            if (profile != null) {
                                                val file = PdfGenerator.generateInvoicePdf(context, itemWithDetails, profile, language)
                                                PdfGenerator.sharePdf(context, file, "${s.invoices} ${inv.invoiceNumber}")
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
