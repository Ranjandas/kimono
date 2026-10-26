package io.github.ranjandas.kimono

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.tv.material3.Text
import io.github.ranjandas.kimono.core.design.icon.KimonoIcon
import io.github.ranjandas.kimono.core.design.theme.KimonoTheme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KimonoTheme {
                // Placeholder until the home layout lands in M1-6.
                Column(
                    modifier = Modifier.fillMaxSize().background(KimonoTheme.colors.background),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("${KimonoIcon.Home.text} kimono", style = KimonoTheme.typography.display)
                    Text(
                        listOf(KimonoIcon.Wifi, KimonoIcon.Volume, KimonoIcon.Clock, KimonoIcon.Settings)
                            .joinToString(separator = "  ", prefix = "body 20 sp  ") { it.text },
                        style = KimonoTheme.typography.body,
                        color = KimonoTheme.colors.textMuted,
                    )
                }
            }
        }
    }
}
