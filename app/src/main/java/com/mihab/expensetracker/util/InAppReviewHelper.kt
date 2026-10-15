package com.mihab.expensetracker.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.google.android.play.core.review.ReviewManagerFactory

object InAppReviewHelper {
    private const val PREF_NAME = "expense_tracker_review_prefs"
    private const val KEY_EXPENSE_COUNT = "expense_add_count"
    private const val KEY_LAST_REVIEW_TIME = "last_review_time"
    private const val KEY_HAS_RATED = "has_rated"

    private const val MIN_EXPENSES_FOR_PROMPT = 5
    private const val COOLDOWN_DAYS = 60L

    fun checkAndRequestReview(context: Context) {
        val activity = context.findActivity() ?: return
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        if (prefs.getBoolean(KEY_HAS_RATED, false)) return

        val count = prefs.getInt(KEY_EXPENSE_COUNT, 0) + 1
        prefs.edit().putInt(KEY_EXPENSE_COUNT, count).apply()

        if (count >= MIN_EXPENSES_FOR_PROMPT) {
            val lastTime = prefs.getLong(KEY_LAST_REVIEW_TIME, 0L)
            val currentTime = System.currentTimeMillis()
            val daysElapsed = (currentTime - lastTime) / (1000 * 60 * 60 * 24)

            if (lastTime == 0L || daysElapsed >= COOLDOWN_DAYS) {
                launchReviewFlowInternal(activity, prefs, isManual = false)
            }
        }
    }

    fun requestReviewManually(context: Context) {
        val activity = context.findActivity()
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (activity != null) {
            launchReviewFlowInternal(activity, prefs, isManual = true)
        } else {
            openPlayStoreFallback(context)
        }
    }

    private fun launchReviewFlowInternal(
        activity: Activity,
        prefs: android.content.SharedPreferences,
        isManual: Boolean
    ) {
        try {
            val manager = ReviewManagerFactory.create(activity)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    val flow = manager.launchReviewFlow(activity, reviewInfo)
                    flow.addOnCompleteListener { _ ->
                        prefs.edit()
                            .putLong(KEY_LAST_REVIEW_TIME, System.currentTimeMillis())
                            .apply()
                    }
                } else {
                    if (isManual) {
                        openPlayStoreFallback(activity)
                    }
                }
            }
        } catch (e: Exception) {
            if (isManual) {
                openPlayStoreFallback(activity)
            }
        }
    }

    private fun openPlayStoreFallback(context: Context) {
        val packageName = context.packageName
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(
                    context,
                    "Play Store is not available (App is not published yet)",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun Context.findActivity(): Activity? {
        var cur = this
        while (cur is ContextWrapper) {
            if (cur is Activity) return cur
            cur = cur.baseContext
        }
        return null
    }
}
