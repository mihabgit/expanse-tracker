package com.mihab.expensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mihab.expensetracker.util.LocaleHelper
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLanguageChanged: () -> Unit,
    onCurrencyChanged: () -> Unit
) {
    val context = LocalContext.current
    val isBengali = Locale.getDefault().language == "bn"
    var currentLanguage by remember { mutableStateOf(if (isBengali) "bn" else "en") }
    var currentCurrency by remember { mutableStateOf(LocaleHelper.getCurrency(context)) }

    val currencies = listOf("৳", "$", "€", "£", "₹")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isBengali) "সেটিংস" else "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                if (isBengali) "ভাষা পরিবর্তন করুন" else "Change Language",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("English")
                        RadioButton(
                            selected = currentLanguage == "en",
                            onClick = {
                                if (currentLanguage != "en") {
                                    currentLanguage = "en"
                                    LocaleHelper.setLocale(context, "en")
                                    onLanguageChanged()
                                }
                            }
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("বাংলা")
                        RadioButton(
                            selected = currentLanguage == "bn",
                            onClick = {
                                if (currentLanguage != "bn") {
                                    currentLanguage = "bn"
                                    LocaleHelper.setLocale(context, "bn")
                                    onLanguageChanged()
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                if (isBengali) "কারেন্সি পরিবর্তন করুন" else "Change Currency",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    currencies.forEachIndexed { index, currency ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(currency, style = MaterialTheme.typography.bodyLarge)
                            RadioButton(
                                selected = currentCurrency == currency,
                                onClick = {
                                    currentCurrency = currency
                                    LocaleHelper.setCurrency(context, currency)
                                    onCurrencyChanged()
                                }
                            )
                        }
                        if (index < currencies.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }
    }
}
