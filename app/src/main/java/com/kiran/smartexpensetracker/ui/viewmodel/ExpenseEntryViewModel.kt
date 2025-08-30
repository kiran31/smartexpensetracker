package com.kiran.smartexpensetracker.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiran.smartexpensetracker.data.local.Expense
import com.kiran.smartexpensetracker.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

sealed class AddExpenseResult {
    data object Success : AddExpenseResult()
    data object Duplicate : AddExpenseResult()
    data object Error : AddExpenseResult()
}

sealed class UIMode {
    data object New : UIMode()
    data class Edit(val expense: Expense) : UIMode()
}

@HiltViewModel
class ExpenseEntryViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val expenseId: Int = savedStateHandle.get<Int>("expenseId") ?: -1

    private val _uiMode = MutableStateFlow<UIMode>(UIMode.New)
    val uiMode: StateFlow<UIMode> = _uiMode.asStateFlow()

    val totalSpentToday: StateFlow<Double> =
        repository.getTotalSpentForDay(getStartOfDay(), getEndOfDay())
            .map { it ?: 0.0 }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0.0
            )

    init {
        if (expenseId != -1) {
            viewModelScope.launch {
                repository.getExpenseById(expenseId)?.let { expense ->
                    _uiMode.value = UIMode.Edit(expense)
                }
            }
        }
    }

    fun saveExpense(id: Int, title: String, amount: Double, category: String, notes: String?) {
        viewModelScope.launch {
            val expense = Expense(
                id = if (id == -1) 0 else id, // Let Room auto-generate for new entries
                title = title.trim(),
                amount = amount,
                category = category,
                date = System.currentTimeMillis(),
                notes = notes?.trim()
            )
            if (id == -1) {
                repository.addExpense(expense)
            } else {
                repository.updateExpense(expense)
            }
        }
    }

    fun deleteExpense() {
        viewModelScope.launch {
            if (uiMode.value is UIMode.Edit) {
                repository.deleteExpense((uiMode.value as UIMode.Edit).expense)
            }
        }
    }


    fun addExpense(title: String, amount: Double, category: String, notes: String?, result: (AddExpenseResult) -> Unit) {
        viewModelScope.launch {
            val twoMinutesAgo = System.currentTimeMillis() - 2 * 60 * 1000
            val isDuplicate = repository.doesExpenseExist(title, amount, category, twoMinutesAgo)

            if (isDuplicate) {
                result(AddExpenseResult.Duplicate)
                return@launch
            }
            if (title.isNotBlank() && amount > 0) {
                repository.addExpense(
                    Expense(
                        title = title.trim(),
                        amount = amount,
                        category = category,
                        date = System.currentTimeMillis(),
                        notes = notes?.trim()
                    )
                )
                result(AddExpenseResult.Success)
            } else {
                result(AddExpenseResult.Error)
            }
        }
    }
    private fun getStartOfDay(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    private fun getEndOfDay(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
}