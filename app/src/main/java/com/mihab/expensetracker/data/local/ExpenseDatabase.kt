package com.mihab.expensetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ExpenseEntity::class, QuickExpenseEntity::class],
    version = 2
)
abstract class ExpenseDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile private var INSTANCE: ExpenseDatabase? = null

        fun getInstance(context: Context): ExpenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val migration_1_2 = object : Migration(1, 2) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        db.execSQL("""
                            CREATE TABLE IF NOT EXISTS `quick_expenses` (
                                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                                `icon` TEXT NOT NULL, 
                                `category` TEXT NOT NULL, 
                                `categoryBn` TEXT NOT NULL, 
                                `amount` REAL NOT NULL, 
                                `color` INTEGER NOT NULL
                            )
                        """.trimIndent())
                    }
                }

                Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase::class.java,
                    "expense_db"
                )
                    .addMigrations(migration_1_2)
                    .fallbackToDestructiveMigration(true)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
