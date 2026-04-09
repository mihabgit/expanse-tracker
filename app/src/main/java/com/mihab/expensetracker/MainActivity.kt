package com.mihab.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.mihab.expensetracker.data.local.ExpenseDatabase
import com.mihab.expensetracker.data.repository.ExpenseRepository
import com.mihab.expensetracker.navigation.AppNavHost
import com.mihab.expensetracker.ui.screens.ExpenseListScreen
import com.mihab.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mihab.expensetracker.util.LocaleHelper
import com.mihab.expensetracker.viewmodel.ExpenseViewModel
import com.mihab.expensetracker.viewmodel.ExpenseViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        LocaleHelper.onAttach(this)

        val db = ExpenseDatabase.getInstance(this)
        val repository = ExpenseRepository(db.expenseDao())
        val factory = ExpenseViewModelFactory(repository)

        setContent {
            val navController = rememberNavController()
            val viewModel: ExpenseViewModel = viewModel(factory = factory)
            var languageChanged by remember { mutableStateOf(0) }

            ExpenseTrackerTheme {
                key(languageChanged) {
                    AppNavHost(
                        navController = navController,
                        viewModel = viewModel,
                        onLanguageChanged = {
                            languageChanged++
                        }
                    )
                }
            }
        }

    }
}

