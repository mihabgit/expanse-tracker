package com.mihab.expensetracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insertExpense(expense: ExpenseEntity)

    @androidx.room.Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @androidx.room.Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Int): ExpenseEntity?

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Int)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY date DESC LIMIT :limit")
    fun getRecentExpenses(limit: Int): Flow<List<ExpenseEntity>>

    @Query("""
        SELECT SUM(amount) FROM expenses 
        WHERE date >= :startOfDay AND date <= :endOfDay
    """)
    fun getTodayTotal(startOfDay: Long, endOfDay: Long): Flow<Double?>

    @Query("""
        SELECT SUM(amount) FROM expenses 
        WHERE strftime('%m', date / 1000, 'unixepoch') = :month
    """)
    fun getMonthlyTotal(month: String): Flow<Double?>

    @Query("""
        SELECT category, SUM(amount) as totalAmount FROM expenses 
        WHERE date >= :startTime AND date <= :endTime
        GROUP BY category
    """)
    fun getCategoryTotalsByTimeRange(startTime: Long, endTime: Long): Flow<List<CategoryTotal>>
}

data class CategoryTotal(
    val category: String,
    val totalAmount: Double
)
