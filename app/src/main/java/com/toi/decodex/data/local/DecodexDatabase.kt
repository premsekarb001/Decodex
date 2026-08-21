package com.toi.decodex.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [PuzzleEntity::class, ClueEntity::class], version = 3, exportSchema = false)
abstract class DecodexDatabase : RoomDatabase() {
    abstract fun puzzleDao(): PuzzleDao

    companion object {
        @Volatile
        private var INSTANCE: DecodexDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add the userProgress column to the puzzles table if it doesn't exist
                // Note: Room might have already created the table with version 2 if starting fresh.
                // But for migration from v1, we need this.
                db.execSQL("ALTER TABLE puzzles ADD COLUMN userProgress TEXT")
            }
        }

        private val CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed initial data
                db.execSQL(
                    """
                    INSERT INTO puzzles (puzzleId, publishDate, difficulty, rowCount, colCount, title, theme, gridLayout, userProgress)
                    VALUES ('welcome_1', '2026-08-22', 'Easy', 3, 3, 'Welcome to Decodex', 'Basics', '.........', NULL)
                    """.trimIndent()
                )
                
                // Across Clues
                db.execSQL("INSERT INTO clues (clueId, puzzleId, clueNumber, direction, clueText, answer, startX, startY, length) VALUES ('c1_a', 'welcome_1', 1, 'ACROSS', 'Feline friend', 'CAT', 0, 0, 3)")
                db.execSQL("INSERT INTO clues (clueId, puzzleId, clueNumber, direction, clueText, answer, startX, startY, length) VALUES ('c4_a', 'welcome_1', 4, 'ACROSS', 'Length of time', 'AGE', 1, 0, 3)")
                db.execSQL("INSERT INTO clues (clueId, puzzleId, clueNumber, direction, clueText, answer, startX, startY, length) VALUES ('c7_a', 'welcome_1', 7, 'ACROSS', 'Number after nine', 'TEN', 2, 0, 3)")
                
                // Down Clues
                db.execSQL("INSERT INTO clues (clueId, puzzleId, clueNumber, direction, clueText, answer, startX, startY, length) VALUES ('c1_d', 'welcome_1', 1, 'DOWN', 'Feline friend', 'CAT', 0, 0, 3)")
                db.execSQL("INSERT INTO clues (clueId, puzzleId, clueNumber, direction, clueText, answer, startX, startY, length) VALUES ('c2_d', 'welcome_1', 2, 'DOWN', 'Length of time', 'AGE', 0, 1, 3)")
                db.execSQL("INSERT INTO clues (clueId, puzzleId, clueNumber, direction, clueText, answer, startX, startY, length) VALUES ('c3_d', 'welcome_1', 3, 'DOWN', 'Number after nine', 'TEN', 0, 2, 3)")
            }
        }

        fun getDatabase(context: Context): DecodexDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DecodexDatabase::class.java,
                    "decodex.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(CALLBACK)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
