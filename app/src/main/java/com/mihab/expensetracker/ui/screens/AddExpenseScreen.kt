package com.mihab.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mihab.expensetracker.data.local.ExpenseEntity
import com.mihab.expensetracker.util.LocaleHelper
import com.mihab.expensetracker.viewmodel.ExpenseViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    expenseId: Int? = null,
    viewModel: ExpenseViewModel? = null,
    onSave: (Double, String, String?) -> Unit,
    onUpdate: (ExpenseEntity) -> Unit,
    onBackClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currency = LocaleHelper.getCurrency(context)
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food") }
    var showCustomCategoryDialog by remember { mutableStateOf(false) }
    var customCategoryName by remember { mutableStateOf("") }
    var existingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

    val isBengali = Locale.getDefault().language == "bn"

    LaunchedEffect(expenseId) {
        if (expenseId != null && viewModel != null) {
            val expense = viewModel.getExpenseById(expenseId)
            if (expense != null) {
                existingExpense = expense
                amount = expense.amount.toString()
                note = expense.note ?: ""
                selectedCategory = expense.category
            }
        }
    }

    val defaultCategories = remember {
        mutableStateListOf(
            "Food" to "🍔",
            "Transport" to "🚌",
            "Rent" to "🏠",
            "Shopping" to "🛒",
            "Bills" to "📄",
            "Entertainment" to "🎬",
            "Health" to "💊",
            "Education" to "📚",
            "Investment" to "📈",
            "Gifts" to "🎁",
            "Travel" to "✈️",
            "Other" to "💰"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (expenseId != null) {
                            if (isBengali) "খরচ সম্পাদনা" else "Edit Expense"
                        } else {
                            if (isBengali) "খরচ যোগ করুন" else "Add Expense"
                        },
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
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // User-friendly "How Much" Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (isBengali) "আপনি কত খরচ করেছেন?" else "How much did you spend?",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            currency,
                            style = TextStyle(
                                fontSize = 40.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = amount,
                            onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amount = it },
                            placeholder = { 
                                Text(
                                    "0.00", 
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                ) 
                            },
                            textStyle = TextStyle(
                                fontSize = 40.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.width(IntrinsicSize.Min),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Category Selection Header with "Add Custom"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isBengali) "বিভাগ নির্বাচন করুন" else "Select Category",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = { showCustomCategoryDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBengali) "নতুন" else "Custom")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            // Visual Category Grid (Manual Row implementation for scrollable column)
            defaultCategories.chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { (name, emoji) ->
                        val isSelected = selectedCategory == name
                        Surface(
                            onClick = { selectedCategory = name },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .height(85.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(emoji, fontSize = 24.sp)
                                Text(
                                    getCategoryName(name, isBengali), 
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    // Fill empty spaces in the last row if needed
                    if (rowItems.size < 3) {
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Note Input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text(if (isBengali) "এটি কিসের জন্য?" else "What is this for?") },
                label = { Text(if (isBengali) "নোট যোগ করুন (ঐচ্ছিক)" else "Add Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Default.Create, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()
                    if (parsedAmount != null && parsedAmount > 0) {
                        if (existingExpense != null) {
                            onUpdate(existingExpense!!.copy(
                                amount = parsedAmount,
                                category = selectedCategory,
                                note = note.ifBlank { null }
                            ))
                        } else {
                            onSave(parsedAmount, selectedCategory, note.ifBlank { null })
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (expenseId != null) {
                            if (isBengali) "হালনাগাদ করুন" else "Update Expense"
                        } else {
                            if (isBengali) "খরচ সংরক্ষণ করুন" else "Save Expense"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp)) // Extra space for keyboard/scrolling
        }
    }

    // Custom Category Dialog
    if (showCustomCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCustomCategoryDialog = false },
            title = { Text(if (isBengali) "নতুন বিভাগ যোগ করুন" else "Add Custom Category") },
            text = {
                OutlinedTextField(
                    value = customCategoryName,
                    onValueChange = { customCategoryName = it },
                    label = { Text(if (isBengali) "বিভাগের নাম" else "Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customCategoryName.isNotBlank()) {
                            defaultCategories.add(customCategoryName to "🏷️")
                            selectedCategory = customCategoryName
                            customCategoryName = ""
                            showCustomCategoryDialog = false
                        }
                    }
                ) {
                    Text(if (isBengali) "যোগ করুন" else "Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomCategoryDialog = false }) {
                    Text(if (isBengali) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
