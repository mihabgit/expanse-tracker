package com.mihab.expensetracker.util

object CategoryUtils {
    fun getCategoryName(category: String, isBengali: Boolean): String {
        return when (category) {
            "Food" -> if (isBengali) "খাবার" else "Food"
            "Transport" -> if (isBengali) "পরিবহন" else "Transport"
            "Shopping" -> if (isBengali) "শপিং" else "Shopping"
            "Entertainment" -> if (isBengali) "বিনোদন" else "Entertainment"
            "Bills" -> if (isBengali) "বিল" else "Bills"
            "Health" -> if (isBengali) "স্বাস্থ্য" else "Health"
            "Education" -> if (isBengali) "শিক্ষা" else "Education"
            "Others" -> if (isBengali) "অন্যান্য" else "Others"
            else -> category
        }
    }

    fun getCategoryEmoji(category: String): String {
        return when (category) {
            "Food" -> "🍔"
            "Transport" -> "🚌"
            "Shopping" -> "🛍️"
            "Entertainment" -> "🎬"
            "Bills" -> "📑"
            "Health" -> "🏥"
            "Education" -> "📚"
            "Others" -> "💰"
            else -> "✨"
        }
    }
}
