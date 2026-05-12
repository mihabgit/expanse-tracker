package com.mihab.expensetracker

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mihab.expensetracker.data.local.ExpenseDatabase
import com.mihab.expensetracker.data.repository.ExpenseRepository
import com.mihab.expensetracker.navigation.AppNavHost
import com.mihab.expensetracker.ui.screens.ExpenseListScreen
import com.mihab.expensetracker.ui.screens.Screen
import com.mihab.expensetracker.ui.theme.ExpenseTrackerTheme
import com.mihab.expensetracker.util.LocaleHelper
import com.mihab.expensetracker.util.NotificationHelper
import com.mihab.expensetracker.viewmodel.ExpenseViewModel
import com.mihab.expensetracker.viewmodel.ExpenseViewModelFactory
import com.mihab.expensetracker.worker.ExpenseNotificationWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission granted or denied
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        LocaleHelper.onAttach(this)
        
        checkNotificationPermission()
        scheduleNotifications()

        val db = ExpenseDatabase.getInstance(this)
        val repository = ExpenseRepository(db.expenseDao(), db.categoryDao())
        val notificationHelper = NotificationHelper(this)
        val factory = ExpenseViewModelFactory(repository, notificationHelper)

        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isFirstRun = sharedPref.getBoolean("is_first_run", true)

        setContent {
            val navController = rememberNavController()
            val viewModel: ExpenseViewModel = viewModel(factory = factory)
            
            LaunchedEffect(Unit) {
                viewModel.setSpendingAlertEnabled(sharedPref.getBoolean("spending_alert_enabled", true)) {
                    sharedPref.edit().putBoolean("spending_alert_enabled", false).apply()
                }
            }

            var languageChanged by remember { mutableStateOf(0) }
            
            var themeMode by remember { 
                mutableStateOf(sharedPref.getString("theme_mode", "system") ?: "system") 
            }
            
            val isDarkMode = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            ExpenseTrackerTheme(darkTheme = isDarkMode) {
                key(languageChanged) {
                    AppNavHost(
                        navController = navController,
                        viewModel = viewModel,
                        startDestination = if (isFirstRun) Screen.Onboarding.route else Screen.Home.route,
                        themeMode = themeMode,
                        onThemeChanged = { mode ->
                            themeMode = mode
                            sharedPref.edit().putString("theme_mode", mode).apply()
                        },
                        onLanguageChanged = {
                            languageChanged++
                        },
                        onOnboardingFinished = {
                            if (isFirstRun) {
                                sharedPref.edit().putBoolean("is_first_run", false).apply()
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                                }
                            } else {
                                navController.popBackStack()
                            }
                        }
                    )
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun scheduleNotifications() {
        val workRequest = PeriodicWorkRequestBuilder<ExpenseNotificationWorker>(
            1, TimeUnit.DAYS
        ).setInitialDelay(8, TimeUnit.HOURS) // Run roughly in the evening if started in morning
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "expense_notification_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

