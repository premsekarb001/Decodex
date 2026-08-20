package com.toi.decodex.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toi.decodex.ui.state.GameUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    uiState: GameUiState,
    onCellClick: (Int, Int) -> Unit,
    onKey: (String) -> Unit,
    onBackspace: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(text = uiState.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = uiState.theme, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text(
                        text = formatTime(uiState.secondsElapsed),
                        modifier = Modifier.padding(end = 16.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Clue Display
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (uiState.currentDirection == com.toi.decodex.ui.state.Direction.ACROSS) "ACROSS" else "DOWN",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = uiState.activeClue?.clueText ?: "Select a cell to see clue",
                        fontSize = 18.sp,
                        lineHeight = 24.sp
                    )
                }
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                CrosswordGrid(
                    grid = uiState.grid,
                    rows = uiState.rowCount,
                    cols = uiState.colCount,
                    onCellClick = onCellClick
                )
                
                if (uiState.isWon) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Card {
                                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Puzzle Solved!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                    Text("Time: ${formatTime(uiState.secondsElapsed)}", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = onBack) { Text("Back to Puzzles") }
                                }
                            }
                        }
                    }
                }
            }

            CrosswordKeyboard(onKey = onKey, onBackspace = onBackspace)
        }
    }
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}
