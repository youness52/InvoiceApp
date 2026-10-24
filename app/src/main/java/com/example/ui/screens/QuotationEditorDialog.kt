package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BusinessProfileEntity
import com.example.data.model.CustomerEntity
import com.example.data.model.ProductEntity
import com.example.data.model.QuotationEntity
import com.example.data.model.QuotationItemEntity
import com.example.data.model.QuotationStatus
import com.example.ui.components.formatCurrency
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StatusOverdue
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotationEditorDialog(
    profile: BusinessProfileEntity?,
    customers: List<CustomerEntity>,
    products: List<ProductEntity>,
    initialQuotationNumber: String,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (QuotationEntity, List<QuotationItemEntity>) -> Unit
) {
    val context = LocalContext.current
    val s = AppStrings[language]
    val currency = profile?.currencyCode ?: "MAD"

    var quotationNumber by remember { mutableStateOf(initialQuotationNumber) }
    var selectedCustomer by remember { mutableStateOf(customers.firstOrNull()) }
    var customerName by remember { mutableStateOf(selectedCustomer?.name ?: "") }
    var customerAddress by remember { mutableStateOf(selectedCustomer?.address ?: "") }
    var customerPhone by remember { mutableStateOf(selectedCustomer?.phone ?: "") }
    var customerEmail by remember { mutableStateOf(selectedCustomer?.email ?: "") }

    var notes by remember {
        mutableStateOf(if (language == AppLanguage.AR) "عرض صالح لمدة 15 يوماً من تاريخ الإصدار." else "Valable 15 jours à compter de la date d'émission.")
    }
    var terms by remember {
        mutableStateOf(if (language == AppLanguage.AR) "طريقة الدفع: 50% تسبقة، 50% عند التسليم." else "Règlement : 50% à la commande, 50% à la livraison.")
    }

    var customerDropdownExpanded by remember { mutableStateOf(false) }

    data class TempItem(
        var description: String = "",
        var quantity: String = "1",
        var unitPrice: String = "0",
        var taxRate: String = "20",
        var discount: String = "0"
    )

    val lineItems = remember {
        mutableStateListOf(
            TempItem(
                description = if (language == AppLanguage.AR) "مقترح خدمات وتطوير" else "Proposition de prestation / service",
                quantity = "1",
                unitPrice = "3000",
                taxRate = "20",
                discount = "0"
            )
        )
    }

    var subtotal = 0.0
    var discountTotal = 0.0
    var taxTotal = 0.0

    lineItems.forEach { item ->
        val q = item.quantity.toDoubleOrNull() ?: 0.0
        val p = item.unitPrice.toDoubleOrNull() ?: 0.0
        val t = item.taxRate.toDoubleOrNull() ?: 0.0
        val d = item.discount.toDoubleOrNull() ?: 0.0

        val lineRaw = q * p
        val lineDisc = (lineRaw * (d / 100.0)).coerceAtLeast(0.0)
        val taxable = (lineRaw - lineDisc).coerceAtLeast(0.0)
        val lineTax = taxable * (t / 100.0)

        subtotal += lineRaw
        discountTotal += lineDisc
        taxTotal += lineTax
    }

    val grandTotal = (subtotal - discountTotal + taxTotal).coerceAtLeast(0.0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryBlue)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = s.createNewQuotation,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = s.cancel, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = quotationNumber,
                            onValueChange = { quotationNumber = it },
                            label = { Text(s.quotationNumber) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = s.clientInfo,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )

                                ExposedDropdownMenuBox(
                                    expanded = customerDropdownExpanded,
                                    onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = customerName,
                                        onValueChange = {
                                            customerName = it
                                            selectedCustomer = null
                                        },
                                        label = { Text(s.clientName) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    if (customers.isNotEmpty()) {
                                        ExposedDropdownMenu(
                                            expanded = customerDropdownExpanded,
                                            onDismissRequest = { customerDropdownExpanded = false }
                                        ) {
                                            customers.forEach { cust ->
                                                DropdownMenuItem(
                                                    text = { Text(cust.name) },
                                                    onClick = {
                                                        selectedCustomer = cust
                                                        customerName = cust.name
                                                        customerAddress = cust.address
                                                        customerPhone = cust.phone
                                                        customerEmail = cust.email
                                                        customerDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = customerAddress,
                                    onValueChange = { customerAddress = it },
                                    label = { Text(s.billingAddress) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = s.lineItems,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Button(
                                onClick = {
                                    lineItems.add(TempItem(description = "", quantity = "1", unitPrice = "0", taxRate = "20", discount = "0"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(s.addProduct, fontSize = 12.sp)
                            }
                        }
                    }

                    itemsIndexed(lineItems) { index, item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#${index + 1}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = PrimaryBlue
                                    )
                                    if (lineItems.size > 1) {
                                        IconButton(
                                            onClick = { lineItems.removeAt(index) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = s.delete, tint = StatusOverdue, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = item.description,
                                    onValueChange = { lineItems[index] = item.copy(description = it) },
                                    label = { Text(s.description) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = item.quantity,
                                        onValueChange = { lineItems[index] = item.copy(quantity = it) },
                                        label = { Text(s.quantity) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    OutlinedTextField(
                                        value = item.unitPrice,
                                        onValueChange = { lineItems[index] = item.copy(unitPrice = it) },
                                        label = { Text("${s.unitPrice} ($currency)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    OutlinedTextField(
                                        value = item.taxRate,
                                        onValueChange = { lineItems[index] = item.copy(taxRate = it) },
                                        label = { Text(s.taxRate) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${s.subtotal}:", style = MaterialTheme.typography.bodyMedium)
                                    Text(formatCurrency(subtotal, currency), style = MaterialTheme.typography.bodyMedium)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${s.taxTva}:", style = MaterialTheme.typography.bodyMedium)
                                    Text(formatCurrency(taxTotal, currency), style = MaterialTheme.typography.bodyMedium)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${s.estimatedTotal}:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(
                                        formatCurrency(grandTotal, currency),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text(s.notesInstructions) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(s.cancel)
                        }

                        Button(
                            onClick = {
                                if (customerName.isBlank()) {
                                    Toast.makeText(context, s.clientName, Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val itemsEntities = lineItems.map { tItem ->
                                    val q = tItem.quantity.toDoubleOrNull() ?: 1.0
                                    val p = tItem.unitPrice.toDoubleOrNull() ?: 0.0
                                    val t = tItem.taxRate.toDoubleOrNull() ?: 20.0
                                    val d = tItem.discount.toDoubleOrNull() ?: 0.0
                                    val raw = q * p
                                    val disc = raw * (d / 100.0)
                                    val lineTot = raw - disc

                                    QuotationItemEntity(
                                        description = tItem.description.ifBlank { "Article" },
                                        quantity = q,
                                        unitPrice = p,
                                        taxRate = t,
                                        discount = d,
                                        lineTotal = (lineTot * 100.0).roundToInt() / 100.0
                                    )
                                }

                                val now = System.currentTimeMillis()
                                val quote = QuotationEntity(
                                    quotationNumber = quotationNumber,
                                    customerId = selectedCustomer?.id ?: 0L,
                                    customerName = customerName,
                                    customerAddress = customerAddress,
                                    customerPhone = customerPhone,
                                    customerEmail = customerEmail,
                                    issueDate = now,
                                    validUntil = now + (15L * 24 * 60 * 60 * 1000),
                                    currency = currency,
                                    status = QuotationStatus.SENT,
                                    notes = notes,
                                    terms = terms
                                )
                                onSave(quote, itemsEntities)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text(s.saveQuotation, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
