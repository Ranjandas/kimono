package io.github.ranjandas.kimono

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Debug builds only: asks for `READ_TV_LISTINGS`, then dumps Watch Next and preview rows (#143).
 * Open it with `adb shell am start -n io.github.ranjandas.kimono/.WatchNextProbeActivity`.
 */
class WatchNextProbeActivity : ComponentActivity() {
    private val request = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        show(if (granted) WatchNextProbe.dump(contentResolver) else listOf("READ_TV_LISTINGS denied"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        request.launch(READ_TV_LISTINGS)
    }

    private fun show(lines: List<String>) {
        lines.forEach { Log.i(WatchNextProbe.TAG, it) }
        setContentView(TextView(this).apply { text = lines.joinToString("\n") })
    }

    private companion object {
        const val READ_TV_LISTINGS = "android.permission.READ_TV_LISTINGS"
    }
}
