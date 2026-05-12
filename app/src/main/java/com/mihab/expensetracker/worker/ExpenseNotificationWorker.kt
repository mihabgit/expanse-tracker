package com.mihab.expensetracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mihab.expensetracker.data.local.ExpenseDatabase
import com.mihab.expensetracker.util.NotificationHelper
import java.util.Calendar
import java.util.Locale

class ExpenseNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val db = ExpenseDatabase.getInstance(applicationContext)
        val dao = db.expenseDao()
        val notificationHelper = NotificationHelper(applicationContext)
        val isBengali = Locale.getDefault().language == "bn"

        val now = Calendar.getInstance()
        
        // Check if user added expense today
        val startOfToday = getStartOfDay(now)
        val endOfToday = getEndOfDay(now)
        val todayCount = dao.getExpenseCountInTimeRange(startOfToday, endOfToday)

        if (todayCount == 0) {
            val title = if (isBengali) "খরচ যোগ করতে ভুলে গেছেন?" else "Forgot to add expenses?"
            val message = if (isBengali) "আজ আপনি কোন খরচ যোগ করেননি। এখনই যোগ করুন!" else "You haven't added any expenses today. Add one now!"
            notificationHelper.showNotification(title, message, 1)
            return Result.success()
        }

        // Check if spend today > spend yesterday
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val startOfYesterday = getStartOfDay(yesterday)
        val endOfYesterday = getEndOfDay(yesterday)
        
        val todayTotal = dao.getTotalInTimeRange(startOfToday, endOfToday) ?: 0.0
        val yesterdayTotal = dao.getTotalInTimeRange(startOfYesterday, endOfYesterday) ?: 0.0

        if (todayTotal > yesterdayTotal && yesterdayTotal > 0) {
            val title = if (isBengali) "সতর্কতা!" else "Spending Alert!"
            val message = if (isBengali) 
                "গতকালকের চেয়ে আজ আপনি বেশি খরচ করেছেন!" 
            else 
                "You have spent more today than yesterday!"
            notificationHelper.showNotification(title, message, 2)
        }

        return Result.success()
    }

    private fun getStartOfDay(calendar: Calendar): Long {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getEndOfDay(calendar: Calendar): Long {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }
}
