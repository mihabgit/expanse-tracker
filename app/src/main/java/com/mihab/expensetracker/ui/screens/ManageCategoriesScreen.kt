package com.mihab.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mihab.expensetracker.data.local.CategoryEntity
import com.mihab.expensetracker.viewmodel.ExpenseViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(
    viewModel: ExpenseViewModel,
    onBackClick: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val isBengali = Locale.getDefault().language == "bn"
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isBengali) "ক্যাটাগরি ম্যানেজ করুন" else "Manage Categories",
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
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { padding ->
        if (categories.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(if (isBengali) "কোন ক্যাটাগরি পাওয়া যায়নি।" else "No categories found.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
            ) {
                items(categories) { category ->
                    CategoryItem(
                        category = category,
                        isBengali = isBengali,
                        onEdit = { editingCategory = category },
                        onDelete = { viewModel.deleteCategory(category) }
                    )
                }
            }
        }

        if (showAddDialog) {
            CategoryDialog(
                onDismiss = { showAddDialog = false },
                onSave = { name, nameBn, icon, color ->
                    viewModel.addCategory(name, nameBn, icon, color)
                    showAddDialog = false
                },
                isBengali = isBengali
            )
        }

        if (editingCategory != null) {
            CategoryDialog(
                category = editingCategory,
                onDismiss = { editingCategory = null },
                onSave = { name, nameBn, icon, color ->
                    viewModel.updateCategory(editingCategory!!.copy(
                        name = name,
                        nameBn = nameBn,
                        icon = icon,
                        color = color
                    ))
                    editingCategory = null
                },
                isBengali = isBengali
            )
        }
    }
}

@Composable
fun CategoryItem(
    category: CategoryEntity,
    isBengali: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = Color(category.color)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = categoryColor.copy(alpha = 0.1f)
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
                        .background(categoryColor.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(category.icon, fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        if (isBengali) category.nameBn else category.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = categoryColor
                    )
                    if (category.isDefault) {
                        Text(
                            if (isBengali) "ডিফল্ট" else "Default",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
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
fun CategoryDialog(
    category: CategoryEntity? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int) -> Unit,
    isBengali: Boolean
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var nameBn by remember { mutableStateOf(category?.nameBn ?: "") }
    var icon by remember { mutableStateOf(category?.icon ?: "💰") }
    var color by remember { mutableStateOf(category?.color ?: 0xFF6200EE.toInt()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) (if (isBengali) "নতুন ক্যাটাগরি" else "Add New Category") else (if (isBengali) "ক্যাটাগরি এডিট" else "Edit Category")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it },
                    label = { Text(if (isBengali) "ইমোজি" else "Emoji Icon") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isBengali) "নাম (English)" else "Name (English)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text(if (isBengali) "নাম (বাংলা)" else "Name (Bengali)") },
                    modifier = Modifier.fillMaxWidth()
                )
                // In a real app, you might want a color picker here. 
                // For now, let's just use a simple text field or a few color options.
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name, nameBn, icon, color)
                },
                enabled = name.isNotBlank() && nameBn.isNotBlank() && icon.isNotBlank()
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
