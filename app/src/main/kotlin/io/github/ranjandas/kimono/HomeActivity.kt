package io.github.ranjandas.kimono

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import io.github.ranjandas.kimono.core.design.component.ComponentSampler
import io.github.ranjandas.kimono.core.design.theme.CornerStyle
import io.github.ranjandas.kimono.core.design.theme.ThemeId

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Placeholder until the home layout lands in M1-6.
            ComponentSampler(ThemeId.Default, CornerStyle.Default, modifier = Modifier.fillMaxSize())
        }
    }
}
