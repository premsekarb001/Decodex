package com.toi.decodex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toi.decodex.ui.state.CellUiState

@Composable
fun CrosswordGrid(grid: Map<Pair<Int, Int>, CellUiState>, rows: Int, cols: Int, onCellClick: (Int, Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp).border(2.dp, Color.Black)) {
        for (r in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (c in 0 until cols) {
                    val state = grid[Pair(r, c)] ?: CellUiState(isBlackBlock = true)
                    val bgColor = when {
                        state.isBlackBlock -> Color.Black
                        state.isFocused -> Color(0xFFFFEB3B)
                        state.isHighlighted -> Color(0xFFFFF9C4)
                        else -> Color.White
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(bgColor)
                            .border(0.5.dp, Color.DarkGray)
                            .clickable(enabled = !state.isBlackBlock) { onCellClick(r, c) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!state.isBlackBlock) {
                            if (state.clueNumber != null) {
                                Text(
                                    text = state.clueNumber.toString(),
                                    fontSize = 9.sp,
                                    color = Color.Black,
                                    modifier = Modifier.align(Alignment.TopStart).padding(2.dp)
                                )
                            }
                            Text(text = state.letter, fontSize = 18.sp, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}
