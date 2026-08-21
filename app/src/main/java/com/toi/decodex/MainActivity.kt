package com.toi.decodex

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
                    val puzzles by viewModel.puzzleList.collectAsStateWithLifecycle()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    when (screenState) {
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
}
