package com.mihab.expensetracker.util

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast
import com.mihab.expensetracker.widget.ExpenseWidgetReceiver

object WidgetHelper {
    fun requestPinWidget(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                val myWidgetReceiver = ComponentName(context, ExpenseWidgetReceiver::class.java)
                appWidgetManager.requestPinAppWidget(myWidgetReceiver, null, null)
                Toast.makeText(context, "Requesting widget pin...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Pinning widgets is not supported on this device/launcher", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Widget pinning requires Android 8.0+", Toast.LENGTH_SHORT).show()
        }
    }
}
