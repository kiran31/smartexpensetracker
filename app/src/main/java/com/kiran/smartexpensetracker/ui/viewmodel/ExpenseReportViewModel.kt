package com.kiran.smartexpensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiran.smartexpensetracker.data.local.Expense
import com.kiran.smartexpensetracker.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ExpenseReportViewModel @Inject constructor(
    repository: ExpenseRepository
) : ViewModel() {
    private val sevenDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6); set(Calendar.HOUR_OF_DAY, 0) }.timeInMillis
    private val todayEnd = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 23) }.timeInMillis

    private val last7DaysExpenses: StateFlow<List<Expense>> = repository.getExpensesForDateRange(sevenDaysAgo, todayEnd)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyTotals: StateFlow<Map<String, Double>> = last7DaysExpenses.map { expenses ->
        val sortedDays = (0..6).map {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -it) }
            SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(cal.time)
        }.reversed()

        val dailyMap = expenses.groupBy { SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(it.date) }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        sortedDays.associateWith { dailyMap[it] ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())


    val categoryTotals: StateFlow<Map<String, Double>> = last7DaysExpenses.map { expenses ->
        expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
}