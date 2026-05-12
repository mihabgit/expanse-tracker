package com.mihab.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mihab.expensetracker.data.repository.ExpenseRepository
import com.mihab.expensetracker.util.NotificationHelper

class ExpenseViewModelFactory(
    private val repository: ExpenseRepository,
    private val notificationHelper: NotificationHelper
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExpenseViewModel(repository, notificationHelper) as T
    }
}
