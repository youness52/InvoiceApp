package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import java.io.File

enum class NavTab(val defaultTitle: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    INVOICES("Invoices", Icons.Default.ReceiptLong),
    QUOTATIONS("Quotations", Icons.Default.Description),
    CUSTOMERS("Customers", Icons.Default.People),
    PRODUCTS("Products", Icons.Default.Inventory2),
    SETTINGS("Settings", Icons.Default.Settings);

    fun getTitle(strings: com.example.ui.i18n.StringsDefinition): String {
        return when (this) {
            DASHBOARD -> strings.dashboard
            INVOICES -> strings.invoices
            QUOTATIONS -> strings.quotations
            CUSTOMERS -> strings.customers
            PRODUCTS -> strings.products
            SETTINGS -> strings.settings
        }
    }
}

sealed class ModalSheetState {
    object None : ModalSheetState()
    object CreateInvoice : ModalSheetState()
    object CreateQuotation : ModalSheetState()
    data class InvoiceDetails(val invoiceId: Long) : ModalSheetState()
    data class QuotationDetails(val quotationId: Long) : ModalSheetState()
    data class RecordPayment(val invoiceId: Long) : ModalSheetState()
    data class EditCustomer(val customerId: Long? = null) : ModalSheetState()
    data class EditProduct(val productId: Long? = null) : ModalSheetState()
    data class PdfPreview(val file: File, val title: String) : ModalSheetState()
}
