package br.edu.utfpr.roadifylogger.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sqrt

// Desenha o nível e movimenta a bolha de acordo com os valores de Roll e Pitch.
@Composable
fun BubbleLevel(
    roll: Float,
    pitch: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.aspectRatio(1f)
    ) {
        val center = Offset(
            x = size.width / 2f,
            y = size.height / 2f
        )

        val levelRadius = size.minDimension / 2f
        val bubbleRadius = levelRadius * 0.12f
        val targetRadius = levelRadius * 0.16f
        val maximumOffset = levelRadius - bubbleRadius

        val maximumAngle = 20f

        var bubbleOffsetX =
            (roll / maximumAngle).coerceIn(-1f, 1f) * maximumOffset

        var bubbleOffsetY =
            (pitch / maximumAngle).coerceIn(-1f, 1f) * maximumOffset

        val distanceFromCenter = sqrt(
            bubbleOffsetX * bubbleOffsetX +
                    bubbleOffsetY * bubbleOffsetY
        )

        if (distanceFromCenter > maximumOffset) {
            val adjustment = maximumOffset / distanceFromCenter
            bubbleOffsetX *= adjustment
            bubbleOffsetY *= adjustment
        }

        val bubbleCenter = Offset(
            x = center.x + bubbleOffsetX,
            y = center.y + bubbleOffsetY
        )

        drawCircle(
            color = Color(0xFFF2EFF4),
            radius = levelRadius,
            center = center
        )

        drawLine(
            color = Color(0xFF8A858F),
            start = Offset(center.x, center.y - levelRadius),
            end = Offset(center.x, center.y + levelRadius),
            strokeWidth = 2f
        )

        drawLine(
            color = Color(0xFF8A858F),
            start = Offset(center.x - levelRadius, center.y),
            end = Offset(center.x + levelRadius, center.y),
            strokeWidth = 2f
        )

        drawCircle(
            color = Color(0xFF7D7882),
            radius = levelRadius,
            center = center,
            style = Stroke(width = 6f)
        )

        drawCircle(
            color = Color(0xFFB3261E),
            radius = targetRadius,
            center = center,
            style = Stroke(width = 4f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF8A8A),
                    Color(0xFFC62828)
                ),
                center = bubbleCenter,
                radius = bubbleRadius
            ),
            radius = bubbleRadius,
            center = bubbleCenter
        )
    }
}