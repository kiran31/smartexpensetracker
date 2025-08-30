package com.kiran.smartexpensetracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Int): Expense?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE date >= :startOfDay AND date < :endOfDay")
    fun getTotalSpentForDay(startOfDay: Long, endOfDay: Long): Flow<Double?>

    @Query("SELECT * FROM expenses WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getExpensesForDateRange(startDate: Long, endDate: Long): Flow<List<Expense>>

    @Query("SELECT EXISTS(SELECT 1 FROM expenses WHERE title = :title AND amount = :amount AND category = :category AND date >= :since)")
    suspend fun doesExpenseExist(title: String, amount: Double, category: String, since: Long): Boolean
}