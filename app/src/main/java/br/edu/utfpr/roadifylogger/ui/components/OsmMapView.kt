package br.edu.utfpr.roadifylogger.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline

@Composable
fun OsmRouteMap(
    points: List<Pair<Double, Double>>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                isTilesScaledToDpi = true
            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            if (points.isNotEmpty()) {
                val geoPoints = points.map { GeoPoint(it.first, it.second) }

                // Draw the route polyline
                val polyline = Polyline().apply {
                    setPoints(geoPoints)
                    outlinePaint.color = android.graphics.Color.parseColor("#3F51B5") // Material Primary color approx
                    outlinePaint.strokeWidth = 12f
                }
                mapView.overlays.add(polyline)

                // Center map on the middle point of the route and zoom in
                val centerPoint = geoPoints[geoPoints.size / 2]
                mapView.controller.setZoom(15.0)
                mapView.controller.setCenter(centerPoint)
            } else {
                // Fallback default center if no points exist (e.g., Pato Branco region)
                mapView.controller.setZoom(5.0)
                mapView.controller.setCenter(GeoPoint(-26.2631, -53.0714))
            }
            mapView.invalidate()
        }
    )
}