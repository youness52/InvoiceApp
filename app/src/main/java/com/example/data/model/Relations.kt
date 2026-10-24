package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class InvoiceWithDetails(
    @Embedded val invoice: InvoiceEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<InvoiceItemEntity> = emptyList(),
    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val payments: List<PaymentEntity> = emptyList()
)

data class QuotationWithDetails(
    @Embedded val quotation: QuotationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "quotationId"
    )
    val items: List<QuotationItemEntity> = emptyList()
)
