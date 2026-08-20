package com.toi.decodex.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "puzzles")
data class PuzzleEntity(
    @PrimaryKey val puzzleId: String,
    val publishDate: String,
    val difficulty: String,
    val rowCount: Int,
    val colCount: Int,
    val title: String,
    val theme: String,
    val gridLayout: String,
    val userProgress: String? = null
)

@Entity(
    tableName = "clues",
    foreignKeys = [
        ForeignKey(
            entity = PuzzleEntity::class,
            parentColumns = ["puzzleId"],
            childColumns = ["puzzleId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ClueEntity(
    @PrimaryKey val clueId: String,
    val puzzleId: String,
    val clueNumber: Int,
    val direction: String, // "ACROSS" or "DOWN"
    val clueText: String,
    val answer: String,
    val startX: Int,
    val startY: Int,
    val length: Int
)
