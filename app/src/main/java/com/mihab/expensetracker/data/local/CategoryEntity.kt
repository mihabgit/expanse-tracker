package com.mihab.expensetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val nameBn: String,
    val icon: String,
    val color: Int = 0xFF6200EE.toInt(),
    val isDefault: Boolean = false
)
