package com.mihab.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mihab.expensetracker.ui.components.AdBanner
import com.mihab.expensetracker.util.LocaleHelper
import com.mihab.expensetracker.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ExpenseViewModel,
    onAddClick: () -> Unit,
    onSeeAllClick: () -> Unit,
    onSummaryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onManageQuickClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currency = LocaleHelper.getCurrency(context)
    val todayTotal by viewModel.todayTotal.collectAsState()
    val recentExpenses by viewModel.recentExpenses.collectAsState()
    val todayCount by viewModel.todayCount.collectAsState()
    val quickExpenses by viewModel.quickExpenses.collectAsState()

    val isBengali = Locale.getDefault().language == "bn"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (isBengali) "স্বাগতম!" else "Welcome back!",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            if (isBengali) "খরচ খাতা" else "Expense Tracker",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSummaryClick) {
                        Icon(Icons.Default.Info, contentDescription = "Summary", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            //AdBanner()
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Today's Total 💰 (Hero Section)
            item {
                val isDark = isSystemInDarkTheme()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = if (isDark) {
                                        listOf(
                                            Color(0xFF6200EE),
                                            Color(0xFF3700B3)
                                        )
                                    } else {
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                )
                            )
                            .padding(24.dp)
                    ) {
                        // Background decorative shapes
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .offset(x = 60.dp, y = (-40).dp)
                                .align(Alignment.TopEnd)
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        )
                        
                        Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                            Text(
                                if (isBengali) "আজকের মোট খরচ" else "Total Spend Today",
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 0.5.sp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "$currency ${"%.2f".format(todayTotal)}",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 40.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF4CAF50), CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        if (isBengali) "আজ $todayCount টি লেনদেন" else "$todayCount transactions today",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Add Section ⚡
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (isBengali) "দ্রুত যোগ করুন" else "Quick Add",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        TextButton(onClick = onManageQuickClick) {
                            Text(if (isBengali) "সম্পাদনা" else "Edit", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Custom Add Button
                        OutlinedCard(
                            onClick = onAddClick,
                            modifier = Modifier.size(width = 80.dp, height = 100.dp),
                            shape = RoundedCornerShape(20.dp),
                            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(if (isBengali) "নতুন" else "New", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        // Predefined Expenses
                        quickExpenses.forEach { quick ->
                            val quickColor = Color(quick.color)
                            Card(
                                onClick = { viewModel.addExpense(quick.amount, quick.category, "Quick add") },
                                modifier = Modifier.size(width = 95.dp, height = 100.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = quickColor.copy(alpha = 0.1f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(quick.icon, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        if (isBengali) quick.categoryBn else quick.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = quickColor,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        "$currency${quick.amount.toInt()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = quickColor.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recent Expenses Section 📜
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isBengali) "সাম্প্রতিক খরচ" else "Recent Expenses",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = onSeeAllClick) {
                        Text(if (isBengali) "সব দেখুন" else "View History", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (recentExpenses.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(if (isBengali) "কোন খরচ পাওয়া যায়নি।" else "No expenses recorded yet.", color = Color.Gray)
                        Text(if (isBengali) "নতুন খরচ যোগ করুন!" else "Start by adding one!", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            } else {
                items(recentExpenses) { expense ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(getCategoryEmoji(expense.category), fontSize = 24.sp)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        getCategoryName(expense.category, isBengali),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(expense.date))
                                    Text(
                                        dateStr,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                "- $currency${"%.2f".format(expense.amount)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

fun getCategoryName(category: String, isBengali: Boolean): String {
    if (!isBengali) return category
    return when (category) {
        "Food" -> "খাবার"
        "Transport" -> "পরিবহন"
        "Rent" -> "ভাড়া"
        "Shopping" -> "কেনাকাটা"
        "Bills" -> "বিল"
        "Entertainment" -> "বিনোদন"
        "Health" -> "স্বাস্থ্য"
        "Education" -> "শিক্ষা"
        "Investment" -> "বিনিয়োগ"
        "Gifts" -> "উপহার"
        "Travel" -> "ভ্রমণ"
        "Coffee" -> "কফি"
        "Bus Fare" -> "বাস ভাড়া"
        "Lunch" -> "দুপুরের খাবার"
        "Snacks" -> "নাস্তা"
        "Grocery" -> "মুদি খরচ"
        "Other" -> "অন্যান্য"
        else -> category
    }
}

fun getCategoryEmoji(category: String): String {
    return when (category) {
        "Food" -> "🍔"
        "Transport" -> "🚌"
        "Rent" -> "🏠"
        "Shopping" -> "🛒"
        "Bills" -> "📄"
        "Entertainment" -> "🎬"
        "Health" -> "💊"
        "Education" -> "📚"
        "Investment" -> "📈"
        "Gifts" -> "🎁"
        "Travel" -> "✈️"
        "Coffee" -> "☕"
        "Bus Fare" -> "🚌"
        "Lunch" -> "🍔"
        "Snacks" -> "🍿"
        "Grocery" -> "🛒"
        else -> "💰"
    }
}
