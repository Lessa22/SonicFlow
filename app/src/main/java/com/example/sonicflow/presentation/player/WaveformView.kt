package com.example.sonicflow.presentation.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
            .height(60.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val seekPosition = offset.x / size.width
                    onSeek(seekPosition)
                }
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barCount = amplitudes.size
        val barWidth = (canvasWidth / barCount) * 0.6f
        val spacing = (canvasWidth / barCount) * 0.4f
        val progressWidth = canvasWidth * progress

        amplitudes.forEachIndexed { index, amplitude ->
            val x = index * (barWidth + spacing) + spacing / 2
            val normalizedAmplitude = amplitude.coerceIn(0.1f, 1f)
            val barHeight = canvasHeight * normalizedAmplitude * 0.9f
            val y = (canvasHeight - barHeight) / 2f

            val color = if (x < progressWidth) primaryColor else surfaceVariant

            // Dessiner une barre arrondie
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}