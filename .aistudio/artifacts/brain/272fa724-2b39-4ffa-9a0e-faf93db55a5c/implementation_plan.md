# Invoice Manager — Professional Invoicing & Business Management

A complete, production-grade Android application for freelancers, contractors, and service businesses to create professional invoices and quotations, manage customers and catalog items, track partial and full payments, and generate high-fidelity PDF documents offline.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following key decisions were confirmed during the initial consultation:
> - **Platform & Technology**: Built natively in Kotlin with Jetpack Compose, Android Material 3, and Room local persistence for responsive performance and offline-first reliability.
> - **Default Currency & Region**: Configured with Moroccan Dirham (`MAD`) as the initial currency preset, with multi-currency support (EUR, USD, etc.) configurable at any time in Settings.
> - **Default Document Style**: Modern template featuring deep blue accents (`#2563EB`), clear corporate typography, distinct itemized tables, and professional summary blocks.

---

## 1. Overview & Core Concept

- **What It Does**: End-to-end invoicing lifecycle management from quotation creation and one-tap conversion to invoice generation, payment recording, balance tracking, and printable A4 PDF export and native sharing.
- **Target Audience / Persona**: Independent professionals, tradespeople, contractors, and small business owners who need to issue clean, branded bills and quotations directly from their mobile device without requiring mandatory subscriptions or cloud accounts.
- **Key Value**: 100% offline-capable business bookkeeping, instant calculation accuracy (subtotals, multi-rate tax, item/invoice discounts, balance due), and native PDF rendering with instant WhatsApp/email sharing.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **First-Launch Onboarding & Business Profile**: Quick setup to configure business name, address, contact details, tax identification (ICE/IF/RC), default currency (`MAD`), and payment terms. Can be edited anytime in Settings.
2. **Dashboard Overview**: Financial telemetry cards displaying Total Invoiced, Received Revenue, Outstanding Balance, and Overdue Amount, alongside a monthly revenue chart, quick actions, and filter tabs (All, This Month, This Year).
3. **Quotation Workflow**: Draft quotations with validity dates and discount/tax items. Mark as Sent, Accepted, or Rejected; one-tap convert to an active Invoice with duplicate prevention.
4. **Invoice Creation & Live Preview**: Select an existing customer or enter details inline, add catalog or custom line items with unit price, quantity, and tax rate. Real-time balance and total calculations.
5. **Payment Tracking & Receipt**: Record full or partial payments across cash, bank transfer, check, or card. Real-time balance updates and payment receipt generation.
6. **A4 PDF Document Generation & Sharing**: Native Android canvas-rendered PDF with company header, client info, itemized table, notes, terms, and direct share to WhatsApp, Gmail, or print spooler.

### Visual Identity & Theme

- **Aesthetic Direction**: Deep Blue Executive / Modern Professional. Utilitarian, crisp, high-contrast readability tailored for business owners.
- **Color Palette**:
  - Primary: Deep Executive Blue (`#1E40AF` / `#2563EB`)
  - Surface & Background: Crisp slate-tinted canvas (`#F8FAFC` light / `#0F172A` dark)
  - Card Surfaces: Pure White (`#FFFFFF` light / `#1E293B` dark) with subtle borders
  - Financial States: Paid/Success (`#16A34A`), Pending/Due (`#D97706`), Overdue/Alert (`#DC2626`)
- **Typography & Hierarchy**: Clear numerical tabular figures for financial values, bold headings for invoice totals, and muted labels for secondary metadata.
- **Layout & Accessibility**: Bottom navigation bar (Dashboard, Invoices, Quotations, Customers, Products, Settings), floating action buttons with standard 48dp+ tap targets, and full edge-to-edge system navigation support.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Native Room SQLite over Client-side Cloud-only Storage**
  - *Chosen Approach*: Fully local-first Room database with reactive Kotlin Flow queries.
  - *Why*: Critical business tools must work instantly on job sites without waiting for network connectivity or cloud authentication gates.
  - *Alternatives Considered*: Firebase-only storage was rejected because offline failures on job sites would block billing.
- **Decision 2: Native Android PdfDocument Engine over Web/HTML Converters**
  - *Chosen Approach*: Core `android.graphics.pdf.PdfDocument` with Android vector rendering and text drawing.
  - *Why*: Eliminates slow headless browser webviews, avoids external third-party binary dependencies, and provides crisp vector typography and consistent page margins.
- **Decision 3: Snapshot Preservation on Financial Documents**
  - *Chosen Approach*: When an invoice or quotation is created, customer details and item details are permanently snapshot in the document record.
  - *Why*: Editing a product price or customer address next month must never retroactively alter already-issued historical legal invoices.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
│ ┌──────────────┬──────────────┬──────────────┬──────────────┬────────┐ │
│ │  Dashboard   │   Invoices   │  Quotations  │  Customers   │Products│ │
│ └──────┬───────┴──────┬───────┴──────┬───────┴──────┬───────┴───┬────┘ │
│        │              │              │              │           │      │
│        ▼              ▼              ▼              ▼           ▼      │
│ ┌────────────────────────────────────────────────────────────────────┐ │
│ │                  ViewModels (StateFlow & MVI Actions)              │ │
│ └──────────────────────────────────┬─────────────────────────────────┘ │
└────────────────────────────────────┼───────────────────────────────────┘
                                     │
                                     ▼
┌────────────────────────────────────────────────────────────────────────┐
│                          Domain & Data Layer                           │
│ ┌────────────────────────────────────────────────────────────────────┐ │
│ │  AppRepository (Business profile, Invoices, Customers, Products)   │ │
│ └──────────────────┬─────────────────────────────┬───────────────────┘ │
│                    │                             │                     │
│                    ▼                             ▼                     │
│ ┌────────────────────────────────────┐ ┌─────────────────────────────┐ │
│ │       Room SQLite Database         │ │    Native PDF Generator     │ │
│ │   (Entities, DAOs, TypeConverters) │ │ (android.graphics.pdf)      │ │
│ └────────────────────────────────────┘ └──────────────┬──────────────┘ │
└───────────────────────────────────────────────────────┼────────────────┘
                                                        ▼
                                         Android Native Share & Print
```

### Data Entities & Schema

- **BusinessProfile**: `id`, `businessName`, `logoUri`, `email`, `phone`, `address`, `taxId` (ICE/IF), `currencyCode` (e.g. "MAD"), `invoicePrefix`, `quotationPrefix`, `defaultTerms`, `defaultNotes`.
- **Customer**: `id`, `name`, `contactPerson`, `email`, `phone`, `address`, `taxNumber`, `notes`, `createdAt`.
- **Product**: `id`, `name`, `description`, `sku`, `unit` (e.g., pcs, hrs, days), `unitPrice`, `taxRate`, `isArchived`.
- **Invoice**: `id`, `invoiceNumber`, `customerId`, `customerName`, `customerAddress`, `customerPhone`, `issueDate`, `dueDate`, `currency`, `subtotal`, `discountTotal`, `taxTotal`, `grandTotal`, `amountPaid`, `balanceDue`, `status` (`DRAFT`, `ISSUED`, `PARTIAL`, `PAID`, `OVERDUE`, `CANCELLED`), `notes`, `terms`, `createdAt`.
- **InvoiceItem**: `id`, `invoiceId`, `description`, `quantity`, `unitPrice`, `taxRate`, `discount`, `lineTotal`.
- **Quotation**: `id`, `quotationNumber`, `customerId`, `customerName`, `customerAddress`, `issueDate`, `validUntil`, `currency`, `subtotal`, `discountTotal`, `taxTotal`, `grandTotal`, `status` (`DRAFT`, `SENT`, `ACCEPTED`, `REJECTED`, `CONVERTED`), `convertedInvoiceId`, `notes`, `terms`.
- **Payment**: `id`, `invoiceId`, `paymentDate`, `amount`, `paymentMethod` (`CASH`, `BANK_TRANSFER`, `CHECK`, `CARD`, `OTHER`), `reference`, `notes`.

### Interactive Component & State Mapping

- **Dashboard**: Real-time aggregation of active invoices; calculates total invoiced, received, outstanding, and overdue amounts with dynamic status pill filters.
- **Invoice Editor**: Dynamic line-item list with instant math recalculations, customer search dropdown, due-date picker, and validation to prevent negative totals or invalid numbers.
- **Quotation Converter**: One-click action converting accepted quotation into an active invoice with duplicate check and link recording.
- **Payment Dialog**: Records payment amount with automatic balance recalculation; transitions invoice status to `PARTIAL` or `PAID`.
- **PDF Viewer & Sharing**: Generates cached PDF file and launches standard Android `Intent.ACTION_SEND` chooser to share via WhatsApp, Mail, or Google Drive.
