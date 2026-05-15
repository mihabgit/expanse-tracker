package com.mihab.expensetracker.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mihab.expensetracker.util.BKashTransaction
import com.mihab.expensetracker.util.LocaleHelper
import com.mihab.expensetracker.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BKashSummaryScreen(
    viewModel: ExpenseViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val transactions by viewModel.bkashTransactions.collectAsState()
    val isBengali = Locale.getDefault().language == "bn"
    val currency = LocaleHelper.getCurrency(context)

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Today, 1: Weekly, 2: Monthly
    var hasSmsPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_SMS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasSmsPermission = isGranted
        if (isGranted) {
            viewModel.loadBKashTransactions(context)
        }
    }

    LaunchedEffect(hasSmsPermission) {
        if (hasSmsPermission) {
            viewModel.loadBKashTransactions(context)
        } else {
            launcher.launch(android.Manifest.permission.READ_SMS)
        }
    }

    val filteredTransactions = remember(transactions, selectedTab) {
        val now = Calendar.getInstance()
        when (selectedTab) {
            0 -> transactions.filter { isSameDay(it.date, now.timeInMillis) }
            1 -> transactions.filter { isSameWeek(it.date, now.timeInMillis) }
            2 -> transactions.filter { isSameMonth(it.date, now.timeInMillis) }
            else -> transactions
        }
    }

    val totalsByType = remember(filteredTransactions) {
        filteredTransactions.groupBy { it.type }.mapValues { entry ->
            entry.value.sumOf { it.amount }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isBengali) "বিকাশ সারসংক্ষেপ" else "bKash Summary") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (!hasSmsPermission) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text(
                        if (isBengali) "SMS পড়ার অনুমতি প্রয়োজন" else "SMS Permission Required",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (isBengali) "আপনার বিকাশ ট্রানজেকশনগুলো দেখতে SMS পড়ার অনুমতি দিন।" else "Please grant SMS permission to track your bKash transactions.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { launcher.launch(android.Manifest.permission.READ_SMS) }) {
                        Text(if (isBengali) "অনুমতি দিন" else "Grant Permission")
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text(if (isBengali) "আজ" else "Today", modifier = Modifier.padding(16.dp))
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text(if (isBengali) "এই সপ্তাহ" else "Weekly", modifier = Modifier.padding(16.dp))
                    }
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                        Text(if (isBengali) "এই মাস" else "Monthly", modifier = Modifier.padding(16.dp))
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Summary Cards
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            totalsByType.forEach { (type, total) ->
                                SummaryCard(type, total, currency, isBengali)
                            }
                        }
                    }

                    item {
                        Text(
                            if (isBengali) "লেনদেনের তালিকা" else "Transaction History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (filteredTransactions.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Text(if (isBengali) "কোন লেনদেন পাওয়া যায়নি" else "No transactions found")
                            }
                        }
                    } else {
                        items(filteredTransactions) { tx ->
                            TransactionItem(tx, currency, isBengali)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(type: String, total: Double, currency: String, isBengali: Boolean) {
    val color = when (type) {
        "Cash In", "Cashback" -> Color(0xFF4CAF50)
        "Cash Out", "Payment", "Mobile Recharge", "Send Money", "Bill Payment" -> Color(0xFFFF5252)
        else -> MaterialTheme.colorScheme.primary
    }
    
    val translatedType = when (type) {
        "Cash In" -> if (isBengali) "ক্যাশ ইন" else "Cash In"
        "Cashback" -> if (isBengali) "ক্যাশব্যাক" else "Cashback"
        "Cash Out" -> if (isBengali) "ক্যাশ আউট" else "Cash Out"
        "Payment" -> if (isBengali) "পেমেন্ট" else "Payment"
        "Mobile Recharge" -> if (isBengali) "মোবাইল রিচার্জ" else "Mobile Recharge"
        "Send Money" -> if (isBengali) "সেন্ট মানি" else "Send Money"
        "Bill Payment" -> if (isBengali) "বিল পেমেন্ট" else "Bill Payment"
        else -> type
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(translatedType, fontWeight = FontWeight.Bold, color = color)
            Text("$currency ${"%.2f".format(total)}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = color)
        }
    }
}

@Composable
fun TransactionItem(tx: BKashTransaction, currency: String, isBengali: Boolean) {
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx.date))
    val translatedType = when (tx.type) {
        "Cash In" -> if (isBengali) "ক্যাশ ইন" else "Cash In"
        "Cashback" -> if (isBengali) "ক্যাশব্যাক" else "Cashback"
        "Cash Out" -> if (isBengali) "ক্যাশ আউট" else "Cash Out"
        "Payment" -> if (isBengali) "পেমেন্ট" else "Payment"
        "Mobile Recharge" -> if (isBengali) "মোবাইল রিচার্জ" else "Mobile Recharge"
        "Send Money" -> if (isBengali) "সেন্ট মানি" else "Send Money"
        "Bill Payment" -> if (isBengali) "বিল পেমেন্ট" else "Bill Payment"
        else -> tx.type
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(translatedType, fontWeight = FontWeight.Bold)
            Text(dateStr, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Text(
            "${if (tx.type == "Cash In" || tx.type == "Cashback") "+" else "-"} $currency ${"%.2f".format(tx.amount)}",
            color = if (tx.type == "Cash In" || tx.type == "Cashback") Color(0xFF4CAF50) else Color(0xFFFF5252),
            fontWeight = FontWeight.Bold
        )
    }
}

private fun isSameDay(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun isSameWeek(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
}

private fun isSameMonth(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
}
