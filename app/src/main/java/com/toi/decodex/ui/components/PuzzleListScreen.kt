package com.toi.decodex.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toi.decodex.data.local.PuzzleEntity

@Composable
fun PuzzleListScreen(puzzles: List<PuzzleEntity>, onPuzzleClick: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).statusBarsPadding()) {
        Text(text = "Decodex Puzzles", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(puzzles) { puzzle ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onPuzzleClick(puzzle.puzzleId) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = puzzle.title ?: "", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Theme: ${puzzle.theme ?: ""} • Difficulty: ${puzzle.difficulty ?: ""}", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
