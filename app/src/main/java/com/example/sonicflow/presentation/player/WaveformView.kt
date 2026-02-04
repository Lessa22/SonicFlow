package com.example.sonicflow.presentation.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun WaveformView(
    amplitudes: List<Float>,
    progress: Float,
    modifier: Modifier = Modifier,
    onSeek: (Float) -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val seekPosition = offset.x / size.width
                    onSeek(seekPosition)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val barWidth = width / amplitudes.size
        val progressWidth = width * progress

        amplitudes.forEachIndexed { index, amplitude ->
            val x = index * barWidth
            val barHeight = height * amplitude * 0.8f
            val y = (height - barHeight) / 2f

            val color = if (x < progressWidth) primaryColor else surfaceVariant

            drawLine(
                color = color,
                start = Offset(x, y),
                end = Offset(x, y + barHeight),
                strokeWidth = barWidth * 0.7f
            )
        }
    }
}