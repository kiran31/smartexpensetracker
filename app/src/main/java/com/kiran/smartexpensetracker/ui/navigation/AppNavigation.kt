package com.kiran.smartexpensetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kiran.smartexpensetracker.ui.screens.ExpenseEntryScreen
import com.kiran.smartexpensetracker.ui.screens.ExpenseListScreen
import com.kiran.smartexpensetracker.ui.screens.ExpenseReportScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "expense_list") {
        composable("expense_list") {
            ExpenseListScreen(
                onAddExpense = { navController.navigate("expense_entry") },
                onShowReport = { navController.navigate("report") },
                onEditExpense = { expenseId ->
                    navController.navigate("expense_entry?expenseId=$expenseId")
                }
            )
        }
        composable(
            route = "expense_entry?expenseId={expenseId}",
            arguments = listOf(navArgument("expenseId") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) {
            ExpenseEntryScreen(onBack = { navController.popBackStack() })
        }

        composable("report") {
            ExpenseReportScreen(onBack = { navController.popBackStack() })
        }
    }
}