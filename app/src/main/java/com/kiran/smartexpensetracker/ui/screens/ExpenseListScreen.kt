package com.kiran.smartexpensetracker.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiran.smartexpensetracker.data.local.Expense
import com.kiran.smartexpensetracker.ui.viewmodel.ExpenseListViewModel
import com.kiran.smartexpensetracker.ui.viewmodel.Grouping
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExpenseListScreen(
    onAddExpense: () -> Unit,
    onShowReport: () -> Unit,
    onEditExpense: (Int) -> Unit,
    viewModel: ExpenseListViewModel = hiltViewModel()
) {
    val expensesByGroup by viewModel.expenses.collectAsState()
    val totalAmount by viewModel.totalAmount.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val currentGrouping by viewModel.grouping.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
    val datePickerDialog = DatePickerDialog(context, { _: DatePicker, y, m, d ->
        viewModel.setSelectedDate(Calendar.getInstance().apply { set(y, m, d) }.timeInMillis)
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Smart Expense Tracker") }, actions = {
                IconButton(onClick = onShowReport) { Icon(Icons.Default.Assessment, "Reports") }
                IconButton(onClick = { datePickerDialog.show() }) { Icon(Icons.Default.DateRange, "Select Date") }
            })
        },
        floatingActionButton = { FloatingActionButton(onClick = onAddExpense) { Icon(Icons.Default.Add, "Add Expense") } }
    ) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by title or notes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )
            Header(totalAmount, totalCount, selectedDate, currentGrouping, viewModel::setGrouping)
            if (expensesByGroup.values.all { it.isEmpty() }) { EmptyState(searchQuery.isNotBlank()) }
            else {
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp)) {
                    expensesByGroup.forEach { (group, expensesInGroup) ->
                        stickyHeader { GroupHeader(group) }
                        items(expensesInGroup, key = { it.id }) { expense ->
                            ExpenseListItem(expense, onClick = { onEditExpense(expense.id) })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header(totalAmount: Double, totalCount: Int, date: Long, activeGrouping: Grouping, onGroupChange: (Grouping) -> Unit) {
    Card(Modifier.fillMaxWidth().padding(8.dp), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault()).format(Date(date)), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(totalAmount), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("$totalCount transactions", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    onClick = { onGroupChange(Grouping.TIME) },
                    selected = activeGrouping == Grouping.TIME
                ) { Text("By Time") }
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    onClick = { onGroupChange(Grouping.CATEGORY) },
                    selected = activeGrouping == Grouping.CATEGORY
                ) { Text("Category") }
            }
        }
    }
}

@Composable
fun GroupHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)).padding(horizontal = 16.dp, vertical = 8.dp))
}

@Composable
fun ExpenseListItem(expense: Expense, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp).clickable(onClick = onClick),) {
        Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = getCategoryIcon(expense.category),
                contentDescription = expense.category,
                modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape).padding(8.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(expense.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (!expense.notes.isNullOrBlank()) {
                    Text(expense.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(expense.amount), fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary, fontSize = 16.sp)
        }
    }
}

@Composable
fun EmptyState(isSearching: Boolean) {
    val message = if (isSearching) "No results found." else "No expenses recorded for this day.\nTap '+' to add one!"
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.ReceiptLong, "No expenses", Modifier.size(64.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(Modifier.height(16.dp))
            Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Food" -> Icons.Default.Fastfood
        "Travel" -> Icons.Default.Commute
        "Utility" -> Icons.Default.Receipt
        "Staff" -> Icons.Default.People
        else -> Icons.Default.Money
    }
}