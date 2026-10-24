package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.BusinessProfileEntity
import com.example.data.model.CustomerEntity
import com.example.data.model.InvoiceEntity
import com.example.data.model.InvoiceItemEntity
import com.example.data.model.InvoiceStatus
import com.example.data.model.InvoiceWithDetails
import com.example.data.model.PaymentEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.ProductEntity
import com.example.data.model.QuotationEntity
import com.example.data.model.QuotationItemEntity
import com.example.data.model.QuotationStatus
import com.example.data.model.QuotationWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class InvoiceRepository(private val database: AppDatabase) {

    private val profileDao = database.businessProfileDao()
    private val customerDao = database.customerDao()
    private val productDao = database.productDao()
    private val invoiceDao = database.invoiceDao()
    private val quotationDao = database.quotationDao()
    private val paymentDao = database.paymentDao()

    val businessProfile: Flow<BusinessProfileEntity?> = profileDao.getProfile()
    val allInvoices: Flow<List<InvoiceWithDetails>> = invoiceDao.getAllInvoices()
    val allQuotations: Flow<List<QuotationWithDetails>> = quotationDao.getAllQuotations()
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    val allProducts: Flow<List<ProductEntity>> = productDao.getActiveProducts()
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()

    suspend fun ensureInitialized() {
        val existing = profileDao.getProfile().firstOrNull()
        if (existing == null) {
            val defaultProfile = BusinessProfileEntity()
            profileDao.saveProfile(defaultProfile)

            // Seed initial sample customers
            val c1Id = customerDao.insertCustomer(
                CustomerEntity(
                    name = "Atlas Tech Solutions",
                    contactPerson = "Karim Bennani",
                    email = "karim@atlastech.ma",
                    phone = "+212 6 61 23 45 67",
                    address = "Anfa Club, Casablanca",
                    taxNumber = "ICE: 002341908000088",
                    notes = "Enterprise software client"
                )
            )
            val c2Id = customerDao.insertCustomer(
                CustomerEntity(
                    name = "Marrakech Creative Studio",
                    contactPerson = "Fatima Zahra",
                    email = "fatima@marrakechstudio.com",
                    phone = "+212 6 62 89 01 23",
                    address = "Gueliz, Marrakech",
                    taxNumber = "ICE: 001998721000031",
                    notes = "Design and marketing partner"
                )
            )

            // Seed initial catalog products
            productDao.insertProduct(
                ProductEntity(
                    name = "Mobile App Development",
                    description = "Native Android Kotlin & Jetpack Compose application development",
                    sku = "DEV-APP",
                    unit = "days",
                    unitPrice = 3500.0,
                    taxRate = 20.0
                )
            )
            productDao.insertProduct(
                ProductEntity(
                    name = "UI/UX Design System",
                    description = "Complete mobile design architecture and brand guidelines",
                    sku = "DS-UI",
                    unit = "project",
                    unitPrice = 8000.0,
                    taxRate = 20.0
                )
            )
            productDao.insertProduct(
                ProductEntity(
                    name = "Cloud Server & Maintenance",
                    description = "Monthly infrastructure hosting, monitoring, and backups",
                    sku = "SRV-MNT",
                    unit = "month",
                    unitPrice = 1800.0,
                    taxRate = 20.0
                )
            )

            // Seed initial sample invoice
            val now = System.currentTimeMillis()
            val invNumber = "INV-2026-0001"
            val invoiceId = invoiceDao.insertInvoice(
                InvoiceEntity(
                    invoiceNumber = invNumber,
                    customerId = c1Id,
                    customerName = "Atlas Tech Solutions",
                    customerAddress = "Anfa Club, Casablanca",
                    customerPhone = "+212 6 61 23 45 67",
                    customerEmail = "karim@atlastech.ma",
                    issueDate = now - (10L * 24 * 60 * 60 * 1000),
                    dueDate = now + (20L * 24 * 60 * 60 * 1000),
                    currency = "MAD",
                    subtotal = 15300.0,
                    discountTotal = 0.0,
                    taxTotal = 3060.0,
                    grandTotal = 18360.0,
                    amountPaid = 9000.0,
                    balanceDue = 9360.0,
                    status = InvoiceStatus.PARTIAL,
                    notes = "First milestone completed. Thank you!",
                    terms = defaultProfile.defaultTerms
                )
            )
            invoiceDao.insertInvoiceItems(
                listOf(
                    InvoiceItemEntity(
                        invoiceId = invoiceId,
                        description = "Mobile App Development (Phase 1)",
                        quantity = 3.0,
                        unitPrice = 3500.0,
                        taxRate = 20.0,
                        discount = 0.0,
                        lineTotal = 10500.0
                    ),
                    InvoiceItemEntity(
                        invoiceId = invoiceId,
                        description = "Cloud Server Setup & Maintenance",
                        quantity = 2.0,
                        unitPrice = 2400.0,
                        taxRate = 20.0,
                        discount = 0.0,
                        lineTotal = 4800.0
                    )
                )
            )
            paymentDao.insertPayment(
                PaymentEntity(
                    invoiceId = invoiceId,
                    paymentDate = now - (5L * 24 * 60 * 60 * 1000),
                    amount = 9000.0,
                    paymentMethod = PaymentMethod.BANK_TRANSFER,
                    reference = "VIR-BMCE-9812",
                    notes = "50% upfront deposit"
                )
            )

            // Seed sample quotation
            val quoteNumber = "DEV-2026-0001"
            val quoteId = quotationDao.insertQuotation(
                QuotationEntity(
                    quotationNumber = quoteNumber,
                    customerId = c2Id,
                    customerName = "Marrakech Creative Studio",
                    customerAddress = "Gueliz, Marrakech",
                    customerPhone = "+212 6 62 89 01 23",
                    customerEmail = "fatima@marrakechstudio.com",
                    issueDate = now - (2L * 24 * 60 * 60 * 1000),
                    validUntil = now + (13L * 24 * 60 * 60 * 1000),
                    currency = "MAD",
                    subtotal = 8000.0,
                    discountTotal = 500.0,
                    taxTotal = 1500.0,
                    grandTotal = 9000.0,
                    status = QuotationStatus.SENT,
                    notes = "Includes 3 revision rounds.",
                    terms = "Validity: 15 days."
                )
            )
            quotationDao.insertQuotationItems(
                listOf(
                    QuotationItemEntity(
                        quotationId = quoteId,
                        description = "UI/UX Design System Specification",
                        quantity = 1.0,
                        unitPrice = 8000.0,
                        taxRate = 20.0,
                        discount = 500.0,
                        lineTotal = 7500.0
                    )
                )
            )
        }
    }

    suspend fun generateNextInvoiceNumber(): String {
        val profile = profileDao.getProfile().firstOrNull() ?: BusinessProfileEntity()
        val invoices = invoiceDao.getAllInvoices().firstOrNull() ?: emptyList()
        val count = invoices.size + 1
        return String.format(Locale.US, "%s%04d", profile.invoicePrefix, count)
    }

    suspend fun generateNextQuotationNumber(): String {
        val profile = profileDao.getProfile().firstOrNull() ?: BusinessProfileEntity()
        val quotes = quotationDao.getAllQuotations().firstOrNull() ?: emptyList()
        val count = quotes.size + 1
        return String.format(Locale.US, "%s%04d", profile.quotationPrefix, count)
    }

    suspend fun saveInvoiceWithItems(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>
    ): Long {
        // Calculate totals accurately
        var subtotal = 0.0
        var discountTotal = 0.0
        var taxTotal = 0.0

        val computedItems = items.map { item ->
            val rawLineTotal = item.quantity * item.unitPrice
            val lineDiscount = (rawLineTotal * (item.discount / 100.0)).coerceAtLeast(0.0)
            val taxableAmount = (rawLineTotal - lineDiscount).coerceAtLeast(0.0)
            val lineTax = taxableAmount * (item.taxRate / 100.0)
            val finalLineTotal = taxableAmount

            subtotal += rawLineTotal
            discountTotal += lineDiscount
            taxTotal += lineTax

            item.copy(lineTotal = round(finalLineTotal))
        }

        val grandTotal = round(subtotal - discountTotal + taxTotal)
        val balanceDue = round((grandTotal - invoice.amountPaid).coerceAtLeast(0.0))

        val computedStatus = when {
            invoice.status == InvoiceStatus.CANCELLED -> InvoiceStatus.CANCELLED
            invoice.status == InvoiceStatus.DRAFT -> InvoiceStatus.DRAFT
            balanceDue <= 0.001 && invoice.amountPaid > 0 -> InvoiceStatus.PAID
            invoice.amountPaid > 0 -> InvoiceStatus.PARTIAL
            System.currentTimeMillis() > invoice.dueDate -> InvoiceStatus.OVERDUE
            else -> InvoiceStatus.ISSUED
        }

        val finalInvoice = invoice.copy(
            subtotal = round(subtotal),
            discountTotal = round(discountTotal),
            taxTotal = round(taxTotal),
            grandTotal = grandTotal,
            balanceDue = balanceDue,
            status = computedStatus
        )

        val invoiceId = if (finalInvoice.id > 0) {
            invoiceDao.updateInvoice(finalInvoice)
            invoiceDao.deleteItemsForInvoice(finalInvoice.id)
            finalInvoice.id
        } else {
            invoiceDao.insertInvoice(finalInvoice)
        }

        val itemsWithId = computedItems.map { it.copy(invoiceId = invoiceId, id = 0) }
        invoiceDao.insertInvoiceItems(itemsWithId)
        return invoiceId
    }

    suspend fun saveQuotationWithItems(
        quotation: QuotationEntity,
        items: List<QuotationItemEntity>
    ): Long {
        var subtotal = 0.0
        var discountTotal = 0.0
        var taxTotal = 0.0

        val computedItems = items.map { item ->
            val rawLineTotal = item.quantity * item.unitPrice
            val lineDiscount = (rawLineTotal * (item.discount / 100.0)).coerceAtLeast(0.0)
            val taxableAmount = (rawLineTotal - lineDiscount).coerceAtLeast(0.0)
            val lineTax = taxableAmount * (item.taxRate / 100.0)
            val finalLineTotal = taxableAmount

            subtotal += rawLineTotal
            discountTotal += lineDiscount
            taxTotal += lineTax

            item.copy(lineTotal = round(finalLineTotal))
        }

        val grandTotal = round(subtotal - discountTotal + taxTotal)
        val finalQuotation = quotation.copy(
            subtotal = round(subtotal),
            discountTotal = round(discountTotal),
            taxTotal = round(taxTotal),
            grandTotal = grandTotal
        )

        val quoteId = if (finalQuotation.id > 0) {
            quotationDao.updateQuotation(finalQuotation)
            quotationDao.deleteItemsForQuotation(finalQuotation.id)
            finalQuotation.id
        } else {
            quotationDao.insertQuotation(finalQuotation)
        }

        val itemsWithId = computedItems.map { it.copy(quotationId = quoteId, id = 0) }
        quotationDao.insertQuotationItems(itemsWithId)
        return quoteId
    }

    suspend fun convertQuotationToInvoice(quotationWithDetails: QuotationWithDetails): Long {
        val quotation = quotationWithDetails.quotation
        val newInvoiceNumber = generateNextInvoiceNumber()

        val invoice = InvoiceEntity(
            invoiceNumber = newInvoiceNumber,
            customerId = quotation.customerId,
            customerName = quotation.customerName,
            customerAddress = quotation.customerAddress,
            customerPhone = quotation.customerPhone,
            customerEmail = quotation.customerEmail,
            issueDate = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
            currency = quotation.currency,
            subtotal = quotation.subtotal,
            discountTotal = quotation.discountTotal,
            taxTotal = quotation.taxTotal,
            grandTotal = quotation.grandTotal,
            amountPaid = 0.0,
            balanceDue = quotation.grandTotal,
            status = InvoiceStatus.ISSUED,
            notes = quotation.notes,
            terms = quotation.terms
        )

        val invoiceItems = quotationWithDetails.items.map { qItem ->
            InvoiceItemEntity(
                description = qItem.description,
                quantity = qItem.quantity,
                unitPrice = qItem.unitPrice,
                taxRate = qItem.taxRate,
                discount = qItem.discount,
                lineTotal = qItem.lineTotal
            )
        }

        val newInvoiceId = saveInvoiceWithItems(invoice, invoiceItems)

        // Mark quotation as converted
        quotationDao.updateQuotation(
            quotation.copy(
                status = QuotationStatus.CONVERTED,
                convertedInvoiceId = newInvoiceId
            )
        )

        return newInvoiceId
    }

    suspend fun recordPayment(
        invoiceWithDetails: InvoiceWithDetails,
        amount: Double,
        paymentMethod: PaymentMethod,
        reference: String,
        notes: String
    ): Long {
        val invoice = invoiceWithDetails.invoice
        val payment = PaymentEntity(
            invoiceId = invoice.id,
            paymentDate = System.currentTimeMillis(),
            amount = round(amount),
            paymentMethod = paymentMethod,
            reference = reference,
            notes = notes
        )
        val paymentId = paymentDao.insertPayment(payment)

        val newAmountPaid = round(invoice.amountPaid + amount)
        val newBalanceDue = round((invoice.grandTotal - newAmountPaid).coerceAtLeast(0.0))
        val newStatus = if (newBalanceDue <= 0.001) {
            InvoiceStatus.PAID
        } else {
            InvoiceStatus.PARTIAL
        }

        invoiceDao.updateInvoice(
            invoice.copy(
                amountPaid = newAmountPaid,
                balanceDue = newBalanceDue,
                status = newStatus
            )
        )

        return paymentId
    }

    suspend fun deleteInvoice(invoice: InvoiceEntity) {
        invoiceDao.deleteInvoice(invoice)
    }

    suspend fun deleteQuotation(quotation: QuotationEntity) {
        quotationDao.deleteQuotation(quotation)
    }

    suspend fun saveCustomer(customer: CustomerEntity): Long {
        return customerDao.insertCustomer(customer)
    }

    suspend fun deleteCustomer(customer: CustomerEntity) {
        customerDao.deleteCustomer(customer)
    }

    suspend fun saveProduct(product: ProductEntity): Long {
        return productDao.insertProduct(product)
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
    }

    suspend fun saveBusinessProfile(profile: BusinessProfileEntity) {
        profileDao.saveProfile(profile)
    }

    private fun round(value: Double): Double {
        return (value * 100.0).roundToInt() / 100.0
    }
}
