package com.mihab.expensetracker.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    private const val PREFS_NAME = "settings"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_CURRENCY = "currency"

    fun setLocale(context: Context, language: String): Context {
        persist(context, KEY_LANGUAGE, language)
        return updateResources(context, language)
    }

    fun onAttach(context: Context): Context {
        val lang = getPersistedData(context, KEY_LANGUAGE, Locale.getDefault().language)
        return updateResources(context, lang)
    }

    fun getLanguage(context: Context): String {
        return getPersistedData(context, KEY_LANGUAGE, Locale.getDefault().language)
    }

    fun setCurrency(context: Context, currency: String) {
        persist(context, KEY_CURRENCY, currency)
    }

    fun getCurrency(context: Context): String {
        return getPersistedData(context, KEY_CURRENCY, "৳")
    }

    private fun persist(context: Context, key: String, value: String) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    private fun getPersistedData(context: Context, key: String, defaultValue: String): String {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return preferences.getString(key, defaultValue) ?: defaultValue
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }
}
