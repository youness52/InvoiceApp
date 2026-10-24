package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessProfileEntity
import com.example.data.model.InvoiceWithDetails
import com.example.pdf.PdfGenerator
import com.example.ui.components.EmptyState
import com.example.ui.components.InvoiceStatusBadge
import com.example.ui.components.RevenueBarChart
import com.example.ui.components.SummaryMetricCard
import com.example.ui.components.formatCurrency
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.navigation.ModalSheetState
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPending
import com.example.ui.viewmodel.DashboardMetrics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    metrics: DashboardMetrics,
    recentInvoices: List<InvoiceWithDetails>,
    profile: BusinessProfileEntity?,
    language: AppLanguage,
    onOpenModal: (ModalSheetState) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val s = AppStrings[language]
    val currency = profile?.currencyCode ?: "MAD"
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = profile?.businessName ?: s.appName,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = s.businessOverview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryMetricCard(
                        title = s.totalInvoiced,
                        value = metrics.totalInvoiced,
                        currency = currency,
                        icon = Icons.Default.MonetizationOn,
                        iconColor = PrimaryBlue,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricCard(
                        title = s.amountReceived,
                        value = metrics.totalPaid,
                        currency = currency,
                        icon = Icons.Default.CheckCircle,
                        iconColor = StatusPaid,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryMetricCard(
                        title = s.outstandingBalance,
                        value = metrics.outstandingBalance,
                        currency = currency,
                        icon = Icons.Default.HourglassEmpty,
                        iconColor = StatusPending,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricCard(
                        title = s.overdue,
                        value = metrics.overdueAmount,
                        currency = currency,
                        icon = Icons.Default.Error,
                        iconColor = StatusOverdue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Text(
                text = s.quickActions,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Button(
                        onClick = { onOpenModal(ModalSheetState.CreateInvoice) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(s.newInvoice)
                    }
                }
                item {
                    OutlinedButton(
                        onClick = { onOpenModal(ModalSheetState.CreateQuotation) },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(s.newQuotation)
                    }
                }
                item {
                    OutlinedButton(
                        onClick = { onOpenModal(ModalSheetState.EditCustomer(null)) },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(s.addCustomer)
                    }
                }
                item {
                    OutlinedButton(
                        onClick = { onOpenModal(ModalSheetState.EditProduct(null)) },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(s.addProduct)
                    }
                }
            }
        }

        item {
            RevenueBarChart(
                data = metrics.monthlyRevenue,
                currency = currency
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = s.recentInvoices,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        if (recentInvoices.isEmpty()) {
            item {
                EmptyState(
                    title = s.noInvoicesYet,
                    message = s.noInvoicesDesc,
                    icon = Icons.Default.ReceiptLong
                )
            }
        } else {
            items(recentInvoices.take(5)) { itemWithDetails ->
                val inv = itemWithDetails.invoice
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenModal(ModalSheetState.InvoiceDetails(inv.id)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = inv.invoiceNumber,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                InvoiceStatusBadge(status = inv.status, language = language)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = inv.customerName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${s.dueDate}: ${dateFormat.format(Date(inv.dueDate))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatCurrency(inv.grandTotal, inv.currency),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (inv.balanceDue > 0) {
                                Text(
                                    text = "${s.balanceDue}: ${formatCurrency(inv.balanceDue, inv.currency)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = StatusOverdue
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (profile != null) {
                                        val file = PdfGenerator.generateInvoicePdf(context, itemWithDetails, profile, language)
                                        PdfGenerator.sharePdf(context, file, "${s.invoices} ${inv.invoiceNumber}")
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = s.sharePdf,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
