package com.toi.decodex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.toi.decodex.data.local.DecodexDatabase
import com.toi.decodex.ui.components.GameScreen
import com.toi.decodex.ui.components.PuzzleListScreen
import com.toi.decodex.ui.theme.DecodexTheme
import com.toi.decodex.ui.viewmodel.DecodexViewModel
import com.toi.decodex.ui.viewmodel.ScreenState

class MainActivity : ComponentActivity() {

    private val viewModel: DecodexViewModel by viewModels {
        DecodexViewModel.Factory(DecodexDatabase.getDatabase(applicationContext).puzzleDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            DecodexTheme {
                val screenState by viewModel.screenState.collectAsState()
                val puzzles by viewModel.puzzleList.collectAsState()
                val uiState by viewModel.uiState.collectAsState()

                when (val state = screenState) {
                    is ScreenState.PuzzleList -> {
                        PuzzleListScreen(
                            puzzles = puzzles,
                            onPuzzleClick = { viewModel.selectPuzzle(it) }
                        )
                    }
                    is ScreenState.Game -> {
                        GameScreen(
                            uiState = uiState,
                            onCellClick = { r, c -> viewModel.onCellClicked(r, c) },
                            onKey = { viewModel.onKeyEntered(it) },
                            onBackspace = { viewModel.onBackspace() },
                            onBack = { viewModel.backToList() }
                        )
                    }
                }
            }
        }
    }
}
