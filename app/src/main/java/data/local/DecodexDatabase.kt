package com.toi.decodex.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PuzzleEntity::class, ClueEntity::class], version = 1, exportSchema = false)
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
                    .createFromAsset("database/initial_puzzles.db")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}