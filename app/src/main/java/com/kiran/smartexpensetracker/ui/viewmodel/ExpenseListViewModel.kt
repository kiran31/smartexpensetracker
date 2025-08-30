package com.kiran.smartexpensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiran.smartexpensetracker.data.local.Expense
import com.kiran.smartexpensetracker.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

enum class Grouping { TIME, CATEGORY }

@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    repository: ExpenseRepository
) : ViewModel() {

    private val _grouping = MutableStateFlow(Grouping.TIME)
    val grouping: StateFlow<Grouping> = _grouping.asStateFlow()

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val expenses: StateFlow<Map<String, List<Expense>>> =
        combine(
            repository.getAllExpenses(),
            _grouping,
            _selectedDate,
            _searchQuery
        ) { expenses, grouping, date, query ->
            val dateFiltered = expenses.filter { isSameDay(it.date, date) }

            val searchFiltered = if (query.isBlank()) {
                dateFiltered
            } else {
                dateFiltered.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.notes?.contains(query, ignoreCase = true) == true
                }
            }

            when (grouping) {
                Grouping.CATEGORY -> searchFiltered.groupBy { it.category }
                Grouping.TIME -> mapOf("Expenses for Selected Day" to searchFiltered)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val totalAmount: StateFlow<Double> = expenses.map { it.values.flatten().sumOf { exp -> exp.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalCount: StateFlow<Int> = expenses.map { it.values.flatten().size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setGrouping(newGrouping: Grouping) { _grouping.value = newGrouping }
    fun setSelectedDate(newDate: Long) { _selectedDate.value = newDate }
    fun onSearchQueryChange(newQuery: String) { _searchQuery.value = newQuery }

    private fun isSameDay(date1: Long, date2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = date1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}