package com.toi.decodex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toi.decodex.data.local.DecodexDatabase
import com.toi.decodex.ui.components.CrosswordGrid
import com.toi.decodex.ui.components.CrosswordKeyboard
import com.toi.decodex.ui.components.PuzzleListScreen
import com.toi.decodex.ui.viewmodel.DecodexViewModel
import com.toi.decodex.ui.viewmodel.ScreenState
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = DecodexDatabase.getDatabase(applicationContext)
        val viewModel = DecodexViewModel(db.puzzleDao())

        setContent {
            MaterialTheme {
                val screenState by viewModel.screenState.collectAsState()
                val puzzleList by viewModel.puzzleList.collectAsState()
                val uiState by viewModel.uiState.collectAsState()

                when (val state = screenState) {
                    is ScreenState.PuzzleList -> {
                        PuzzleListScreen(
                            puzzles = puzzleList,
                            onPuzzleClick = { id -> viewModel.selectPuzzle(id) }
                        )
                    }
                    is ScreenState.Game -> {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                Row(
                                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { viewModel.backToList() }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Back"
                                            )
                                        }
                                        Column {
                                            Text(text = uiState.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                            Text(text = "Theme: ${uiState.theme}", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }
                                    // Timer Display
                                    val mins = uiState.secondsElapsed / 60
                                    val secs = uiState.secondsElapsed % 60
                                    Text(
                                        text = String.format(Locale.getDefault(), "%02d:%02d", mins, secs),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF311B92)
                                    )
                                }
                            },
                            bottomBar = {
                                CrosswordKeyboard(
                                    onKey = { char -> viewModel.onKeyEntered(char) },
                                    onBackspace = { viewModel.onBackspace() }
                                )
                            }
                        ) { padding ->
                            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                    CrosswordGrid(
                                        grid = uiState.grid,
                                        rows = uiState.rowCount,
                                        cols = uiState.colCount,
                                        onCellClick = { r, c -> viewModel.onCellClicked(r, c) }
                                    )

                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp).background(Color(0xFFEDE7F6)).padding(12.dp)) {
                                        val clue = uiState.activeClue
                                        val text = if (clue != null) "${clue.clueNumber} ${clue.direction}: ${clue.clueText}" else "Select a cell"
                                        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF311B92))
                                    }
                                }

                                // Victory Dialog Popup
                                if (uiState.isWon) {
                                    AlertDialog(
                                        onDismissRequest = {},
                                        title = { Text(text = "🎉 Puzzle Completed!") },
                                        text = {
                                            val mins = uiState.secondsElapsed / 60
                                            val secs = uiState.secondsElapsed % 60
                                            Text(text = "Amazing job! You solved this crossword in $mins minutes and $secs seconds.")
                                        },
                                        confirmButton = {
                                            Button(onClick = { viewModel.backToList() }) {
                                                Text("Back to Puzzles")
                                            }
                                        },
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
