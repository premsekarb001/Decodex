package com.toi.decodex.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.toi.decodex.data.local.ClueEntity
import com.toi.decodex.data.local.PuzzleDao
import com.toi.decodex.data.local.PuzzleEntity
import com.toi.decodex.ui.state.CellUiState
import com.toi.decodex.ui.state.Direction
import com.toi.decodex.ui.state.GameUiState
import kotlinx.coroutines.Dispatchers
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
    private var currentPuzzleId: String? = null
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
            currentPuzzleId = puzzleId

            initializeGrid(
                puzzle.rowCount, 
                puzzle.colCount, 
                puzzle.gridLayout, 
                puzzle.title, 
                puzzle.theme, 
                clues,
                puzzle.userProgress
            )
            _screenState.value = ScreenState.Game(puzzleId)
            startTimer()
        }
    }

    fun backToList() {
        timerJob?.cancel()
        _screenState.value = ScreenState.PuzzleList
        currentPuzzleId = null
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
        rows: Int, cols: Int, layout: String, title: String, theme: String, 
        clues: List<ClueEntity>, savedProgress: String?
    ) {
        val newGrid = mutableMapOf<Pair<Int, Int>, CellUiState>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val idx = r * cols + c
                val isBlock = idx < layout.length && layout[idx] == '#'
                val clue = clues.find { it.startX == r && it.startY == c }
                
                // Restore letter from savedProgress if available
                val letter = if (!isBlock && savedProgress != null && idx < savedProgress.length) {
                    val savedChar = savedProgress[idx]
                    if (savedChar != ' ' && savedChar != '#') savedChar.toString() else ""
                } else ""

                newGrid[Pair(r, c)] = CellUiState(
                    letter = letter,
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
        
        saveProgress(updatedGrid)
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
        saveProgress(updatedGrid)
        updateHighlights()
    }

    private fun saveProgress(grid: Map<Pair<Int, Int>, CellUiState>) {
        val puzzleId = currentPuzzleId ?: return
        val rows = _uiState.value.rowCount
        val cols = _uiState.value.colCount
        
        val progressString = StringBuilder()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cell = grid[Pair(r, c)]
                progressString.append(
                    if (cell?.isBlackBlock == true) "#" 
                    else if (cell?.letter.isNullOrEmpty()) " " 
                    else cell!!.letter
                )
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            dao.updatePuzzleProgress(puzzleId, progressString.toString())
        }
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
        do {
            if (dir == Direction.ACROSS) {
                nc++
                if (nc >= cols) { nc = 0; nr++ }
            } else {
                nr++
                if (nr >= rows) { nr = 0; nc++ }
            }
            if (nr >= rows || nc >= cols) return Pair(r, c) // Hit the end
            
            val cell = grid[Pair(nr, nc)]
            if (cell != null && !cell.isBlackBlock) return Pair(nr, nc)
        } while (nr < rows && nc < cols)
        
        return Pair(r, c)
    }

    private fun getPrevCell(r: Int, c: Int, dir: Direction, grid: Map<Pair<Int, Int>, CellUiState>): Pair<Int, Int> {
        var pr = r; var pc = c
        val rows = _uiState.value.rowCount
        val cols = _uiState.value.colCount
        do {
            if (dir == Direction.ACROSS) {
                pc--
                if (pc < 0) { pc = cols - 1; pr-- }
            } else {
                pr--
                if (pr < 0) { pr = rows - 1; pc-- }
            }
            if (pr < 0 || pc < 0) return Pair(r, c)
            val cell = grid[Pair(pr, pc)]
            if (cell != null && !cell.isBlackBlock) return Pair(pr, pc)
        } while (pr >= 0 && pc >= 0)
        
        return Pair(r, c)
    }

    class Factory(private val dao: PuzzleDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DecodexViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DecodexViewModel(dao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
