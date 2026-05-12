package com.mihab.expensetracker.data.repository

import com.mihab.expensetracker.data.local.CategoryDao
import com.mihab.expensetracker.data.local.CategoryEntity
import com.mihab.expensetracker.data.local.ExpenseDao
import com.mihab.expensetracker.data.local.ExpenseEntity
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val dao: ExpenseDao,
    private val categoryDao: CategoryDao
) {

    fun getExpenses(): Flow<List<ExpenseEntity>> =
        dao.getAllExpenses()

    fun getRecentExpenses(limit: Int): Flow<List<ExpenseEntity>> =
        dao.getRecentExpenses(limit)

    fun getTodayTotal(startOfDay: Long, endOfDay: Long): Flow<Double?> =
        dao.getTodayTotal(startOfDay, endOfDay)

    fun getCategoryTotals(startTime: Long, endTime: Long): Flow<List<com.mihab.expensetracker.data.local.CategoryTotal>> =
        dao.getCategoryTotalsByTimeRange(startTime, endTime)

    suspend fun addExpense(expense: ExpenseEntity) =
        dao.insertExpense(expense)

    suspend fun updateExpense(expense: ExpenseEntity) =
        dao.updateExpense(expense)

    suspend fun deleteExpense(expense: ExpenseEntity) =
        dao.deleteExpense(expense)

    suspend fun deleteExpenseById(id: Int) =
        dao.deleteExpenseById(id)

    suspend fun getExpenseById(id: Int) =
        dao.getExpenseById(id)

    fun getAllQuickExpenses(): Flow<List<com.mihab.expensetracker.data.local.QuickExpenseEntity>> =
        dao.getAllQuickExpenses()

    suspend fun insertQuickExpense(quickExpense: com.mihab.expensetracker.data.local.QuickExpenseEntity) =
        dao.insertQuickExpense(quickExpense)

    suspend fun updateQuickExpense(quickExpense: com.mihab.expensetracker.data.local.QuickExpenseEntity) =
        dao.updateQuickExpense(quickExpense)

    suspend fun deleteQuickExpense(quickExpense: com.mihab.expensetracker.data.local.QuickExpenseEntity) =
        dao.deleteQuickExpense(quickExpense)

    suspend fun getQuickExpensesCount(): Int =
        dao.getQuickExpensesCount()

    suspend fun getExpenseCountInTimeRange(startOfDay: Long, endOfDay: Long): Int =
        dao.getExpenseCountInTimeRange(startOfDay, endOfDay)

    suspend fun getTotalInTimeRange(startOfDay: Long, endOfDay: Long): Double? =
        dao.getTotalInTimeRange(startOfDay, endOfDay)

    // Category Methods
    fun getAllCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getAllCategories()

    suspend fun addCategory(category: CategoryEntity) =
        categoryDao.insertCategory(category)

    suspend fun updateCategory(category: CategoryEntity) =
        categoryDao.updateCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) =
        categoryDao.deleteCategory(category)

    suspend fun getCategoryCount(): Int =
        categoryDao.getCategoryCount()

    suspend fun insertCategories(categories: List<CategoryEntity>) =
        categoryDao.insertCategories(categories)
}
