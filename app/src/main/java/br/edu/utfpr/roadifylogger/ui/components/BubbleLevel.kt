package br.edu.utfpr.roadifylogger.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sqrt
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import br.edu.utfpr.roadifylogger.data.model.BubbleViscosity

// Desenha o nível e movimenta a bolha de acordo com os valores de Roll e Pitch.
@Composable
fun BubbleLevel(
    roll: Float,
    pitch: Float,
    modifier: Modifier = Modifier,
    viscosity: BubbleViscosity,
    economyMode: Boolean
) {
    val animationDuration = when (viscosity) {
        BubbleViscosity.LOW -> 120
        BubbleViscosity.MEDIUM -> 180
        BubbleViscosity.HIGH -> 360
    }

    val displayedRoll by animateFloatAsState(
        targetValue = roll,
        animationSpec =
            if (economyMode) {
                snap()
            } else {
                tween(durationMillis = animationDuration)
            },
        label = "bubbleRoll"
    )

    val displayedPitch by animateFloatAsState(
        targetValue = pitch,
        animationSpec =
            if (economyMode) {
                snap()
            } else {
                tween(durationMillis = animationDuration)
            },
        label = "bubblePitch"
    )

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
            -(displayedRoll / maximumAngle)
                .coerceIn(-1f, 1f) * maximumOffset

        var bubbleOffsetY =
          -(displayedPitch / maximumAngle)
                .coerceIn(-1f, 1f) * maximumOffset

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

@Composable
fun LinearBubbleLevel(
    value: Float,
    modifier: Modifier = Modifier,
    viscosity: BubbleViscosity,
    economyMode: Boolean
) {
    val animationDuration = when (viscosity) {
        BubbleViscosity.LOW -> 120
        BubbleViscosity.MEDIUM -> 180
        BubbleViscosity.HIGH -> 360
    }

    val displayedValue by animateFloatAsState(
        targetValue = value,
        animationSpec =
            if (economyMode) {
                snap()
            } else {
                tween(durationMillis = animationDuration)
            },
        label = "linearBubbleValue"
    )

    Canvas(
        modifier = modifier.aspectRatio(4.5f)
    ) {
        val center = Offset(
            x = size.width / 2f,
            y = size.height / 2f
        )

        val borderWidth = 6f
        val bubbleRadius = size.height * 0.32f
        val maximumAngle = 20f
        val maximumOffset =
            size.width / 2f - bubbleRadius - borderWidth

        val bubbleOffset =
            -(displayedValue / maximumAngle)
                .coerceIn(-1f, 1f) * maximumOffset

        val bubbleCenter = Offset(
            x = center.x + bubbleOffset,
            y = center.y
        )

        drawRoundRect(
            color = Color(0xFFF2EFF4),
            cornerRadius = CornerRadius(
                x = size.height / 2f,
                y = size.height / 2f
            )
        )

        drawRoundRect(
            color = Color(0xFF7D7882),
            cornerRadius = CornerRadius(
                x = size.height / 2f,
                y = size.height / 2f
            ),
            style = Stroke(width = borderWidth)
        )

        val targetDistance = size.height * 0.55f

        drawLine(
            color = Color(0xFFB3261E),
            start = Offset(
                x = center.x - targetDistance,
                y = borderWidth
            ),
            end = Offset(
                x = center.x - targetDistance,
                y = size.height - borderWidth
            ),
            strokeWidth = 4f
        )

        drawLine(
            color = Color(0xFFB3261E),
            start = Offset(
                x = center.x + targetDistance,
                y = borderWidth
            ),
            end = Offset(
                x = center.x + targetDistance,
                y = size.height - borderWidth
            ),
            strokeWidth = 4f
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