package com.toi.decodex.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CrosswordKeyboard(onKey: (String) -> Unit, onBackspace: () -> Unit) {
    val rows = listOf(
        listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
        listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
        listOf("Z", "X", "C", "V", "B", "N", "M")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .navigationBarsPadding()
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                row.forEach { char ->
                    Button(
                        onClick = { onKey(char) },
                        modifier = Modifier
                            .padding(2.dp)
                            .weight(1f, fill = false),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = char, fontSize = 16.sp)
                    }
                }
                if (row === rows.last()) {
                    IconButton(onClick = onBackspace) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Backspace"
                        )
                    }
                }
            }
        }
    }
}
