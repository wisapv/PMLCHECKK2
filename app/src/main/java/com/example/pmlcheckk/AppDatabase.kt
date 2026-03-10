package com.example.pmlcheckk

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [InventoryItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao

    // ในไฟล์ AppDatabase.kt
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pml_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

