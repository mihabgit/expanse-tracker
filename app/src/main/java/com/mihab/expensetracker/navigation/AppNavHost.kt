package com.mihab.expensetracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mihab.expensetracker.data.local.ExpenseEntity
import com.mihab.expensetracker.ui.screens.AddExpenseScreen
import com.mihab.expensetracker.ui.screens.ExpenseListScreen
import com.mihab.expensetracker.ui.screens.HomeScreen
import com.mihab.expensetracker.ui.screens.ManageQuickExpensesScreen
import com.mihab.expensetracker.ui.screens.OnboardingScreen
import com.mihab.expensetracker.ui.screens.Screen
import com.mihab.expensetracker.ui.screens.SettingsScreen
import com.mihab.expensetracker.ui.screens.SummaryScreen
import com.mihab.expensetracker.viewmodel.ExpenseViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: ExpenseViewModel,
    startDestination: String = Screen.Home.route,
    onLanguageChanged: () -> Unit,
    onOnboardingFinished: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = onOnboardingFinished,
                showSkip = startDestination != Screen.Onboarding.route
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = {
                    navController.navigate(Screen.AddExpense.route)
                },
                onSeeAllClick = {
                    navController.navigate(Screen.ExpenseList.route)
                },
                onSummaryClick = {
                    navController.navigate(Screen.Summary.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onManageQuickClick = {
                    navController.navigate(Screen.ManageQuickExpenses.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLanguageChanged = onLanguageChanged,
                onCurrencyChanged = onLanguageChanged, // reusing language change trigger for refreshing UI
                onManageQuickExpensesClick = {
                    navController.navigate(Screen.ManageQuickExpenses.route)
                },
                onTutorialClick = {
                    navController.navigate(Screen.Onboarding.route)
                }
            )
        }

        composable(Screen.ManageQuickExpenses.route) {
            ManageQuickExpensesScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Summary.route) {
            SummaryScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ExpenseList.route) {
            ExpenseListScreen(
                viewModel = viewModel,
                onAddClick = {
                    navController.navigate(Screen.AddExpense.route)
                },
                onEditClick = { id ->
                    navController.navigate("${Screen.AddExpense.route}?expenseId=$id")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "${Screen.AddExpense.route}?expenseId={expenseId}",
            arguments = listOf(
                androidx.navigation.navArgument("expenseId") {
                    type = androidx.navigation.NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getInt("expenseId") ?: -1
            AddExpenseScreen(
                expenseId = if (expenseId != -1) expenseId else null,
                viewModel = viewModel,
                onSave = { amount, category, note, date ->
                    viewModel.addExpense(amount, category, note, date)
                    navController.popBackStack()
                },
                onUpdate = { expense: ExpenseEntity ->
                    viewModel.updateExpense(expense)
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
