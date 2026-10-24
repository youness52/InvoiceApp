package com.example.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.BusinessProfileEntity
import com.example.data.model.InvoiceWithDetails
import com.example.data.model.QuotationWithDetails
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard width in points
    private const val PAGE_HEIGHT = 842 // A4 standard height in points
    private const val MARGIN = 40f

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private data class PdfText(
        val titleInvoice: String,
        val titleQuotation: String,
        val numberPrefix: String,
        val statusPrefix: String,
        val issueDate: String,
        val dueDate: String,
        val validUntil: String,
        val from: String,
        val to: String,
        val desc: String,
        val qty: String,
        val price: String,
        val tax: String,
        val total: String,
        val subtotal: String,
        val discount: String,
        val taxTotal: String,
        val grandTotal: String,
        val estimatedTotal: String,
        val amountPaid: String,
        val balanceDue: String,
        val notes: String,
        val terms: String,
        val thankYou: String,
        val footerGen: String
    )

    private fun getPdfLabels(language: AppLanguage): PdfText {
        return when (language) {
            AppLanguage.FR -> PdfText(
                titleInvoice = "FACTURE",
                titleQuotation = "DEVIS",
                numberPrefix = "N°",
                statusPrefix = "STATUT",
                issueDate = "Date d'émission",
                dueDate = "Date d'échéance",
                validUntil = "Valable jusqu'au",
                from = "DE (ÉMETTEUR):",
                to = "FACTURÉ À (CLIENT):",
                desc = "DESCRIPTION",
                qty = "QTÉ",
                price = "PRIX",
                tax = "TVA %",
                total = "TOTAL",
                subtotal = "Sous-total:",
                discount = "Remise:",
                taxTotal = "Total TVA:",
                grandTotal = "TOTAL À PAYER:",
                estimatedTotal = "TOTAL ESTIMÉ:",
                amountPaid = "Montant Payé:",
                balanceDue = "RESTE À PAYER:",
                notes = "NOTES:",
                terms = "CONDITIONS GÉNÉRALES:",
                thankYou = "Merci pour votre confiance !",
                footerGen = "Généré via Gestionnaire de Factures"
            )
            AppLanguage.AR -> PdfText(
                titleInvoice = "فاتورة",
                titleQuotation = "عرض أسعار",
                numberPrefix = "رقم",
                statusPrefix = "الحالة",
                issueDate = "تاريخ الإصدار",
                dueDate = "تاريخ الاستحقاق",
                validUntil = "صالح حتى",
                from = "من (المصدر):",
                to = "إلى (العميل):",
                desc = "الوصف",
                qty = "الكمية",
                price = "السعر",
                tax = "الضريبة %",
                total = "المجموع",
                subtotal = "المجموع الفرعي:",
                discount = "الخصم:",
                taxTotal = "إجمالي الضريبة:",
                grandTotal = "المجموع الإجمالي:",
                estimatedTotal = "المجموع التقديري:",
                amountPaid = "المبلغ المدفوع:",
                balanceDue = "المتبقي للدفع:",
                notes = "ملاحظات:",
                terms = "الشروط والأحكام:",
                thankYou = "شكراً لتعاملكم معنا !",
                footerGen = "تم الإنشاء عبر إدارة الفواتير"
            )
            AppLanguage.EN -> PdfText(
                titleInvoice = "INVOICE",
                titleQuotation = "QUOTATION",
                numberPrefix = "No",
                statusPrefix = "STATUS",
                issueDate = "Issue Date",
                dueDate = "Due Date",
                validUntil = "Valid Until",
                from = "FROM (ISSUED BY):",
                to = "BILLED TO (CUSTOMER):",
                desc = "DESCRIPTION",
                qty = "QTY",
                price = "PRICE",
                tax = "TAX %",
                total = "TOTAL",
                subtotal = "Subtotal:",
                discount = "Discount:",
                taxTotal = "Tax Total (VAT):",
                grandTotal = "TOTAL AMOUNT:",
                estimatedTotal = "ESTIMATED TOTAL:",
                amountPaid = "Amount Paid:",
                balanceDue = "BALANCE DUE:",
                notes = "NOTES:",
                terms = "TERMS & CONDITIONS:",
                thankYou = "Thank you for your business!",
                footerGen = "Generated via Invoice Manager"
            )
        }
    }

    fun generateInvoicePdf(
        context: Context,
        invoiceWithDetails: InvoiceWithDetails,
        profile: BusinessProfileEntity,
        language: AppLanguage = AppLanguage.FR
    ): File {
        val invoice = invoiceWithDetails.invoice
        val items = invoiceWithDetails.items
        val lbl = getPdfLabels(language)

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val primaryPaint = Paint().apply {
            color = Color.parseColor("#1E40AF")
            isAntiAlias = true
        }
        val textPrimaryPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            isAntiAlias = true
            textSize = 10f
        }
        val textSecondaryPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            isAntiAlias = true
            textSize = 9f
        }
        val boldPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            isAntiAlias = true
            isFakeBoldText = true
            textSize = 10f
        }
        val whiteTextPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textSize = 9.5f
            isFakeBoldText = true
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
        }
        val tableBgPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
        }

        // Top Header Accent Bar
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 10f, primaryPaint)

        // Title & Number
        var currentY = 55f
        boldPaint.textSize = 22f
        boldPaint.color = Color.parseColor("#1E40AF")
        canvas.drawText(lbl.titleInvoice, MARGIN, currentY, boldPaint)

        boldPaint.textSize = 11f
        boldPaint.color = Color.parseColor("#0F172A")
        val invNoText = "${lbl.numberPrefix}: ${invoice.invoiceNumber}"
        val invNoWidth = boldPaint.measureText(invNoText)
        canvas.drawText(invNoText, PAGE_WIDTH - MARGIN - invNoWidth, currentY, boldPaint)

        currentY += 18f
        val statusText = "${lbl.statusPrefix}: ${AppStrings.getStatusLabel(invoice.status, language)}"
        textSecondaryPaint.textSize = 9f
        val statusWidth = textSecondaryPaint.measureText(statusText)
        canvas.drawText(statusText, PAGE_WIDTH - MARGIN - statusWidth, currentY, textSecondaryPaint)

        val dateText = "${lbl.issueDate}: ${dateFormat.format(Date(invoice.issueDate))}"
        canvas.drawText(dateText, MARGIN, currentY, textSecondaryPaint)

        currentY += 14f
        val dueText = "${lbl.dueDate}: ${dateFormat.format(Date(invoice.dueDate))}"
        canvas.drawText(dueText, MARGIN, currentY, textSecondaryPaint)

        currentY += 20f
        canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)

        currentY += 24f
        boldPaint.textSize = 10f
        canvas.drawText(lbl.from, MARGIN, currentY, boldPaint)
        canvas.drawText(lbl.to, PAGE_WIDTH / 2f + 10f, currentY, boldPaint)

        currentY += 16f
        boldPaint.textSize = 11f
        canvas.drawText(profile.businessName, MARGIN, currentY, boldPaint)
        canvas.drawText(invoice.customerName, PAGE_WIDTH / 2f + 10f, currentY, boldPaint)

        currentY += 14f
        if (profile.address.isNotEmpty()) canvas.drawText(profile.address, MARGIN, currentY, textSecondaryPaint)
        if (invoice.customerAddress.isNotEmpty()) canvas.drawText(invoice.customerAddress, PAGE_WIDTH / 2f + 10f, currentY, textSecondaryPaint)

        currentY += 13f
        if (profile.phone.isNotEmpty()) canvas.drawText("Tél: ${profile.phone}", MARGIN, currentY, textSecondaryPaint)
        if (invoice.customerPhone.isNotEmpty()) canvas.drawText("Tél: ${invoice.customerPhone}", PAGE_WIDTH / 2f + 10f, currentY, textSecondaryPaint)

        currentY += 13f
        if (profile.email.isNotEmpty()) canvas.drawText(profile.email, MARGIN, currentY, textSecondaryPaint)
        if (invoice.customerEmail.isNotEmpty()) canvas.drawText(invoice.customerEmail, PAGE_WIDTH / 2f + 10f, currentY, textSecondaryPaint)

        currentY += 13f
        if (profile.taxId.isNotEmpty()) canvas.drawText(profile.taxId, MARGIN, currentY, textSecondaryPaint)

        currentY += 30f
        val tableTop = currentY
        val tableHeight = 22f
        canvas.drawRoundRect(RectF(MARGIN, tableTop, PAGE_WIDTH - MARGIN, tableTop + tableHeight), 4f, 4f, primaryPaint)

        val colDesc = MARGIN + 8f
        val colQty = MARGIN + 260f
        val colPrice = MARGIN + 320f
        val colTax = MARGIN + 395f
        val colTotal = PAGE_WIDTH - MARGIN - 60f

        canvas.drawText(lbl.desc, colDesc, tableTop + 15f, whiteTextPaint)
        canvas.drawText(lbl.qty, colQty, tableTop + 15f, whiteTextPaint)
        canvas.drawText(lbl.price, colPrice, tableTop + 15f, whiteTextPaint)
        canvas.drawText(lbl.tax, colTax, tableTop + 15f, whiteTextPaint)
        canvas.drawText(lbl.total, colTotal, tableTop + 15f, whiteTextPaint)

        currentY = tableTop + tableHeight

        items.forEachIndexed { index, item ->
            val rowHeight = 24f
            if (index % 2 == 1) {
                canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + rowHeight, tableBgPaint)
            }
            canvas.drawLine(MARGIN, currentY + rowHeight, PAGE_WIDTH - MARGIN, currentY + rowHeight, linePaint)

            val textY = currentY + 16f
            val desc = if (item.description.length > 40) item.description.take(38) + "..." else item.description
            canvas.drawText(desc, colDesc, textY, textPrimaryPaint)
            canvas.drawText(String.format(Locale.US, "%.1f", item.quantity), colQty, textY, textPrimaryPaint)
            canvas.drawText(String.format(Locale.US, "%.2f", item.unitPrice), colPrice, textY, textPrimaryPaint)
            canvas.drawText("${item.taxRate.toInt()}%", colTax, textY, textSecondaryPaint)
            canvas.drawText(String.format(Locale.US, "%.2f", item.lineTotal), colTotal, textY, boldPaint)

            currentY += rowHeight
        }

        currentY += 20f
        val summaryLeft = PAGE_WIDTH - MARGIN - 210f
        val summaryValueRight = PAGE_WIDTH - MARGIN - 8f

        fun drawSummaryRow(label: String, value: String, isBold: Boolean = false, isAccent: Boolean = false) {
            val pLabel = if (isBold) boldPaint else textSecondaryPaint
            val pVal = if (isBold) boldPaint else textPrimaryPaint

            if (isAccent) {
                canvas.drawRoundRect(RectF(summaryLeft - 8f, currentY - 14f, PAGE_WIDTH - MARGIN, currentY + 8f), 4f, 4f, primaryPaint)
                canvas.drawText(label, summaryLeft, currentY, whiteTextPaint)
                val valWidth = whiteTextPaint.measureText(value)
                canvas.drawText(value, summaryValueRight - valWidth, currentY, whiteTextPaint)
            } else {
                canvas.drawText(label, summaryLeft, currentY, pLabel)
                val valWidth = pVal.measureText(value)
                canvas.drawText(value, summaryValueRight - valWidth, currentY, pVal)
            }
            currentY += 18f
        }

        drawSummaryRow(lbl.subtotal, String.format(Locale.US, "%.2f %s", invoice.subtotal, invoice.currency))
        if (invoice.discountTotal > 0) {
            drawSummaryRow(lbl.discount, String.format(Locale.US, "-%.2f %s", invoice.discountTotal, invoice.currency))
        }
        drawSummaryRow(lbl.taxTotal, String.format(Locale.US, "%.2f %s", invoice.taxTotal, invoice.currency))
        drawSummaryRow(lbl.grandTotal, String.format(Locale.US, "%.2f %s", invoice.grandTotal, invoice.currency), isBold = true, isAccent = true)
        currentY += 4f
        drawSummaryRow(lbl.amountPaid, String.format(Locale.US, "%.2f %s", invoice.amountPaid, invoice.currency), isBold = true)
        drawSummaryRow(lbl.balanceDue, String.format(Locale.US, "%.2f %s", invoice.balanceDue, invoice.currency), isBold = true)

        var notesY = currentY - 100f
        if (invoice.notes.isNotEmpty()) {
            boldPaint.textSize = 9.5f
            canvas.drawText(lbl.notes, MARGIN, notesY, boldPaint)
            notesY += 13f
            canvas.drawText(invoice.notes, MARGIN, notesY, textSecondaryPaint)
            notesY += 18f
        }
        if (invoice.terms.isNotEmpty()) {
            boldPaint.textSize = 9.5f
            canvas.drawText(lbl.terms, MARGIN, notesY, boldPaint)
            notesY += 13f
            val terms = if (invoice.terms.length > 70) invoice.terms.take(68) + "..." else invoice.terms
            canvas.drawText(terms, MARGIN, notesY, textSecondaryPaint)
        }

        val footerY = PAGE_HEIGHT - 35f
        canvas.drawLine(MARGIN, footerY, PAGE_WIDTH - MARGIN, footerY, linePaint)
        textSecondaryPaint.textSize = 8f
        canvas.drawText(lbl.thankYou, MARGIN, footerY + 16f, textSecondaryPaint)
        val generatedAt = "Page 1/1 • ${lbl.footerGen}"
        val genWidth = textSecondaryPaint.measureText(generatedAt)
        canvas.drawText(generatedAt, PAGE_WIDTH - MARGIN - genWidth, footerY + 16f, textSecondaryPaint)

        document.finishPage(page)

        val outDir = File(context.cacheDir, "invoices")
        if (!outDir.exists()) outDir.mkdirs()
        val file = File(outDir, "${invoice.invoiceNumber}.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    fun generateQuotationPdf(
        context: Context,
        quotationWithDetails: QuotationWithDetails,
        profile: BusinessProfileEntity,
        language: AppLanguage = AppLanguage.FR
    ): File {
        val quote = quotationWithDetails.quotation
        val items = quotationWithDetails.items
        val lbl = getPdfLabels(language)

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val primaryPaint = Paint().apply {
            color = Color.parseColor("#0284C7")
            isAntiAlias = true
        }
        val textPrimaryPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            isAntiAlias = true
            textSize = 10f
        }
        val textSecondaryPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            isAntiAlias = true
            textSize = 9f
        }
        val boldPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            isAntiAlias = true
            isFakeBoldText = true
            textSize = 10f
        }
        val whiteTextPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textSize = 9.5f
            isFakeBoldText = true
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
        }

        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 10f, primaryPaint)

        var currentY = 55f
        boldPaint.textSize = 22f
        boldPaint.color = Color.parseColor("#0284C7")
        canvas.drawText(lbl.titleQuotation, MARGIN, currentY, boldPaint)

        boldPaint.textSize = 11f
        boldPaint.color = Color.parseColor("#0F172A")
        val quoteNoText = "${lbl.numberPrefix}: ${quote.quotationNumber}"
        val quoteNoWidth = boldPaint.measureText(quoteNoText)
        canvas.drawText(quoteNoText, PAGE_WIDTH - MARGIN - quoteNoWidth, currentY, boldPaint)

        currentY += 18f
        val statusText = "${lbl.statusPrefix}: ${AppStrings.getQuotationStatusLabel(quote.status, language)}"
        val statusWidth = textSecondaryPaint.measureText(statusText)
        canvas.drawText(statusText, PAGE_WIDTH - MARGIN - statusWidth, currentY, textSecondaryPaint)

        canvas.drawText("${lbl.issueDate}: ${dateFormat.format(Date(quote.issueDate))}", MARGIN, currentY, textSecondaryPaint)
        currentY += 14f
        canvas.drawText("${lbl.validUntil}: ${dateFormat.format(Date(quote.validUntil))}", MARGIN, currentY, textSecondaryPaint)

        currentY += 20f
        canvas.drawLine(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY, linePaint)

        currentY += 24f
        boldPaint.textSize = 10f
        canvas.drawText(lbl.from, MARGIN, currentY, boldPaint)
        canvas.drawText(lbl.to, PAGE_WIDTH / 2f + 10f, currentY, boldPaint)

        currentY += 16f
        boldPaint.textSize = 11f
        canvas.drawText(profile.businessName, MARGIN, currentY, boldPaint)
        canvas.drawText(quote.customerName, PAGE_WIDTH / 2f + 10f, currentY, boldPaint)

        currentY += 14f
        if (profile.address.isNotEmpty()) canvas.drawText(profile.address, MARGIN, currentY, textSecondaryPaint)
        if (quote.customerAddress.isNotEmpty()) canvas.drawText(quote.customerAddress, PAGE_WIDTH / 2f + 10f, currentY, textSecondaryPaint)

        currentY += 13f
        if (profile.phone.isNotEmpty()) canvas.drawText("Tél: ${profile.phone}", MARGIN, currentY, textSecondaryPaint)
        if (quote.customerPhone.isNotEmpty()) canvas.drawText("Tél: ${quote.customerPhone}", PAGE_WIDTH / 2f + 10f, currentY, textSecondaryPaint)

        currentY += 30f
        val tableTop = currentY
        val tableHeight = 22f
        canvas.drawRoundRect(RectF(MARGIN, tableTop, PAGE_WIDTH - MARGIN, tableTop + tableHeight), 4f, 4f, primaryPaint)

        val colDesc = MARGIN + 8f
        val colQty = MARGIN + 260f
        val colPrice = MARGIN + 320f
        val colTax = MARGIN + 395f
        val colTotal = PAGE_WIDTH - MARGIN - 60f

        canvas.drawText(lbl.desc, colDesc, tableTop + 15f, whiteTextPaint)
        canvas.drawText(lbl.qty, colQty, tableTop + 15f, whiteTextPaint)
        canvas.drawText(lbl.price, colPrice, tableTop + 15f, whiteTextPaint)
        canvas.drawText(lbl.tax, colTax, tableTop + 15f, whiteTextPaint)
        canvas.drawText(lbl.total, colTotal, tableTop + 15f, whiteTextPaint)

        currentY = tableTop + tableHeight

        items.forEachIndexed { _, item ->
            val rowHeight = 24f
            canvas.drawLine(MARGIN, currentY + rowHeight, PAGE_WIDTH - MARGIN, currentY + rowHeight, linePaint)
            val textY = currentY + 16f
            val desc = if (item.description.length > 40) item.description.take(38) + "..." else item.description
            canvas.drawText(desc, colDesc, textY, textPrimaryPaint)
            canvas.drawText(String.format(Locale.US, "%.1f", item.quantity), colQty, textY, textPrimaryPaint)
            canvas.drawText(String.format(Locale.US, "%.2f", item.unitPrice), colPrice, textY, textPrimaryPaint)
            canvas.drawText("${item.taxRate.toInt()}%", colTax, textY, textSecondaryPaint)
            canvas.drawText(String.format(Locale.US, "%.2f", item.lineTotal), colTotal, textY, boldPaint)
            currentY += rowHeight
        }

        currentY += 20f
        val summaryLeft = PAGE_WIDTH - MARGIN - 210f
        val summaryValueRight = PAGE_WIDTH - MARGIN - 8f

        fun drawSummaryRow(label: String, value: String, isAccent: Boolean = false) {
            if (isAccent) {
                canvas.drawRoundRect(RectF(summaryLeft - 8f, currentY - 14f, PAGE_WIDTH - MARGIN, currentY + 8f), 4f, 4f, primaryPaint)
                canvas.drawText(label, summaryLeft, currentY, whiteTextPaint)
                val valWidth = whiteTextPaint.measureText(value)
                canvas.drawText(value, summaryValueRight - valWidth, currentY, whiteTextPaint)
            } else {
                canvas.drawText(label, summaryLeft, currentY, textSecondaryPaint)
                val valWidth = textPrimaryPaint.measureText(value)
                canvas.drawText(value, summaryValueRight - valWidth, currentY, textPrimaryPaint)
            }
            currentY += 18f
        }

        drawSummaryRow(lbl.subtotal, String.format(Locale.US, "%.2f %s", quote.subtotal, quote.currency))
        if (quote.discountTotal > 0) {
            drawSummaryRow(lbl.discount, String.format(Locale.US, "-%.2f %s", quote.discountTotal, quote.currency))
        }
        drawSummaryRow(lbl.taxTotal, String.format(Locale.US, "%.2f %s", quote.taxTotal, quote.currency))
        drawSummaryRow(lbl.estimatedTotal, String.format(Locale.US, "%.2f %s", quote.grandTotal, quote.currency), isAccent = true)

        val footerY = PAGE_HEIGHT - 35f
        canvas.drawLine(MARGIN, footerY, PAGE_WIDTH - MARGIN, footerY, linePaint)
        textSecondaryPaint.textSize = 8f
        canvas.drawText(lbl.thankYou, MARGIN, footerY + 16f, textSecondaryPaint)

        document.finishPage(page)

        val outDir = File(context.cacheDir, "invoices")
        if (!outDir.exists()) outDir.mkdirs()
        val file = File(outDir, "${quote.quotationNumber}.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    fun sharePdf(context: Context, file: File, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share $title via"))
    }
}
