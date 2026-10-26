package io.github.ranjandas.kimono.feature.home

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.delay

/** The current time from [clock], updated on each minute boundary. */
@Composable
internal fun rememberClock(clock: Clock): State<LocalDateTime> = produceState(LocalDateTime.now(clock), clock) {
    while (true) {
        val now = LocalDateTime.now(clock)
        value = now
        delay(Duration.between(now, now.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1)).toMillis())
    }
}

@Composable
internal fun is24HourClock(): Boolean = DateFormat.is24HourFormat(LocalContext.current)

/** The default network's transport, updated as it changes. */
@Composable
internal fun rememberNetworkStatus(): State<NetworkStatus> {
    val context = LocalContext.current
    val connectivity = context.getSystemService(ConnectivityManager::class.java)
    // Start from the current network, so the bar doesn't show offline until the first callback arrives.
    val initial = remember(context) {
        connectivity.getNetworkCapabilities(connectivity.activeNetwork)?.toStatus() ?: NetworkStatus.Offline
    }
    return produceState(initial, context) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                value = capabilities.toStatus()
            }

            override fun onLost(network: Network) {
                value = NetworkStatus.Offline
            }
        }
        connectivity.registerDefaultNetworkCallback(callback, Handler(Looper.getMainLooper()))
        awaitDispose { connectivity.unregisterNetworkCallback(callback) }
    }
}

private fun NetworkCapabilities.toStatus(): NetworkStatus = when {
    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkStatus.Ethernet
    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkStatus.Wifi
    else -> NetworkStatus.Other
}

/**
 * Media volume as a percentage, updated when it changes, or null when the device doesn't control volume itself
 * (for example when the remote sets the TV's volume over HDMI-CEC).
 */
@Composable
internal fun rememberVolumePercent(): State<Int?> {
    val context = LocalContext.current
    return produceState(readVolume(context), context) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                value = readVolume(context)
            }
        }
        // Stream volumes are stored in system settings, so this fires on every volume change.
        context.contentResolver.registerContentObserver(Settings.System.CONTENT_URI, true, observer)
        awaitDispose { context.contentResolver.unregisterContentObserver(observer) }
    }
}

private fun readVolume(context: Context): Int? {
    val audio = context.getSystemService(AudioManager::class.java)
    val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    return if (audio.isVolumeFixed ||
        max <= 0
    ) {
        null
    } else {
        audio.getStreamVolume(AudioManager.STREAM_MUSIC) * PERCENT / max
    }
}

private const val PERCENT = 100

internal enum class NetworkStatus(val icon: KimonoIcon, val label: String) {
    Wifi(KimonoIcon.Wifi, "wifi"),
    Ethernet(KimonoIcon.Ethernet, "ethernet"),
    Other(KimonoIcon.Wifi, "online"),
    Offline(KimonoIcon.WifiOff, "offline"),
}
