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
                db.execSQL("ALTER TABLE puzzles ADD COLUMN userProgress TEXT")
            }
        }

        private val CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                seedPuzzles(db)
            }

            private fun seedPuzzles(db: SupportSQLiteDatabase) {
                // Puzzle 1: Technology
                db.execSQL("""
                    INSERT INTO puzzles (puzzleId, publishDate, difficulty, rowCount, colCount, title, theme, gridLayout, userProgress)
                    VALUES ('tech_1', '2026-08-22', 'Easy', 5, 5, 'Digital Mini', 'Technology', 'APP##NET##WEB##############', NULL)
                """.trimIndent())
                db.execSQL("INSERT INTO clues VALUES ('t1_a1', 'tech_1', 1, 'ACROSS', 'Smartphone software', 'APP', 0, 0, 3)")
                db.execSQL("INSERT INTO clues VALUES ('t1_a2', 'tech_1', 2, 'ACROSS', 'The internet, for short', 'NET', 1, 0, 3)")
                db.execSQL("INSERT INTO clues VALUES ('t1_a3', 'tech_1', 3, 'ACROSS', 'World Wide ___', 'WEB', 2, 0, 3)")
                db.execSQL("INSERT INTO clues VALUES ('t1_d1', 'tech_1', 1, 'DOWN', 'Software for a phone', 'ANW', 0, 0, 3)") // Intersect check: A, N, W

                // Puzzle 2: Nature
                db.execSQL("""
                    INSERT INTO puzzles (puzzleId, publishDate, difficulty, rowCount, colCount, title, theme, gridLayout, userProgress)
                    VALUES ('nat_1', '2026-08-22', 'Easy', 5, 5, 'Green Mini', 'Nature', 'SKY##SUN##AIR##############', NULL)
                """.trimIndent())
                db.execSQL("INSERT INTO clues VALUES ('n1_a1', 'nat_1', 1, 'ACROSS', 'Blue expanse above', 'SKY', 0, 0, 3)")
                db.execSQL("INSERT INTO clues VALUES ('n1_a2', 'nat_1', 2, 'ACROSS', 'Our local star', 'SUN', 1, 0, 3)")
                db.execSQL("INSERT INTO clues VALUES ('n1_a3', 'nat_1', 3, 'ACROSS', 'What we breathe', 'AIR', 2, 0, 3)")

                // Puzzle 3: Geography
                db.execSQL("""
                    INSERT INTO puzzles (puzzleId, publishDate, difficulty, rowCount, colCount, title, theme, gridLayout, userProgress)
                    VALUES ('geo_1', '2026-08-22', 'Easy', 5, 5, 'World Mini', 'Geography', 'MAP##SEA##BAY##############', NULL)
                """.trimIndent())
                db.execSQL("INSERT INTO clues VALUES ('g1_a1', 'geo_1', 1, 'ACROSS', 'Atlas page', 'MAP', 0, 0, 3)")
                db.execSQL("INSERT INTO clues VALUES ('g1_a2', 'geo_1', 2, 'ACROSS', 'Large body of salt water', 'SEA', 1, 0, 3)")
                db.execSQL("INSERT INTO clues VALUES ('g1_a3', 'geo_1', 3, 'ACROSS', 'San Francisco __', 'BAY', 2, 0, 3)")

                // Puzzle 4: Cinema
                db.execSQL("""
                    INSERT INTO puzzles (puzzleId, publishDate, difficulty, rowCount, colCount, title, theme, gridLayout, userProgress)
                    VALUES ('mov_1', '2026-08-22', 'Easy', 5, 5, 'Movie Mini', 'Cinema', 'ACT##SET##FAN##############', NULL)
                """.trimIndent())
                db.execSQL("INSERT INTO clues VALUES ('m1_a1', 'mov_1', 1, 'ACROSS', 'Perform in a movie', 'ACT', 0, 0, 3)")
                db.execSQL("INSERT INTO clues VALUES ('m1_a2', 'mov_1', 2, 'ACROSS', 'Where filming happens', 'SET', 1, 0, 3)")
                db.execSQL("INSERT INTO clues VALUES ('m1_a3', 'mov_1', 3, 'ACROSS', 'Movie enthusiast', 'FAN', 2, 0, 3)")
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
