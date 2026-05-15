package com.mihab.expensetracker.util

import java.util.regex.Pattern

data class BKashTransaction(
    val type: String,
    val amount: Double,
    val date: Long,
    val reference: String? = null
)

object BKashSmsParser {
    // Regex for different types of bKash messages
    // Payment: Payment Tk 10.00 to ...
    // Cash Out: Cash Out Tk 1,000.00 to ...
    // Cash In: Cash In Tk 500.00 from ...
    // Mobile Recharge: Mobile Recharge Tk 20.00 to ...
    // Send Money: Send Money Tk 100.00 to ...

    private val paymentRegex = Pattern.compile("Payment Tk ([\\d,.]+)")
    private val cashOutRegex = Pattern.compile("Cash Out Tk ([\\d,.]+)")
    private val cashInRegex = Pattern.compile("Cash In Tk ([\\d,.]+)")
    private val receivedRegex = Pattern.compile("You have received Tk ([\\d,.]+)")
    private val cashbackRegex = Pattern.compile("You have received Cashback Tk ([\\d,.]+)")
    private val rechargeRegex = Pattern.compile("Mobile Recharge Tk ([\\d,.]+)")
    private val sendMoneyRegex = Pattern.compile("Send Money Tk ([\\d,.]+)")
    private val billRegex = Pattern.compile("Amount: Tk ([\\d,.]+)")

    fun parse(body: String, date: Long): BKashTransaction? {
        return when {
            body.contains("Bill successfully paid", ignoreCase = true) -> {
                extractAmount(billRegex, body)?.let { BKashTransaction("Bill Payment", it, date) }
            }
            body.contains("Payment", ignoreCase = true) -> {
                extractAmount(paymentRegex, body)?.let { BKashTransaction("Payment", it, date) }
            }
            body.contains("Cash Out", ignoreCase = true) -> {
                extractAmount(cashOutRegex, body)?.let { BKashTransaction("Cash Out", it, date) }
            }
            body.contains("Cash In", ignoreCase = true) -> {
                extractAmount(cashInRegex, body)?.let { BKashTransaction("Cash In", it, date) }
            }
            body.contains("received Cashback", ignoreCase = true) -> {
                extractAmount(cashbackRegex, body)?.let { BKashTransaction("Cashback", it, date) }
            }
            body.contains("received", ignoreCase = true) -> {
                extractAmount(receivedRegex, body)?.let { BKashTransaction("Cash In", it, date) }
            }
            body.contains("Mobile Recharge", ignoreCase = true) -> {
                extractAmount(rechargeRegex, body)?.let { BKashTransaction("Mobile Recharge", it, date) }
            }
            body.contains("Send Money", ignoreCase = true) -> {
                extractAmount(sendMoneyRegex, body)?.let { BKashTransaction("Send Money", it, date) }
            }
            else -> null
        }
    }

    private fun extractAmount(pattern: Pattern, body: String): Double? {
        val matcher = pattern.matcher(body)
        if (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(",", "")
            return amountStr?.toDoubleOrNull()
        }
        return null
    }
}
