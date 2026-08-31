package br.edu.utfpr.roadifylogger.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import br.edu.utfpr.pb.dainf.medicaosensores.data.model.MotionSample
import br.edu.utfpr.pb.dainf.medicaosensores.ui.theme.RoadifyAccelX
import br.edu.utfpr.pb.dainf.medicaosensores.ui.theme.RoadifyAccelY
import br.edu.utfpr.pb.dainf.medicaosensores.ui.theme.RoadifyAccelZ
import br.edu.utfpr.roadifylogger.data.model.MotionSample
import kotlin.math.abs
import kotlin.math.max

/**
 * Reference full-scale magnitudes used as a floor when auto-scaling the charts below.
 *
 * Without a floor, both charts scale to the *current window's own* max value - so a
 * roughly-constant signal (Earth's magnetic field at rest, gyroscope drift, residual
 * accelerometer noise) ends up being treated as "the max" and gets stretched to fill
 * the whole chart, which reads as constant noisy movement even when the device is
 * perfectly still. Anchoring the scale to a realistic full-scale value keeps quiet
 * signals visually quiet, while still auto-expanding for genuinely large readings.
 */
private const val ACCEL_SCALE_FLOOR = 12f // m/s², linear (gravity-compensated) acceleration
private const val GYRO_SCALE_FLOOR = 4f // rad/s
private const val MAGNETOMETER_SCALE_FLOOR = 80f // µT (Earth's field is ~25-65 µT)
private const val DEFAULT_SCALE_FLOOR = 10f

/** Small bar chart showing the magnitude of recent samples, used on dashboard cards. */
@Composable
fun MiniMagnitudeBarChart(
    history: List<MotionSample>,
    modifier: Modifier = Modifier,
    scaleFloor: Float = DEFAULT_SCALE_FLOOR,
    barColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
) {
    Canvas(modifier = modifier.fillMaxWidth().height(56.dp)) {
        if (history.isEmpty()) return@Canvas
        val magnitudes = history.map { abs(it.x) + abs(it.y) + abs(it.z) }
        val maxMag = max(magnitudes.maxOrNull() ?: scaleFloor, scaleFloor)
        val barWidth = size.width / (magnitudes.size * 1.6f)
        val gap = barWidth * 0.6f
        magnitudes.forEachIndexed { index, mag ->
            val barHeight = (mag / maxMag).coerceIn(0f, 1f) * size.height
            val x = index * (barWidth + gap)
            drawLine(
                color = barColor,
                start = Offset(x, size.height),
                end = Offset(x, size.height - barHeight),
                strokeWidth = barWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

/** Multi-line X/Y/Z chart used on the sensor detail screen. */
@Composable
fun TriAxisLineChart(
    history: List<MotionSample>,
    modifier: Modifier = Modifier,
    scaleFloor: Float = DEFAULT_SCALE_FLOOR,
) {
    Canvas(modifier = modifier.fillMaxWidth().height(180.dp)) {
        if (history.size < 2) return@Canvas

        val allValues = history.flatMap { listOf(it.x, it.y, it.z) }
        val maxAbs = max(allValues.maxOfOrNull { abs(it) } ?: scaleFloor, scaleFloor)
        val midY = size.height / 2f

        fun pathFor(selector: (MotionSample) -> Float, color: Color) {
            val stepX = size.width / (history.size - 1).coerceAtLeast(1)
            var prev: Offset? = null
            history.forEachIndexed { index, sample ->
                val value = selector(sample).coerceIn(-maxAbs, maxAbs)
                val y = midY - (value / maxAbs) * midY
                val point = Offset(index * stepX, y)
                prev?.let { start ->
                    drawLine(color = color, start = start, end = point, strokeWidth = 3.5f, cap = StrokeCap.Round)
                }
                prev = point
            }
        }

        // Zero baseline
        drawLine(
            color = Color.Gray.copy(alpha = 0.3f),
            start = Offset(0f, midY),
            end = Offset(size.width, midY),
            strokeWidth = 1.5f,
        )

        pathFor({ it.x }, RoadifyAccelX)
        pathFor({ it.y }, RoadifyAccelY)
        pathFor({ it.z }, RoadifyAccelZ)
    }
}

/** Picks a sensible fixed scale floor for a given sensor's magnitude/line charts. */
fun scaleFloorFor(kind: br.edu.utfpr.pb.dainf.medicaosensores.data.model.SensorKind): Float = when (kind) {
    br.edu.utfpr.roadifylogger.data.model.SensorKind.ACCELEROMETER -> ACCEL_SCALE_FLOOR
    br.edu.utfpr.roadifylogger.data.model.SensorKind.GYROSCOPE -> GYRO_SCALE_FLOOR
    br.edu.utfpr.roadifylogger.data.model.SensorKind.MAGNETOMETER -> MAGNETOMETER_SCALE_FLOOR
    else -> DEFAULT_SCALE_FLOOR
}

/**
 * Single-line trend chart, min/max-normalized to the visible window - used for the
 * barometer. Unlike the accelerometer/gyroscope/magnetometer charts above, pressure
 * doesn't have a meaningful "zero" baseline to anchor a fixed scale to: it sits around
 * ~1013 hPa and what matters is the small trend/delta, so stretching that delta to
 * fill the chart is the *correct* behavior here rather than the noise problem those
 * fixed floors solve.
 */
@Composable
fun TrendLineChart(
    history: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Canvas(modifier = modifier.fillMaxWidth().height(56.dp)) {
        if (history.size < 2) return@Canvas
        val minV = history.min()
        val maxV = history.max()
        val range = (maxV - minV).coerceAtLeast(0.5f) // avoid a divide-by-near-zero flat line
        val stepX = size.width / (history.size - 1).coerceAtLeast(1)
        var prev: Offset? = null
        history.forEachIndexed { index, value ->
            val y = size.height - ((value - minV) / range) * size.height
            val point = Offset(index * stepX, y)
            prev?.let { start ->
                drawLine(color = color, start = start, end = point, strokeWidth = 3.5f, cap = StrokeCap.Round)
            }
            prev = point
        }
    }
}
