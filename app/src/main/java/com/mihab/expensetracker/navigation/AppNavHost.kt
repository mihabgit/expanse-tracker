package com.mihab.expensetracker.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mihab.expensetracker.data.local.ExpenseEntity
import com.mihab.expensetracker.ui.screens.AddExpenseScreen
import com.mihab.expensetracker.ui.screens.BKashSummaryScreen
import com.mihab.expensetracker.ui.screens.ExpenseListScreen
import com.mihab.expensetracker.ui.screens.HomeScreen
import com.mihab.expensetracker.ui.screens.ManageCategoriesScreen
import com.mihab.expensetracker.ui.screens.ManageQuickExpensesScreen
import com.mihab.expensetracker.ui.screens.OnboardingScreen
import com.mihab.expensetracker.ui.screens.Screen
import com.mihab.expensetracker.ui.screens.SettingsScreen
import com.mihab.expensetracker.ui.screens.SummaryScreen
import com.mihab.expensetracker.util.LocaleHelper
import com.mihab.expensetracker.viewmodel.ExpenseViewModel
import java.util.Locale

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: ExpenseViewModel,
    startDestination: String = Screen.Home.route,
    themeMode: String = "system",
    onThemeChanged: (String) -> Unit = {},
    showBKashCard: Boolean = true,
    onBKashCardToggle: (Boolean) -> Unit = {},
    onLanguageChanged: () -> Unit,
    onOnboardingFinished: () -> Unit = {}
) {
    val spendingMessage by viewModel.showSpendingDialog.collectAsState()
    val isBengali = Locale.getDefault().language == "bn"

    if (spendingMessage != null) {
        var dontShowAgain by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { viewModel.dismissSpendingDialog() },
            title = { Text(if (isBengali) "সতর্কতা!" else "Spending Alert!") },
            text = {
                Column {
                    Text(spendingMessage!!)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { dontShowAgain = !dontShowAgain }
                    ) {
                        Checkbox(
                            checked = dontShowAgain,
                            onCheckedChange = { dontShowAgain = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isBengali) "আর দেখাবেন না" else "Don't show again")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissSpendingDialog(dontShowAgain) }) {
                    Text(if (isBengali) "ঠিক আছে" else "OK")
                }
            }
        )
    }

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
            val context = LocalContext.current
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
                },
                onLanguageToggle = {
                    val currentLang = Locale.getDefault().language
                    val newLang = if (currentLang == "bn") "en" else "bn"
                    LocaleHelper.setLocale(context, newLang)
                    onLanguageChanged()
                },
                onBKashClick = {
                    navController.navigate(Screen.BKashSummary.route)
                },
                showBKashCard = showBKashCard
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
                onManageCategoriesClick = {
                    navController.navigate(Screen.ManageCategories.route)
                },
                onTutorialClick = {
                    navController.navigate(Screen.Onboarding.route)
                },
                themeMode = themeMode,
                onThemeChanged = onThemeChanged,
                showBKashCard = showBKashCard,
                onBKashCardToggle = onBKashCardToggle
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

        composable(Screen.ManageCategories.route) {
            ManageCategoriesScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.BKashSummary.route) {
            BKashSummaryScreen(
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
                onEditCategoriesClick = {
                    navController.navigate(Screen.ManageCategories.route)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
