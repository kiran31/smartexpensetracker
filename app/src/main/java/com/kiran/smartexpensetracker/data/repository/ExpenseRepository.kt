package com.kiran.smartexpensetracker.data.repository

import com.kiran.smartexpensetracker.data.local.Expense
import com.kiran.smartexpensetracker.data.local.ExpenseDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExpenseRepository @Inject constructor(private val expenseDao: ExpenseDao) {

    suspend fun addExpense(expense: Expense) = expenseDao.insertExpense(expense)
    suspend fun getExpenseById(id: Int): Expense? = expenseDao.getExpenseById(id)
    suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)

    fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses()

    fun getTotalSpentForDay(start: Long, end: Long): Flow<Double?> = expenseDao.getTotalSpentForDay(start, end)
    fun getExpensesForDateRange(start: Long, end: Long): Flow<List<Expense>> = expenseDao.getExpensesForDateRange(start, end)
    suspend fun doesExpenseExist(title: String, amount: Double, category: String, since: Long): Boolean =
        expenseDao.doesExpenseExist(title, amount, category, since)
}