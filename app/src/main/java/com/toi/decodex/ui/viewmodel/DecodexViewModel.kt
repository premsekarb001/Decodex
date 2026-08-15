package com.toi.decodex.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toi.decodex.data.local.ClueEntity
import com.toi.decodex.data.local.PuzzleDao
import com.toi.decodex.data.local.PuzzleEntity
import com.toi.decodex.ui.state.CellUiState
import com.toi.decodex.ui.state.Direction
import com.toi.decodex.ui.state.GameUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ScreenState {
    object PuzzleList : ScreenState
    data class Game(val puzzleId: String) : ScreenState
}

class DecodexViewModel(private val dao: PuzzleDao) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.PuzzleList)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    private val _puzzleList = MutableStateFlow<List<PuzzleEntity>>(emptyList())
    val puzzleList: StateFlow<List<PuzzleEntity>> = _puzzleList.asStateFlow()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var currentClues: List<ClueEntity> = emptyList()
    private var timerJob: Job? = null

    init {
        fetchPuzzles()
    }

    private fun fetchPuzzles() {
        viewModelScope.launch {
            dao.getAllPuzzles().collect { puzzles ->
                _puzzleList.value = puzzles
            }
        }
    }

    fun selectPuzzle(puzzleId: String) {
        viewModelScope.launch {
            val puzzles = dao.getAllPuzzles().first()
            val puzzle = puzzles.find { it.puzzleId == puzzleId } ?: return@launch
            val clues = dao.getCluesForPuzzle(puzzleId).first()
            currentClues = clues

            initializeGrid(puzzle.rowCount, puzzle.colCount, puzzle.gridLayout, puzzle.title, puzzle.theme, clues)
            _screenState.value = ScreenState.Game(puzzleId)
            startTimer()
        }
    }

    fun backToList() {
        timerJob?.cancel()
        _screenState.value = ScreenState.PuzzleList
    }

    private fun startTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(secondsElapsed = 0, isWon = false) }
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _uiState.update { it.copy(secondsElapsed = it.secondsElapsed + 1) }
            }
        }
    }

    private fun initializeGrid(
        rows: Int, cols: Int, layout: String, title: String, theme: String, clues: List<ClueEntity>
    ) {
        val newGrid = mutableMapOf<Pair<Int, Int>, CellUiState>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val idx = r * cols + c
                val isBlock = idx < layout.length && layout[idx] == '#'
                val clue = clues.find { it.startX == r && it.startY == c }
                newGrid[Pair(r, c)] = CellUiState(
                    isBlackBlock = isBlock,
                    clueNumber = clue?.clueNumber
                )
            }
        }

        val firstPlayable = newGrid.entries.firstOrNull { !it.value.isBlackBlock }?.key ?: Pair(0, 0)

        _uiState.update {
            it.copy(
                title = title, theme = theme, rowCount = rows, colCount = cols,
                grid = newGrid, selectedRow = firstPlayable.first, selectedCol = firstPlayable.second
            )
        }
        updateHighlights()
    }

    fun onCellClicked(row: Int, col: Int) {
        val current = _uiState.value
        if (current.isWon) return
        val cell = current.grid[Pair(row, col)] ?: return
        if (cell.isBlackBlock) return

        if (current.selectedRow == row && current.selectedCol == col) {
            val newDir = if (current.currentDirection == Direction.ACROSS) Direction.DOWN else Direction.ACROSS
            _uiState.update { it.copy(currentDirection = newDir) }
        } else {
            _uiState.update { it.copy(selectedRow = row, selectedCol = col) }
        }
        updateHighlights()
    }

    fun onKeyEntered(char: String) {
        val current = _uiState.value
        if (current.isWon) return
        val r = current.selectedRow
        val c = current.selectedCol
        val cell = current.grid[Pair(r, c)] ?: return
        if (cell.isBlackBlock) return

        val updatedGrid = current.grid.toMutableMap()
        updatedGrid[Pair(r, c)] = cell.copy(letter = char.uppercase())

        val nextCoords = getNextCell(r, c, current.currentDirection, current.rowCount, current.colCount, updatedGrid)
        _uiState.update {
            it.copy(grid = updatedGrid, selectedRow = nextCoords.first, selectedCol = nextCoords.second)
        }
        updateHighlights()
        checkForWin(updatedGrid)
    }

    fun onBackspace() {
        val current = _uiState.value
        if (current.isWon) return
        val r = current.selectedRow
        val c = current.selectedCol
        val updatedGrid = current.grid.toMutableMap()
        val cell = updatedGrid[Pair(r, c)]

        if (cell != null && cell.letter.isNotEmpty()) {
            updatedGrid[Pair(r, c)] = cell.copy(letter = "")
            _uiState.update { it.copy(grid = updatedGrid) }
        } else {
            val prevCoords = getPrevCell(r, c, current.currentDirection, updatedGrid)
            val prevCell = updatedGrid[prevCoords]
            if (prevCell != null) {
                updatedGrid[prevCoords] = prevCell.copy(letter = "")
                _uiState.update {
                    it.copy(grid = updatedGrid, selectedRow = prevCoords.first, selectedCol = prevCoords.second)
                }
            }
        }
        updateHighlights()
    }

    private fun checkForWin(grid: Map<Pair<Int, Int>, CellUiState>) {
        for (clue in currentClues) {
            var r = clue.startX
            var c = clue.startY
            val expectedWord = clue.answer.uppercase()

            for (char in expectedWord) {
                val cellLetter = grid[Pair(r, c)]?.letter ?: ""
                if (cellLetter != char.toString()) return // Not solved yet

                if (clue.direction.equals("ACROSS", true)) c++ else r++
            }
        }

        // If all clues match!
        timerJob?.cancel()
        _uiState.update { it.copy(isWon = true) }
    }

    private fun updateHighlights() {
        val current = _uiState.value
        val r = current.selectedRow
        val c = current.selectedCol
        val dir = current.currentDirection

        val activeClue = currentClues.find { clue ->
            clue.direction.equals(dir.name, ignoreCase = true) &&
            if (dir == Direction.ACROSS) clue.startX == r && c in clue.startY until (clue.startY + clue.length)
            else clue.startY == c && r in clue.startX until (clue.startX + clue.length)
        }

        val updatedGrid = current.grid.mapValues { (coords, state) ->
            val isFocused = coords.first == r && coords.second == c
            val isHighlighted = if (activeClue != null) {
                if (dir == Direction.ACROSS) coords.first == activeClue.startX && coords.second in activeClue.startY until (activeClue.startY + activeClue.length)
                else coords.second == activeClue.startY && coords.first in activeClue.startX until (activeClue.startX + activeClue.length)
            } else false
            state.copy(isFocused = isFocused, isHighlighted = isHighlighted)
        }
        _uiState.update { it.copy(grid = updatedGrid, activeClue = activeClue) }
    }

    private fun getNextCell(r: Int, c: Int, dir: Direction, rows: Int, cols: Int, grid: Map<Pair<Int, Int>, CellUiState>): Pair<Int, Int> {
        var nr = r; var nc = c
        if (dir == Direction.ACROSS && c + 1 < cols) nc++
        else if (dir == Direction.DOWN && r + 1 < rows) nr++
        return if (grid[Pair(nr, nc)]?.isBlackBlock == false) Pair(nr, nc) else Pair(r, c)
    }

    private fun getPrevCell(r: Int, c: Int, dir: Direction, grid: Map<Pair<Int, Int>, CellUiState>): Pair<Int, Int> {
        var pr = r; var pc = c
        if (dir == Direction.ACROSS && c - 1 >= 0) pc--
        else if (dir == Direction.DOWN && r - 1 >= 0) pr--
        return if (grid[Pair(pr, pc)]?.isBlackBlock == false) Pair(pr, pc) else Pair(r, c)
    }
}
