package com.mihab.expensetracker.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.mihab.expensetracker.data.local.ExpenseEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {
    fun exportExpensesToCsv(context: Context, expenses: List<ExpenseEntity>) {
        val fileName = "expenses_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
        val file = File(context.cacheDir, fileName)
        
        file.bufferedWriter().use { writer ->
            writer.write("ID,Amount,Category,Note,Date\n")
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            expenses.forEach { expense ->
                val dateStr = dateFormat.format(Date(expense.date))
                writer.write("${expense.id},${expense.amount},${expense.category},\"${expense.note ?: ""}\",${dateStr}\n")
            }
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Expense Tracker Export")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Export CSV"))
    }
}
