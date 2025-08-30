package com.kiran.smartexpensetracker.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kiran.smartexpensetracker.ui.viewmodel.ExpenseReportViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseReportScreen(onBack: () -> Unit, viewModel: ExpenseReportViewModel = hiltViewModel()) {
    val dailyTotals by viewModel.dailyTotals.collectAsState()
    val categoryTotals by viewModel.categoryTotals.collectAsState()
    val totalForWeek = categoryTotals.values.sum()
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(
            title = { Text("Last 7 Days Report") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
            actions = {
                IconButton(onClick = {
                    val reportText = generateReportText(totalForWeek, categoryTotals, dailyTotals)
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, reportText)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Export Report")
                    context.startActivity(shareIntent)
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Export Report")
                }
            }
        ) }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp)) {
            item { ReportSummaryCard(totalForWeek); Spacer(Modifier.height(24.dp)) }
            item { SectionHeader("Category Breakdown"); CategoryTotalsList(categoryTotals); Spacer(Modifier.height(24.dp)) }
            item { SectionHeader("Daily Breakdown"); BarChart(dailyTotals); Spacer(Modifier.height(24.dp)) }
            items(dailyTotals.entries.toList()) { (date, total) -> DailyTotalItem(date, total) }
        }
    }
}

private fun generateReportText(total: Double, categories: Map<String, Double>, dailies: Map<String, Double>): String {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return buildString {
        append("--- Expense Report (Last 7 Days) ---\n\n")
        append("TOTAL SPENT: ${currencyFormat.format(total)}\n\n")
        append("--- By Category ---\n")
        categories.forEach { (cat, amt) -> append("- $cat: ${currencyFormat.format(amt)}\n") }
        append("\n--- By Day ---\n")
        dailies.forEach { (day, amt) -> append("- $day: ${currencyFormat.format(amt)}\n") }
    }
}

@Composable
fun BarChart(data: Map<String, Double>) {
    val maxAmount = data.values.maxOrNull() ?: 1.0
    Card(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { (day, amount) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    val percentage = (amount / maxAmount).toFloat()
                    Box(
                        modifier = Modifier
                            .height(120.dp * percentage)
                            .width(20.dp)
                            .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(day.take(3), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}


@Composable
fun CategoryTotalsList(totals: Map<String, Double>) {
    val totalValue = totals.values.sum()
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            if (totals.isEmpty()) Text("No data for this period.") else {
                totals.entries.sortedByDescending { it.value }.forEach { (cat, amt) ->
                    CategoryTotalItem(cat, amt, if (totalValue > 0) (amt / totalValue).toFloat() else 0f)
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ReportSummaryCard(total: Double) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Total Spent This Week", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(8.dp))
            Text(NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(total), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 12.dp, top = 8.dp))
}

@Composable
fun CategoryTotalItem(category: String, amount: Double, percentage: Float) {
    val color = when (category) { "Food" -> Color(0xFFFFA726); "Travel" -> Color(0xFF42A5F5); "Utility" -> Color(0xFF26A69A); "Staff" -> Color(0xFF7E57C2); else -> Color.Gray }
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(category, fontWeight = FontWeight.Medium)
            }
            Text(NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(amount))
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(progress = { percentage }, modifier = Modifier.fillMaxWidth().clip(CircleShape), color = color)
    }
}

@Composable
fun DailyTotalItem(date: String, total: Double) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(date, style = MaterialTheme.typography.bodyLarge)
        Text(NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(total), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
    Divider()
}