package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.data.repository.InvoiceRepository
import com.example.ui.navigation.ModalSheetState
import com.example.ui.navigation.NavTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardMetrics(
    val totalInvoiced: Double = 0.0,
    val totalPaid: Double = 0.0,
    val outstandingBalance: Double = 0.0,
    val overdueAmount: Double = 0.0,
    val totalInvoicesCount: Int = 0,
    val totalCustomersCount: Int = 0,
    val monthlyRevenue: List<Pair<String, Double>> = emptyList()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InvoiceRepository(AppDatabase.getDatabase(application))

    private val prefs = application.getSharedPreferences("invoice_manager_prefs", android.content.Context.MODE_PRIVATE)
    private val _currentLanguage = MutableStateFlow(
        com.example.ui.i18n.AppLanguage.fromCode(prefs.getString("app_language", "fr") ?: "fr")
    )
    val currentLanguage: StateFlow<com.example.ui.i18n.AppLanguage> = _currentLanguage.asStateFlow()

    fun setLanguage(language: com.example.ui.i18n.AppLanguage) {
        _currentLanguage.value = language
        prefs.edit().putString("app_language", language.code).apply()
    }

    private val _isOnboardingCompleted = MutableStateFlow(
        prefs.getBoolean("is_onboarding_completed", false)
    )
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    fun completeOnboarding(profile: BusinessProfileEntity) {
        viewModelScope.launch {
            repository.saveBusinessProfile(profile)
            prefs.edit().putBoolean("is_onboarding_completed", true).apply()
            _isOnboardingCompleted.value = true
        }
    }

    fun startOnboarding() {
        _isOnboardingCompleted.value = false
    }

    private val _currentTab = MutableStateFlow(NavTab.DASHBOARD)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    private val _modalState = MutableStateFlow<ModalSheetState>(ModalSheetState.None)
    val modalState: StateFlow<ModalSheetState> = _modalState.asStateFlow()

    private val _invoiceSearch = MutableStateFlow("")
    val invoiceSearch: StateFlow<String> = _invoiceSearch.asStateFlow()

    private val _invoiceStatusFilter = MutableStateFlow<InvoiceStatus?>(null)
    val invoiceStatusFilter: StateFlow<InvoiceStatus?> = _invoiceStatusFilter.asStateFlow()

    private val _quotationStatusFilter = MutableStateFlow<QuotationStatus?>(null)
    val quotationStatusFilter: StateFlow<QuotationStatus?> = _quotationStatusFilter.asStateFlow()

    val businessProfile: StateFlow<BusinessProfileEntity?> = repository.businessProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allInvoices: StateFlow<List<InvoiceWithDetails>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuotations: StateFlow<List<QuotationWithDetails>> = repository.allQuotations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayments: StateFlow<List<PaymentEntity>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        repository.allInvoices,
        repository.allCustomers
    ) { invoices, customers ->
        var invoiced = 0.0
        var paid = 0.0
        var outstanding = 0.0
        var overdue = 0.0

        val monthsMap = mutableMapOf<Int, Double>()
        val cal = Calendar.getInstance()

        invoices.forEach { item ->
            val inv = item.invoice
            if (inv.status != InvoiceStatus.CANCELLED && inv.status != InvoiceStatus.DRAFT) {
                invoiced += inv.grandTotal
                paid += inv.amountPaid
                outstanding += inv.balanceDue
                if (inv.status == InvoiceStatus.OVERDUE || (inv.balanceDue > 0 && System.currentTimeMillis() > inv.dueDate)) {
                    overdue += inv.balanceDue
                }

                cal.timeInMillis = inv.issueDate
                val m = cal.get(Calendar.MONTH)
                monthsMap[m] = (monthsMap[m] ?: 0.0) + inv.amountPaid
            }
        }

        val monthLabels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val chartData = (0..5).map { offset ->
            val m = (currentMonth - 5 + offset + 12) % 12
            monthLabels[m] to (monthsMap[m] ?: 0.0)
        }

        DashboardMetrics(
            totalInvoiced = invoiced,
            totalPaid = paid,
            outstandingBalance = outstanding,
            overdueAmount = overdue,
            totalInvoicesCount = invoices.size,
            totalCustomersCount = customers.size,
            monthlyRevenue = chartData
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
        }
    }

    fun selectTab(tab: NavTab) {
        _currentTab.value = tab
    }

    fun openModal(modal: ModalSheetState) {
        _modalState.value = modal
    }

    fun closeModal() {
        _modalState.value = ModalSheetState.None
    }

    fun setInvoiceSearch(query: String) {
        _invoiceSearch.value = query
    }

    fun setInvoiceStatusFilter(status: InvoiceStatus?) {
        _invoiceStatusFilter.value = status
    }

    fun setQuotationStatusFilter(status: QuotationStatus?) {
        _quotationStatusFilter.value = status
    }

    suspend fun getNextInvoiceNumber(): String {
        return repository.generateNextInvoiceNumber()
    }

    suspend fun getNextQuotationNumber(): String {
        return repository.generateNextQuotationNumber()
    }

    fun saveInvoice(invoice: InvoiceEntity, items: List<InvoiceItemEntity>, onSaved: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.saveInvoiceWithItems(invoice, items)
            onSaved?.invoke(id)
            closeModal()
        }
    }

    fun saveQuotation(quotation: QuotationEntity, items: List<QuotationItemEntity>, onSaved: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.saveQuotationWithItems(quotation, items)
            onSaved?.invoke(id)
            closeModal()
        }
    }

    fun convertQuotationToInvoice(quotationWithDetails: QuotationWithDetails) {
        viewModelScope.launch {
            val invId = repository.convertQuotationToInvoice(quotationWithDetails)
            _modalState.value = ModalSheetState.InvoiceDetails(invId)
        }
    }

    fun recordPayment(
        invoiceWithDetails: InvoiceWithDetails,
        amount: Double,
        paymentMethod: PaymentMethod,
        reference: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.recordPayment(invoiceWithDetails, amount, paymentMethod, reference, notes)
            closeModal()
        }
    }

    fun deleteInvoice(invoice: InvoiceEntity) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
            closeModal()
        }
    }

    fun deleteQuotation(quotation: QuotationEntity) {
        viewModelScope.launch {
            repository.deleteQuotation(quotation)
            closeModal()
        }
    }

    fun saveCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.saveCustomer(customer)
            closeModal()
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            closeModal()
        }
    }

    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.saveProduct(product)
            closeModal()
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            closeModal()
        }
    }

    fun saveBusinessProfile(profile: BusinessProfileEntity) {
        viewModelScope.launch {
            repository.saveBusinessProfile(profile)
        }
    }
}
