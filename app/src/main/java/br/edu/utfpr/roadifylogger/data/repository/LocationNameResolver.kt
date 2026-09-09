package br.edu.utfpr.roadifylogger.data.repository

import android.content.Context
import android.location.Geocoder
import java.text.Normalizer
import java.util.Locale

/** Resolves the initial GPS fix to a readable, file-system-safe place name. */
object LocationNameResolver {

    fun resolve(context: Context, latitude: Double?, longitude: Double?): String? {
        if (latitude == null || longitude == null) return null

        return runCatching {
            if (!Geocoder.isPresent()) return@runCatching null

            @Suppress("DEPRECATION")
            Geocoder(context, Locale.getDefault())
                .getFromLocation(latitude, longitude, 1)
                ?.firstOrNull()
                ?.let { address ->
                    address.getAddressLine(0)?.takeIf { it.isNotBlank() }
                        ?: address.locality
                        ?: address.subAdminArea
                        ?: address.adminArea
                }
        }.getOrNull()?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun asFileName(locationName: String?, fallback: String): String {
        val normalized = locationName
            ?.let { Normalizer.normalize(it, Normalizer.Form.NFD).replace("\\p{M}".toRegex(), "") }
            ?.replace(ILLEGAL_FILE_CHARACTERS, "-")
            ?.replace(WHITESPACE, " ")
            ?.trim()
            ?.trim('.')
            ?.take(MAX_FILE_NAME_LENGTH)
            ?.trim()

        return if (normalized.isNullOrEmpty()) fallback else normalized
    }

    private val ILLEGAL_FILE_CHARACTERS = "[\\\\/:*?\"<>|]".toRegex()
    private val WHITESPACE = "\\s+".toRegex()
    private const val MAX_FILE_NAME_LENGTH = 80
}
