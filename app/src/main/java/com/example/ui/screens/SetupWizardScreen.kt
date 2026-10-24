package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessProfileEntity
import com.example.ui.components.formatCurrency
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StatusPaid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupWizardScreen(
    currentProfile: BusinessProfileEntity?,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onFinish: (BusinessProfileEntity) -> Unit
) {
    var step by remember { mutableStateOf(0) }
    val s = AppStrings[currentLanguage]

    BackHandler(enabled = true) {
        if (step > 0) {
            step--
        } else if (currentProfile != null) {
            onFinish(currentProfile)
        }
    }

    val initial = currentProfile ?: BusinessProfileEntity()
    var businessName by remember { mutableStateOf(initial.businessName) }
    var email by remember { mutableStateOf(initial.email) }
    var phone by remember { mutableStateOf(initial.phone) }
    var address by remember { mutableStateOf(initial.address) }
    var taxId by remember { mutableStateOf(initial.taxId) }
    var currencyCode by remember { mutableStateOf(initial.currencyCode) }
    var invoicePrefix by remember { mutableStateOf(initial.invoicePrefix) }
    var quotationPrefix by remember { mutableStateOf(initial.quotationPrefix) }
    var defaultTerms by remember { mutableStateOf(initial.defaultTerms) }
    var defaultNotes by remember { mutableStateOf(initial.defaultNotes) }

    val currencies = listOf("MAD", "EUR", "USD", "GBP", "CAD")
    var currencyExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Top Bar with Skip Button & Step Progress Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step Dots
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (0..3).forEach { index ->
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (step == index) 28.dp else 10.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (step >= index) PrimaryBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        )
                    }
                }

                if (step < 3) {
                    TextButton(
                        onClick = {
                            val profile = initial.copy(
                                businessName = businessName,
                                email = email,
                                phone = phone,
                                address = address,
                                taxId = taxId,
                                currencyCode = currencyCode,
                                invoicePrefix = invoicePrefix,
                                quotationPrefix = quotationPrefix,
                                defaultTerms = defaultTerms,
                                defaultNotes = defaultNotes
                            )
                            onFinish(profile)
                        }
                    ) {
                        Text(s.skip, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "WizardStepAnimation"
                ) { targetStep ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (targetStep) {
                            0 -> {
                                // Step 1: Welcome & Language
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.ReceiptLong,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(38.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = s.wizardWelcomeTitle,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = s.wizardWelcomeSubtitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = s.stepLanguageTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )

                                // Language choice cards
                                AppLanguage.values().forEach { lang ->
                                    val isSelected = currentLanguage == lang
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onLanguageChange(lang) },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) PrimaryBlue.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                                        ),
                                        border = if (isSelected) BorderStroke(2.dp, PrimaryBlue) else CardDefaults.outlinedCardBorder()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Language,
                                                    contentDescription = null,
                                                    tint = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = lang.nativeName,
                                                        style = MaterialTheme.typography.titleMedium.copy(
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                        ),
                                                        color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = lang.displayName,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = PrimaryBlue,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            1 -> {
                                // Step 2: Business Profile
                                Column {
                                    Text(
                                        text = s.stepCompanyTitle,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = s.stepCompanySubtitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = CardDefaults.outlinedCardBorder()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = businessName,
                                            onValueChange = { businessName = it },
                                            label = { Text(s.businessName) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        OutlinedTextField(
                                            value = phone,
                                            onValueChange = { phone = it },
                                            label = { Text(s.phone) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        OutlinedTextField(
                                            value = email,
                                            onValueChange = { email = it },
                                            label = { Text(s.email) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        OutlinedTextField(
                                            value = address,
                                            onValueChange = { address = it },
                                            label = { Text(s.billingAddress) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        OutlinedTextField(
                                            value = taxId,
                                            onValueChange = { taxId = it },
                                            label = { Text(s.taxIdIce) },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                            }

                            2 -> {
                                // Step 3: Currency & Numbering
                                Column {
                                    Text(
                                        text = s.stepPreferencesTitle,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = s.stepPreferencesSubtitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = CardDefaults.outlinedCardBorder()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        ExposedDropdownMenuBox(
                                            expanded = currencyExpanded,
                                            onExpandedChange = { currencyExpanded = !currencyExpanded }
                                        ) {
                                            OutlinedTextField(
                                                value = currencyCode,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text(s.defaultCurrency) },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .menuAnchor(),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            ExposedDropdownMenu(
                                                expanded = currencyExpanded,
                                                onDismissRequest = { currencyExpanded = false }
                                            ) {
                                                currencies.forEach { curr ->
                                                    DropdownMenuItem(
                                                        text = { Text(curr) },
                                                        onClick = {
                                                            currencyCode = curr
                                                            currencyExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = invoicePrefix,
                                                onValueChange = { invoicePrefix = it },
                                                label = { Text(s.invoicePrefix) },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            OutlinedTextField(
                                                value = quotationPrefix,
                                                onValueChange = { quotationPrefix = it },
                                                label = { Text(s.quotationPrefix) },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                        }

                                        OutlinedTextField(
                                            value = defaultTerms,
                                            onValueChange = { defaultTerms = it },
                                            label = { Text(s.defaultTerms) },
                                            modifier = Modifier.fillMaxWidth(),
                                            maxLines = 2,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                            }

                            3 -> {
                                // Step 4: Summary & Ready
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(StatusPaid.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = StatusPaid,
                                            modifier = Modifier.size(38.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = s.stepReadyTitle,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = s.stepReadySubtitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = CardDefaults.outlinedCardBorder()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = s.companyDetails,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text("${s.businessName}: $businessName", fontWeight = FontWeight.SemiBold)
                                        if (phone.isNotEmpty()) Text("${s.phone}: $phone", style = MaterialTheme.typography.bodySmall)
                                        if (email.isNotEmpty()) Text("${s.email}: $email", style = MaterialTheme.typography.bodySmall)
                                        if (address.isNotEmpty()) Text("${s.billingAddress}: $address", style = MaterialTheme.typography.bodySmall)
                                        if (taxId.isNotEmpty()) Text("${s.taxIdIce}: $taxId", style = MaterialTheme.typography.bodySmall)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${s.defaultCurrency}: $currencyCode • ${s.invoicePrefix}: $invoicePrefix", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Buttons (Previous, Next / Finish)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (step > 0) {
                    OutlinedButton(
                        onClick = { step-- },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(s.previous)
                    }
                }

                Button(
                    onClick = {
                        if (step < 3) {
                            step++
                        } else {
                            val profile = initial.copy(
                                businessName = businessName,
                                email = email,
                                phone = phone,
                                address = address,
                                taxId = taxId,
                                currencyCode = currencyCode,
                                invoicePrefix = invoicePrefix,
                                quotationPrefix = quotationPrefix,
                                defaultTerms = defaultTerms,
                                defaultNotes = defaultNotes
                            )
                            onFinish(profile)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (step < 3) s.next else s.finishSetup,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
