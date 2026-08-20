package com.toi.decodex.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [PuzzleEntity::class, ClueEntity::class], version = 4, exportSchema = false)
abstract class DecodexDatabase : RoomDatabase() {
    abstract fun puzzleDao(): PuzzleDao

    companion object {
        @Volatile
        private var INSTANCE: DecodexDatabase? = null

        fun getDatabase(context: Context): DecodexDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DecodexDatabase::class.java,
                    "decodex.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed data safely via DAO after creation to prevent table locks
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getDatabase(context).puzzleDao()
                                // If needed, initialization can happen cleanly via DAO queries or let it start clean
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}