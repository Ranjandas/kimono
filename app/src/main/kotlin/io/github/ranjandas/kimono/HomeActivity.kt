package io.github.ranjandas.kimono

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.Text

// Placeholder colours (Tokyo Night) until the theme system lands in M1-1.
private val PlaceholderBackground = Color(0xFF1A1B26)
private val PlaceholderText = Color(0xFFC0CAF5)

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(
                modifier = Modifier.fillMaxSize().background(PlaceholderBackground),
                contentAlignment = Alignment.Center,
            ) {
                Text("kimono", color = PlaceholderText)
            }
        }
    }
}
