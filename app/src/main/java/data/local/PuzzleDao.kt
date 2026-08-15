package com.toi.decodex.data.local

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PuzzleDao {
    @Query("SELECT * FROM puzzles")
    fun getAllPuzzles(): Flow<List<PuzzleEntity>>

    @Query("SELECT * FROM clues WHERE puzzleId = :puzzleId")
    fun getCluesForPuzzle(puzzleId: String): Flow<List<ClueEntity>>
}