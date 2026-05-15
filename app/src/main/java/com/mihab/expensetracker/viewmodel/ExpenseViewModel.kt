package com.mihab.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mihab.expensetracker.data.local.CategoryEntity
import com.mihab.expensetracker.data.local.ExpenseEntity
import com.mihab.expensetracker.data.local.QuickExpenseEntity
import com.mihab.expensetracker.data.repository.ExpenseRepository
import com.mihab.expensetracker.util.BKashSmsParser
import com.mihab.expensetracker.util.BKashTransaction
import com.mihab.expensetracker.util.NotificationHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class ExpenseViewModel(
    private val repository: ExpenseRepository,
    private val notificationHelper: NotificationHelper? = null
) : ViewModel() {

    private val _bkashTransactions = MutableStateFlow<List<BKashTransaction>>(emptyList())
    val bkashTransactions = _bkashTransactions.asStateFlow()

    fun loadBKashTransactions(context: android.content.Context) {
        viewModelScope.launch {
            val transactions = mutableListOf<BKashTransaction>()
            val uri = android.net.Uri.parse("content://sms/inbox")
            
            // STRICT FILTER: Only query messages where the sender address is exactly "bKash"
            // This prevents the app from even seeing personal messages in the cursor.
            val cursor = try {
                context.contentResolver.query(
                    uri,
                    arrayOf("body", "date", "address"),
                    "address = ?",
                    arrayOf("bKash"),
                    "date DESC"
                )
            } catch (e: Exception) {
                null
            }

            cursor?.use {
                val bodyIndex = it.getColumnIndex("body")
                val dateIndex = it.getColumnIndex("date")
                val addressIndex = it.getColumnIndex("address")
                
                while (it.moveToNext()) {
                    val address = it.getString(addressIndex)
                    
                    // DOUBLE VERIFICATION: Extra check to ensure we only process bKash messages
                    if (address?.equals("bKash", ignoreCase = true) == true) {
                        val body = it.getString(bodyIndex)
                        val date = it.getLong(dateIndex)
                        BKashSmsParser.parse(body, date)?.let { transaction ->
                            transactions.add(transaction)
                        }
                    }
                }
            }
            _bkashTransactions.value = transactions
        }
    }

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

    val thisMonthTotal = repository.getTodayTotal(getCurrentMonthRange().first, getCurrentMonthRange().second)
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

    val categories = repository.getAllCategories().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _showSpendingDialog = MutableStateFlow<String?>(null)
    val showSpendingDialog = _showSpendingDialog.asStateFlow()

    private var isSpendingAlertEnabled = true
    private var onDisableAlerts: (() -> Unit)? = null

    fun setSpendingAlertEnabled(enabled: Boolean, onDisable: () -> Unit) {
        isSpendingAlertEnabled = enabled
        onDisableAlerts = onDisable
    }

    fun dismissSpendingDialog(dontShowAgain: Boolean = false) {
        _showSpendingDialog.value = null
        if (dontShowAgain) {
            isSpendingAlertEnabled = false
            onDisableAlerts?.invoke()
        }
    }

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

            if (repository.getCategoryCount() == 0) {
                val defaultCategories = listOf(
                    CategoryEntity(name = "Food", nameBn = "খাবার", icon = "🍔", isDefault = true, color = 0xFFFF9800.toInt()),
                    CategoryEntity(name = "Transport", nameBn = "পরিবহন", icon = "🚌", isDefault = true, color = 0xFF2196F3.toInt()),
                    CategoryEntity(name = "Rent", nameBn = "ভাড়া", icon = "🏠", isDefault = true, color = 0xFF795548.toInt()),
                    CategoryEntity(name = "Grocery", nameBn = "মুদি", icon = "🛒", isDefault = true, color = 0xFFE91E63.toInt()),
                    CategoryEntity(name = "Shopping", nameBn = "শপিং", icon = "🛍️", isDefault = true, color = 0xFFE91E63.toInt()),
                    CategoryEntity(name = "Bills", nameBn = "বিল", icon = "📄", isDefault = true, color = 0xFF607D8B.toInt()),
                    CategoryEntity(name = "Entertainment", nameBn = "বিনোদন", icon = "🎬", isDefault = true, color = 0xFF9C27B0.toInt()),
                    CategoryEntity(name = "Health", nameBn = "স্বাস্থ্য", icon = "💊", isDefault = true, color = 0xFFF44336.toInt()),
                    CategoryEntity(name = "Education", nameBn = "শিক্ষা", icon = "📚", isDefault = true, color = 0xFF3F51B5.toInt()),
                    CategoryEntity(name = "Investment", nameBn = "বিনিয়োগ", icon = "📈", isDefault = true, color = 0xFF4CAF50.toInt()),
                    CategoryEntity(name = "Gifts", nameBn = "উপহার", icon = "🎁", isDefault = true, color = 0xFFFF5722.toInt()),
                    CategoryEntity(name = "Travel", nameBn = "ভ্রমণ", icon = "✈️", isDefault = true, color = 0xFF00BCD4.toInt()),
                    CategoryEntity(name = "Other", nameBn = "অন্যান্য", icon = "💰", isDefault = true, color = 0xFF9E9E9E.toInt())
                )
                repository.insertCategories(defaultCategories)
            }
        }
    }

    fun addCategory(name: String, nameBn: String, icon: String, color: Int) {
        viewModelScope.launch {
            repository.addCategory(CategoryEntity(name = name, nameBn = nameBn, icon = icon, color = color))
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
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
            
            // Check if spend today > spend yesterday for notification
            checkSpendingAlert()
        }
    }

    private suspend fun checkSpendingAlert() {
        if (!isSpendingAlertEnabled) return

        val todayStart = getStartOfDay()
        val todayEnd = getEndOfDay()
        
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayStart = getStartOfDay(yesterday)
        val yesterdayEnd = getEndOfDay(yesterday)
        
        val todayTotalVal = repository.getTotalInTimeRange(todayStart, todayEnd) ?: 0.0
        val yesterdayTotalVal = repository.getTotalInTimeRange(yesterdayStart, yesterdayEnd) ?: 0.0
        
        if (todayTotalVal > yesterdayTotalVal && yesterdayTotalVal > 0) {
            // Logic to prevent multiple notifications in the same day
            // We can check a simple logic or just let it show if user wants it "whenever it exceed"
            // But usually once is enough. Let's stick to showing it for now as requested.

            val isBengali = Locale.getDefault().language == "bn"
            val message = if (isBengali) 
                "সতর্কতা! গতকালকের চেয়ে আজ আপনি বেশি খরচ করেছেন!" 
            else 
                "Spending Alert! You have spent more today than yesterday!"
            
            _showSpendingDialog.value = message
        }
    }

    private fun getStartOfDay(calendar: Calendar): Long {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getEndOfDay(calendar: Calendar): Long {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
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
