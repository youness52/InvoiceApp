package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class InvoiceStatus {
    DRAFT,
    ISSUED,
    PARTIAL,
    PAID,
    OVERDUE,
    CANCELLED
}

enum class QuotationStatus {
    DRAFT,
    SENT,
    ACCEPTED,
    REJECTED,
    CONVERTED
}

enum class PaymentMethod {
    CASH,
    BANK_TRANSFER,
    CHECK,
    CARD,
    OTHER
}

@Entity(tableName = "business_profile")
data class BusinessProfileEntity(
    @PrimaryKey val id: Int = 1,
    val businessName: String = "Atlas Solutions SARL",
    val email: String = "contact@atlas-solutions.ma",
    val phone: String = "+212 5 22 12 34 56",
    val address: String = "120 Boulevard Zerktouni, Casablanca",
    val taxId: String = "ICE: 001524389000042 - IF: 40291823",
    val currencyCode: String = "MAD",
    val invoicePrefix: String = "INV-2026-",
    val quotationPrefix: String = "DEV-2026-",
    val defaultTerms: String = "Payment due within 30 days of issue date. Bank transfer preferred.",
    val defaultNotes: String = "Thank you for your business!"
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val contactPerson: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val taxNumber: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val sku: String = "",
    val unit: String = "unit", // unit, hrs, days, m²
    val unitPrice: Double = 0.0,
    val taxRate: Double = 20.0, // Standard 20% TVA in Morocco or custom
    val isArchived: Boolean = false
)

@Entity(
    tableName = "invoices",
    indices = [Index(value = ["invoiceNumber"], unique = true)]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long,
    val customerName: String,
    val customerAddress: String = "",
    val customerPhone: String = "",
    val customerEmail: String = "",
    val issueDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
    val currency: String = "MAD",
    val subtotal: Double = 0.0,
    val discountTotal: Double = 0.0,
    val taxTotal: Double = 0.0,
    val grandTotal: Double = 0.0,
    val amountPaid: Double = 0.0,
    val balanceDue: Double = 0.0,
    val status: InvoiceStatus = InvoiceStatus.ISSUED,
    val notes: String = "",
    val terms: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("invoiceId")]
)
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long = 0,
    val description: String,
    val quantity: Double = 1.0,
    val unitPrice: Double = 0.0,
    val taxRate: Double = 20.0,
    val discount: Double = 0.0,
    val lineTotal: Double = 0.0
)

@Entity(
    tableName = "quotations",
    indices = [Index(value = ["quotationNumber"], unique = true)]
)
data class QuotationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quotationNumber: String,
    val customerId: Long,
    val customerName: String,
    val customerAddress: String = "",
    val customerPhone: String = "",
    val customerEmail: String = "",
    val issueDate: Long = System.currentTimeMillis(),
    val validUntil: Long = System.currentTimeMillis() + (15L * 24 * 60 * 60 * 1000),
    val currency: String = "MAD",
    val subtotal: Double = 0.0,
    val discountTotal: Double = 0.0,
    val taxTotal: Double = 0.0,
    val grandTotal: Double = 0.0,
    val status: QuotationStatus = QuotationStatus.DRAFT,
    val convertedInvoiceId: Long? = null,
    val notes: String = "",
    val terms: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "quotation_items",
    foreignKeys = [
        ForeignKey(
            entity = QuotationEntity::class,
            parentColumns = ["id"],
            childColumns = ["quotationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("quotationId")]
)
data class QuotationItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quotationId: Long = 0,
    val description: String,
    val quantity: Double = 1.0,
    val unitPrice: Double = 0.0,
    val taxRate: Double = 20.0,
    val discount: Double = 0.0,
    val lineTotal: Double = 0.0
)

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("invoiceId")]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val paymentDate: Long = System.currentTimeMillis(),
    val amount: Double,
    val paymentMethod: PaymentMethod = PaymentMethod.BANK_TRANSFER,
    val reference: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
