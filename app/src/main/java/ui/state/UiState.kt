package com.toi.decodex.ui.state

import androidx.compose.runtime.Immutable

enum class Direction { ACROSS, DOWN }

@Immutable
data class CellUiState(
    val letter: String = "",
    val isBlackBlock: Boolean = false,
    val isFocused: Boolean = false,
    val isHighlighted: Boolean = false,
    val clueNumber: Int? = null
)

@Immutable
data class GameUiState(
    val title: String = "",
    val theme: String = "",
    val rowCount: Int = 5,
    val colCount: Int = 5,
    val grid: Map<Pair<Int, Int>, CellUiState> = emptyMap(),
    val selectedRow: Int = 0,
    val selectedCol: Int = 0,
    val currentDirection: Direction = Direction.ACROSS,
    val activeClue: com.toi.decodex.data.local.ClueEntity? = null,
    val secondsElapsed: Int = 0,
    val isWon: Boolean = false
)
