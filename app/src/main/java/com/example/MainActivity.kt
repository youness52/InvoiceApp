package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.navigation.ModalSheetState
import com.example.ui.navigation.NavTab
import com.example.ui.screens.CustomerEditorDialog
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InvoiceDetailsDialog
import com.example.ui.screens.InvoiceEditorDialog
import com.example.ui.screens.InvoicesScreen
import com.example.ui.screens.ProductEditorDialog
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.QuotationDetailsDialog
import com.example.ui.screens.QuotationEditorDialog
import com.example.ui.screens.QuotationsScreen
import com.example.ui.screens.RecordPaymentDialog
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupWizardScreen
import com.example.ui.theme.InvoiceManagerTheme
import com.example.ui.theme.PrimaryBlue
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InvoiceManagerTheme {
                MainAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MainViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val modalState by viewModel.modalState.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val profile by viewModel.businessProfile.collectAsStateWithLifecycle()
    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val quotations by viewModel.allQuotations.collectAsStateWithLifecycle()
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val invoiceSearch by viewModel.invoiceSearch.collectAsStateWithLifecycle()
    val invoiceStatusFilter by viewModel.invoiceStatusFilter.collectAsStateWithLifecycle()
    val quotationStatusFilter by viewModel.quotationStatusFilter.collectAsStateWithLifecycle()
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()

    val s = AppStrings[currentLanguage]
    val currency = profile?.currencyCode ?: "MAD"

    var languageMenuExpanded by remember { mutableStateOf(false) }

    // Enforce RTL / LTR layout direction dynamically based on chosen language
    CompositionLocalProvider(LocalLayoutDirection provides currentLanguage.layoutDirection) {
        if (!isOnboardingCompleted) {
            SetupWizardScreen(
                currentProfile = profile,
                currentLanguage = currentLanguage,
                onLanguageChange = { viewModel.setLanguage(it) },
                onFinish = { viewModel.completeOnboarding(it) }
            )
        } else {
            BackHandler(enabled = modalState != ModalSheetState.None || currentTab != NavTab.DASHBOARD) {
                if (modalState != ModalSheetState.None) {
                    viewModel.closeModal()
                } else if (currentTab != NavTab.DASHBOARD) {
                    viewModel.selectTab(NavTab.DASHBOARD)
                }
            }

            Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentTab) {
                                NavTab.DASHBOARD -> profile?.businessName ?: s.appName
                                NavTab.INVOICES -> s.invoices
                                NavTab.QUOTATIONS -> s.quotations
                                NavTab.CUSTOMERS -> s.clientsDirectory
                                NavTab.PRODUCTS -> s.productCatalog
                                NavTab.SETTINGS -> s.settings
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            )
                        )
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { languageMenuExpanded = true }) {
                                Icon(Icons.Default.Language, contentDescription = s.selectLanguage, tint = PrimaryBlue)
                            }
                            DropdownMenu(
                                expanded = languageMenuExpanded,
                                onDismissRequest = { languageMenuExpanded = false }
                            ) {
                                AppLanguage.values().forEach { lang ->
                                    val isSelected = currentLanguage == lang
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                lang.nativeName,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        trailingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryBlue) }
                                        } else null,
                                        onClick = {
                                            viewModel.setLanguage(lang)
                                            languageMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.navigationBarsPadding(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavTab.values().forEach { tab ->
                        val tabTitle = tab.getTitle(s)
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = { viewModel.selectTab(tab) },
                            icon = { Icon(tab.icon, contentDescription = tabTitle) },
                            label = { Text(tabTitle, fontSize = 11.sp, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                indicatorColor = PrimaryBlue.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    NavTab.DASHBOARD -> DashboardScreen(
                        metrics = metrics,
                        recentInvoices = invoices,
                        profile = profile,
                        language = currentLanguage,
                        onOpenModal = { viewModel.openModal(it) }
                    )
                    NavTab.INVOICES -> InvoicesScreen(
                        invoices = invoices,
                        searchQuery = invoiceSearch,
                        onSearchChange = { viewModel.setInvoiceSearch(it) },
                        statusFilter = invoiceStatusFilter,
                        onStatusFilterChange = { viewModel.setInvoiceStatusFilter(it) },
                        profile = profile,
                        language = currentLanguage,
                        onOpenModal = { viewModel.openModal(it) }
                    )
                    NavTab.QUOTATIONS -> QuotationsScreen(
                        quotations = quotations,
                        statusFilter = quotationStatusFilter,
                        onStatusFilterChange = { viewModel.setQuotationStatusFilter(it) },
                        profile = profile,
                        language = currentLanguage,
                        onOpenModal = { viewModel.openModal(it) },
                        onConvertQuotation = { viewModel.convertQuotationToInvoice(it) }
                    )
                    NavTab.CUSTOMERS -> CustomersScreen(
                        customers = customers,
                        invoices = invoices,
                        currency = currency,
                        language = currentLanguage,
                        onOpenModal = { viewModel.openModal(it) },
                        onDeleteCustomer = { viewModel.deleteCustomer(it) }
                    )
                    NavTab.PRODUCTS -> ProductsScreen(
                        products = products,
                        currency = currency,
                        language = currentLanguage,
                        onOpenModal = { viewModel.openModal(it) },
                        onDeleteProduct = { viewModel.deleteProduct(it) }
                    )
                    NavTab.SETTINGS -> SettingsScreen(
                        profile = profile,
                        language = currentLanguage,
                        onLanguageChange = { viewModel.setLanguage(it) },
                        onSaveProfile = { viewModel.saveBusinessProfile(it) },
                        onRerunWizard = { viewModel.startOnboarding() }
                    )
                }
            }
        }

        // Modal Sheet Handlers
        when (val state = modalState) {
            ModalSheetState.None -> {}

            ModalSheetState.CreateInvoice -> {
                InvoiceEditorDialog(
                    profile = profile,
                    customers = customers,
                    products = products,
                    initialInvoiceNumber = String.format(java.util.Locale.US, "%s%04d", profile?.invoicePrefix ?: "INV-2026-", invoices.size + 1),
                    language = currentLanguage,
                    onDismiss = { viewModel.closeModal() },
                    onSave = { inv, items ->
                        viewModel.saveInvoice(inv, items)
                    }
                )
            }

            ModalSheetState.CreateQuotation -> {
                QuotationEditorDialog(
                    profile = profile,
                    customers = customers,
                    products = products,
                    initialQuotationNumber = String.format(java.util.Locale.US, "%s%04d", profile?.quotationPrefix ?: "DEV-2026-", quotations.size + 1),
                    language = currentLanguage,
                    onDismiss = { viewModel.closeModal() },
                    onSave = { quote, items ->
                        viewModel.saveQuotation(quote, items)
                    }
                )
            }

            is ModalSheetState.InvoiceDetails -> {
                val itemWithDetails = invoices.firstOrNull { it.invoice.id == state.invoiceId }
                if (itemWithDetails != null) {
                    InvoiceDetailsDialog(
                        invoiceWithDetails = itemWithDetails,
                        profile = profile,
                        language = currentLanguage,
                        onDismiss = { viewModel.closeModal() },
                        onRecordPayment = { viewModel.openModal(ModalSheetState.RecordPayment(state.invoiceId)) },
                        onDelete = { viewModel.deleteInvoice(itemWithDetails.invoice) }
                    )
                }
            }

            is ModalSheetState.QuotationDetails -> {
                val quoteWithDetails = quotations.firstOrNull { it.quotation.id == state.quotationId }
                if (quoteWithDetails != null) {
                    QuotationDetailsDialog(
                        quotationWithDetails = quoteWithDetails,
                        profile = profile,
                        language = currentLanguage,
                        onDismiss = { viewModel.closeModal() },
                        onConvert = { viewModel.convertQuotationToInvoice(quoteWithDetails) },
                        onDelete = { viewModel.deleteQuotation(quoteWithDetails.quotation) }
                    )
                }
            }

            is ModalSheetState.RecordPayment -> {
                val itemWithDetails = invoices.firstOrNull { it.invoice.id == state.invoiceId }
                if (itemWithDetails != null) {
                    RecordPaymentDialog(
                        invoiceWithDetails = itemWithDetails,
                        language = currentLanguage,
                        onDismiss = { viewModel.closeModal() },
                        onConfirm = { amount, method, reference, notes ->
                            viewModel.recordPayment(itemWithDetails, amount, method, reference, notes)
                        }
                    )
                }
            }

            is ModalSheetState.EditCustomer -> {
                val customer = customers.firstOrNull { it.id == state.customerId }
                CustomerEditorDialog(
                    initialCustomer = customer,
                    language = currentLanguage,
                    onDismiss = { viewModel.closeModal() },
                    onSave = { viewModel.saveCustomer(it) }
                )
            }

            is ModalSheetState.EditProduct -> {
                val product = products.firstOrNull { it.id == state.productId }
                ProductEditorDialog(
                    initialProduct = product,
                    currency = currency,
                    language = currentLanguage,
                    onDismiss = { viewModel.closeModal() },
                    onSave = { viewModel.saveProduct(it) }
                )
            }

            is ModalSheetState.PdfPreview -> {}
        }
        }
    }
}
