package com.mihab.expensetracker.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.mihab.expensetracker.MainActivity
import com.mihab.expensetracker.data.local.ExpenseDatabase
import com.mihab.expensetracker.util.LocaleHelper
import java.util.Calendar
import java.util.Locale

class ExpenseWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dao = ExpenseDatabase.getInstance(context).expenseDao()
        val currency = LocaleHelper.getCurrency(context)
        val isBengali = Locale.getDefault().language == "bn"

        val now = Calendar.getInstance()
        val startOfToday = getStartOfDay(now)
        val endOfToday = getEndOfDay(now)

        val startOfMonth = getStartOfMonth(now)
        val endOfMonth = getEndOfMonth(now)

        val todayTotal = dao.getTotalInTimeRange(startOfToday, endOfToday) ?: 0.0
        val monthTotal = dao.getTotalInTimeRange(startOfMonth, endOfMonth) ?: 0.0

        provideContent {
            GlanceTheme {
                ExpenseWidgetContent(
                    todayTotal = todayTotal,
                    monthTotal = monthTotal,
                    currency = currency,
                    isBengali = isBengali,
                    context = context
                )
            }
        }
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

    private fun getStartOfMonth(calendar: Calendar): Long {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getEndOfMonth(calendar: Calendar): Long {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }
}

@Composable
fun ExpenseWidgetContent(
    todayTotal: Double,
    monthTotal: Double,
    currency: String,
    isBengali: Boolean,
    context: Context
) {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF1F1B24), Color(0xFF1F1B24)))
            .padding(16.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = if (isBengali) "💰 খরচ খাতা" else "💰 Expense Tracker",
                style = TextStyle(
                    color = ColorProvider(Color.White, Color.White),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(12.dp))

        // Totals Row
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            // Today Column
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Text(
                    text = if (isBengali) "আজকে" else "Today",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFB0BEC5), Color(0xFFB0BEC5)),
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = "$currency${formatAmount(todayTotal)}",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF4CAF50), Color(0xFF4CAF50)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Month Column
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Text(
                    text = if (isBengali) "এই মাস" else "This Month",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFB0BEC5), Color(0xFFB0BEC5)),
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = "$currency${formatAmount(monthTotal)}",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFFF9800), Color(0xFFFF9800)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(12.dp))

        // Action Button to Open App
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(ColorProvider(Color(0xFF6750A4), Color(0xFF6750A4)))
                .clickable(actionStartActivity(intent))
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(
                text = if (isBengali) "+ খরচ যোগ করুন" else "+ Add Expense",
                style = TextStyle(
                    color = ColorProvider(Color.White, Color.White),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

private fun formatAmount(amount: Double): String {
    return if (amount % 1.0 == 0.0) {
        amount.toInt().toString()
    } else {
        String.format(Locale.US, "%.2f", amount)
    }
}
