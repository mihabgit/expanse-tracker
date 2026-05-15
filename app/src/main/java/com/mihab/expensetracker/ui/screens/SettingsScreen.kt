package com.mihab.expensetracker.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onCurrencyChanged: () -> Unit,
    onManageQuickExpensesClick: () -> Unit,
    onManageCategoriesClick: () -> Unit,
    onTutorialClick: () -> Unit,
    themeMode: String,
    onThemeChanged: (String) -> Unit,
    showBKashCard: Boolean,
    onBKashCardToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val isBengali = Locale.getDefault().language == "bn"
    var currentLanguage by remember { mutableStateOf(if (isBengali) "bn" else "en") }
    var currentCurrency by remember { mutableStateOf(LocaleHelper.getCurrency(context)) }

    //val currencies = listOf("৳", "$", "€", "£", "₹")
    val currencies = listOf("৳", "$")

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
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
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
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                if (isBengali) "থিম পরিবর্তন করুন" else "Change Theme",
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isBengali) "সিস্টেম ডিফল্ট" else "System Default")
                        RadioButton(
                            selected = themeMode == "system",
                            onClick = { onThemeChanged("system") }
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isBengali) "লাইট মোড" else "Light Mode")
                        RadioButton(
                            selected = themeMode == "light",
                            onClick = { onThemeChanged("light") }
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isBengali) "ডার্ক মোড" else "Dark Mode")
                        RadioButton(
                            selected = themeMode == "dark",
                            onClick = { onThemeChanged("dark") }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                if (isBengali) "বিকাশ সারসংক্ষেপ টুল" else "bKash Summary Tool",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (isBengali) "হোম স্ক্রিনে দেখান" else "Show on Home Screen",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            if (isBengali) "বিকাশ ট্রানজেকশন ট্র্যাকিং কার্ড" else "bKash transaction tracking card",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = showBKashCard,
                        onCheckedChange = onBKashCardToggle
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                if (isBengali) "দ্রুত খরচ কাস্টমাইজ করুন" else "Customize Quick Expenses",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Card(
                onClick = onManageQuickExpensesClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        if (isBengali) "দ্রুত খরচ ম্যানেজ করুন" else "Manage Quick Expenses",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                if (isBengali) "ক্যাটাগরি কাস্টমাইজ করুন" else "Customize Categories",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Card(
                onClick = onManageCategoriesClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        if (isBengali) "ক্যাটাগরি ম্যানেজ করুন" else "Manage Categories",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                if (isBengali) "টিউটোরিয়াল" else "Tutorial",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Card(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/shorts/W3UfxJrjliQ?feature=share"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        if (isBengali) "ভিডিও টিউটোরিয়াল দেখুন" else "Watch Video Tutorial",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("📺")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))

            // App Version Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val versionName = try {
                    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                    packageInfo.versionName
                } catch (_: Exception) {
                    "1.0.5"
                }

                Text(
                    text = if (isBengali) "ভার্সন $versionName" else "Version $versionName",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
                Text(
                    text = if (isBengali) "© ২০২৬ খরচ খাতা" else "© 2026 Expense Tracker",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
