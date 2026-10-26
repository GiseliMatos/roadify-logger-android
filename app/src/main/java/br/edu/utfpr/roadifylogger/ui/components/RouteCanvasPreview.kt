package br.edu.utfpr.roadifylogger.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import br.edu.utfpr.roadifylogger.R

@Composable
fun RouteCanvasPreview(
    points: List<Pair<Double, Double>>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(id = R.string.summary_no_gps),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val minLat = points.minOf { it.first }
    val maxLat = points.maxOf { it.first }
    val minLon = points.minOf { it.second }
    val maxLon = points.maxOf { it.second }

    val latSpan = maxLat - minLat
    val lonSpan = maxLon - minLon

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 36f

        drawRect(color = surfaceVariant.copy(alpha = 0.2f))

        if (latSpan == 0.0 && lonSpan == 0.0) {
            val centerOffset = Offset(width / 2f, height / 2f)
            drawCircle(color = primaryColor, radius = 10f, center = centerOffset)
            drawCircle(color = primaryColor.copy(alpha = 0.3f), radius = 22f, center = centerOffset)
        } else {
            val drawWidth = width - (padding * 2)
            val drawHeight = height - (padding * 2)

            val scaledPoints = points.map { (lat, lon) ->
                val x = padding + ((lon - minLon) / lonSpan).toFloat() * drawWidth
                val y = height - (padding + ((lat - minLat) / latSpan).toFloat() * drawHeight)
                Offset(x, y)
            }

            val path = Path().apply {
                scaledPoints.forEachIndexed { index, offset ->
                    if (index == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                }
            }

            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(
                    width = 5f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            scaledPoints.firstOrNull()?.let {
                drawCircle(color = androidx.compose.ui.graphics.Color(0xFF4CAF50), radius = 8f, center = it)
            }
            scaledPoints.lastOrNull()?.let {
                drawCircle(color = androidx.compose.ui.graphics.Color(0xFFE53935), radius = 8f, center = it)
            }
        }
    }
}