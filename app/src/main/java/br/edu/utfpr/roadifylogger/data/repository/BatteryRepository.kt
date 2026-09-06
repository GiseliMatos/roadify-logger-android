package br.edu.utfpr.roadifylogger.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import br.edu.utfpr.roadifylogger.data.model.BatteryStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Streams battery level/temperature via the sticky [Intent.ACTION_BATTERY_CHANGED] broadcast. */
class BatteryRepository(private val context: Context) {

    fun observe(): Flow<BatteryStatus> = callbackFlow {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)

        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent ?: return
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val percent = if (level >= 0 && scale > 0) (level * 100 / scale) else 0
                val tenthsOfCelsius = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                trySend(
                    BatteryStatus(
                        levelPercent = percent,
                        temperatureCelsius = tenthsOfCelsius / 10f,
                    ),
                )
            }
        }

        // The sticky broadcast delivers the current state immediately on registration.
        context.registerReceiver(receiver, filter)

        awaitClose { context.unregisterReceiver(receiver) }
    }
}
