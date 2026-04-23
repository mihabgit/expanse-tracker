package com.mihab.expensetracker.ui.screens

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object ExpenseList : Screen("expense_list")
    data object AddExpense : Screen("add_expense")
    data object Summary : Screen("summary")
    data object Settings : Screen("settings")
    data object ManageQuickExpenses : Screen("manage_quick_expenses")
    data object Onboarding : Screen("onboarding")
}
