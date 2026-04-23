package com.mihab.expensetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quick_expenses")
data class QuickExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val icon: String,
    val category: String,
    val categoryBn: String,
    val amount: Double,
    val color: Int // Store color as Int (ARGB)
)
