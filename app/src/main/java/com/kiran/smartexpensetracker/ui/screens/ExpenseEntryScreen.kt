package com.kiran.smartexpensetracker.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiran.smartexpensetracker.ui.viewmodel.AddExpenseResult
import com.kiran.smartexpensetracker.ui.viewmodel.ExpenseEntryViewModel
import com.kiran.smartexpensetracker.ui.viewmodel.UIMode
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ExpenseEntryScreen(onBack: () -> Unit, viewModel: ExpenseEntryViewModel = hiltViewModel()) {
    val uiMode by viewModel.uiMode.collectAsState()
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var expenseId by remember { mutableStateOf(-1) }
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Staff", "Travel", "Food", "Utility")
    var showDeleteDialog by remember { mutableStateOf(false) }
    val totalSpentToday by viewModel.totalSpentToday.collectAsState()
    var submissionState by remember { mutableStateOf<AddExpenseResult?>(null) }

    LaunchedEffect(uiMode) {
        if (uiMode is UIMode.Edit) {
            val expense = (uiMode as UIMode.Edit).expense
            expenseId = expense.id
            title = expense.title
            amount = expense.amount.toString()
            category = expense.category
            notes = expense.notes ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiMode is UIMode.Edit) "Edit Expense" else "Add Expense") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    if (uiMode is UIMode.Edit) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Spent Today", style = MaterialTheme.typography.titleMedium)
                    Text(NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(totalSpentToday),
                        style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title*") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount*") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(16.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(readOnly = true, value = category, onValueChange = {}, label = { Text("Category*") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth())
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach { selectionOption ->
                        DropdownMenuItem(text = { Text(selectionOption) }, onClick = { category = selectionOption; expanded = false })
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = notes, onValueChange = { if (it.length <= 100) notes = it },
                label = { Text("Optional Notes") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = { Toast.makeText(context, "Feature not yet implemented", Toast.LENGTH_SHORT).show() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = "Upload", modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Upload Receipt (Optional)")
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (title.isBlank() || amountValue == null || amountValue <= 0) {
                        Toast.makeText(context, "Please enter a valid title and amount", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.saveExpense(expenseId, title, amountValue, category, notes)
                        val message = if (expenseId == -1) "Expense Added!" else "Expense Updated!"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(if (uiMode is UIMode.Edit) "Update Expense" else "Save Expense")
            }

            AnimatedVisibility(
                visible = submissionState is AddExpenseResult.Success,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {

                LaunchedEffect(submissionState) {
                    delay(400)
                    onBack()
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete this expense? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExpense()
                        showDeleteDialog = false
                        Toast.makeText(context, "Expense Deleted", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}