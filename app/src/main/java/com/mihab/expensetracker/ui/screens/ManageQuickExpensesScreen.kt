package com.mihab.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mihab.expensetracker.data.local.QuickExpenseEntity
import com.mihab.expensetracker.viewmodel.ExpenseViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageQuickExpensesScreen(
    viewModel: ExpenseViewModel,
    onBackClick: () -> Unit
) {
    val quickExpenses by viewModel.quickExpenses.collectAsState()
    val isBengali = Locale.getDefault().language == "bn"
    var showAddDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<QuickExpenseEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isBengali) "দ্রুত খরচ ম্যানেজ করুন" else "Manage Quick Expenses",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Quick Expense")
            }
        }
    ) { padding ->
        if (quickExpenses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(if (isBengali) "কোন দ্রুত খরচ পাওয়া যায়নি।" else "No quick expenses found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(quickExpenses) { expense ->
                    QuickExpenseItem(
                        expense = expense,
                        isBengali = isBengali,
                        onEdit = { editingExpense = expense },
                        onDelete = { viewModel.deleteQuickExpense(expense) }
                    )
                }
            }
        }

        if (showAddDialog) {
            QuickExpenseDialog(
                onDismiss = { showAddDialog = false },
                onSave = { icon, cat, catBn, amount, color ->
                    viewModel.addQuickExpense(icon, cat, catBn, amount, color)
                    showAddDialog = false
                },
                isBengali = isBengali
            )
        }

        if (editingExpense != null) {
            QuickExpenseDialog(
                expense = editingExpense,
                onDismiss = { editingExpense = null },
                onSave = { icon, cat, catBn, amount, color ->
                    viewModel.updateQuickExpense(editingExpense!!.copy(
                        icon = icon,
                        category = cat,
                        categoryBn = catBn,
                        amount = amount,
                        color = color
                    ))
                    editingExpense = null
                },
                isBengali = isBengali
            )
        }
    }
}

@Composable
fun QuickExpenseItem(
    expense: QuickExpenseEntity,
    isBengali: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val quickColor = Color(expense.color)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = quickColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(quickColor.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(expense.icon, fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        if (isBengali) expense.categoryBn else expense.category,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = quickColor
                    )
                    Text(
                        "Amount: ${expense.amount}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickExpenseDialog(
    expense: QuickExpenseEntity? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double, Int) -> Unit,
    isBengali: Boolean
) {
    var icon by remember { mutableStateOf(expense?.icon ?: "💰") }
    var category by remember { mutableStateOf(expense?.category ?: "") }
    var categoryBn by remember { mutableStateOf(expense?.categoryBn ?: "") }
    var amount by remember { mutableStateOf(expense?.amount?.toString() ?: "") }
    
    // Default color for new ones
    var color by remember { mutableStateOf(expense?.color ?: 0xFF6200EE.toInt()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (expense == null) (if (isBengali) "নতুন যোগ করুন" else "Add New") else (if (isBengali) "এডিট করুন" else "Edit Quick Expense")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it },
                    label = { Text(if (isBengali) "ইমোজি" else "Emoji Icon") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(if (isBengali) "ক্যাটাগরি (English)" else "Category (English)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = categoryBn,
                    onValueChange = { categoryBn = it },
                    label = { Text(if (isBengali) "ক্যাটাগরি (বাংলা)" else "Category (Bengali)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(if (isBengali) "পরিমাণ" else "Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull() ?: 0.0
                    onSave(icon, category, categoryBn, amountVal, color)
                },
                enabled = category.isNotBlank() && amount.isNotBlank()
            ) {
                Text(if (isBengali) "সেভ" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBengali) "বাতিল" else "Cancel")
            }
        }
    )
}
