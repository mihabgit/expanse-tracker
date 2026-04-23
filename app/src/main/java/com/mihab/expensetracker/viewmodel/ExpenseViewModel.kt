package com.mihab.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mihab.expensetracker.data.local.ExpenseEntity
import com.mihab.expensetracker.data.local.QuickExpenseEntity
import com.mihab.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class ExpenseViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    val expenses = repository.getExpenses()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    val recentExpenses = repository.getRecentExpenses(10)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    val todayCount = repository.getExpenses()
        .map { list ->
            val start = getStartOfDay()
            val end = getEndOfDay()
            list.count { it.date in start..end }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0
        )

    val todayTotal = repository.getTodayTotal(getStartOfDay(), getEndOfDay())
        .map { it ?: 0.0 }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0.0
        )

    val quickExpenses = repository.getAllQuickExpenses().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            if (repository.getQuickExpensesCount() == 0) {
                val defaultQuickExpenses = listOf(
                    QuickExpenseEntity(icon = "☕", category = "Coffee", categoryBn = "কফি", amount = 100.0, color = 0xFF795548.toInt()),
                    QuickExpenseEntity(icon = "🚌", category = "Bus Fare", categoryBn = "বাস ভাড়া", amount = 30.0, color = 0xFF2196F3.toInt()),
                    QuickExpenseEntity(icon = "🍔", category = "Lunch", categoryBn = "দুপুরের খাবার", amount = 250.0, color = 0xFFFF9800.toInt()),
                    QuickExpenseEntity(icon = "🍿", category = "Snacks", categoryBn = "নাস্তা", amount = 50.0, color = 0xFFE91E63.toInt()),
                    QuickExpenseEntity(icon = "🛒", category = "Grocery", categoryBn = "মুদি খরচ", amount = 500.0, color = 0xFF4CAF50.toInt())
                )
                defaultQuickExpenses.forEach { repository.insertQuickExpense(it) }
            }
        }
    }

    fun addQuickExpense(icon: String, category: String, categoryBn: String, amount: Double, color: Int) {
        viewModelScope.launch {
            repository.insertQuickExpense(QuickExpenseEntity(icon = icon, category = category, categoryBn = categoryBn, amount = amount, color = color))
        }
    }

    fun updateQuickExpense(quickExpense: QuickExpenseEntity) {
        viewModelScope.launch {
            repository.updateQuickExpense(quickExpense)
        }
    }

    fun deleteQuickExpense(quickExpense: QuickExpenseEntity) {
        viewModelScope.launch {
            repository.deleteQuickExpense(quickExpense)
        }
    }

    private val _selectedMonthRange = MutableStateFlow(getCurrentMonthRange())

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoryTotals = _selectedMonthRange.flatMapLatest { range ->
        repository.getCategoryTotals(range.first, range.second)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun selectMonth(monthOffset: Int) {
        _selectedMonthRange.value = getMonthRange(monthOffset)
    }

    private fun getCurrentMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis

        return start to end
    }

    private fun getMonthRange(monthOffset: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, monthOffset)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis

        return start to end
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    fun addExpense(
        amount: Double,
        category: String,
        note: String?,
        date: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            repository.addExpense(
                ExpenseEntity(
                    amount = amount,
                    category = category,
                    date = date,
                    note = note
                )
            )
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    suspend fun getExpenseById(id: Int) = repository.getExpenseById(id)
}
