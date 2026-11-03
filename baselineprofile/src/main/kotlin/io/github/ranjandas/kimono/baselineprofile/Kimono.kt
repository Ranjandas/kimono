package io.github.ranjandas.kimono.baselineprofile

import android.content.ComponentName
import android.content.Intent

internal const val KIMONO_PACKAGE = "io.github.ranjandas.kimono"

/** Kimono is a LEANBACK_LAUNCHER app, which the default launch intent lookup doesn't find. */
internal fun homeIntent(): Intent = Intent(Intent.ACTION_MAIN)
    .setComponent(ComponentName(KIMONO_PACKAGE, "$KIMONO_PACKAGE.HomeActivity"))
    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
