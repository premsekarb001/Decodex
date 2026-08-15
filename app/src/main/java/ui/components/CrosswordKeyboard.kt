package com.toi.decodex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CrosswordKeyboard(onKey: (String) -> Unit, onBackspace: () -> Unit) {
    val rows = listOf(
        listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
        listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
        listOf("Z", "X", "C", "V", "B", "N", "M", "⌫")
    )

    Surface(color = Color(0xFFE2E8F0), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            rows.forEach { rowKeys ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowKeys.forEach { key ->
                        val weight = if (key == "⌫") 1.5f else 1.0f
                        Box(
                            modifier = Modifier
                                .weight(weight)
                                .height(44.dp)
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .clickable { if (key == "⌫") onBackspace() else onKey(key) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = key, fontSize = 16.sp, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}