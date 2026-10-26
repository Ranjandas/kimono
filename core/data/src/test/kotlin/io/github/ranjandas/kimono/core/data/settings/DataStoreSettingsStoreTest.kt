package io.github.ranjandas.kimono.core.data.settings

import app.cash.turbine.test
import io.github.ranjandas.kimono.core.data.InMemoryDataStore
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DataStoreSettingsStoreTest {
    private val dataStore = InMemoryDataStore()
    private val store = DataStoreSettingsStore(dataStore)

    @Test
    fun `starts with every setting at its default`() = runTest {
        store.settings.test { assertEquals(LauncherSettings(), awaitItem()) }
    }

    @Test
    fun `keeps every setting`() = runTest {
        val changed = LauncherSettings(
            themeName = "Gruvbox",
            cornerStyleName = "Round",
            clockFormat = ClockFormat.TwentyFourHour,
            textSize = TextSize.Large,
        )
        store.update { changed }
        DataStoreSettingsStore(dataStore).settings.test { assertEquals(changed, awaitItem()) }
    }

    @Test
    fun `an update leaves the other settings alone`() = runTest {
        store.update { it.copy(themeName = "Nord") }
        store.update { it.copy(textSize = TextSize.Large) }
        store.settings.test {
            val settings = awaitItem()
            assertEquals("Nord", settings.themeName)
            assertEquals(TextSize.Large, settings.textSize)
        }
    }

    @Test
    fun `going back to the default theme forgets the stored name`() = runTest {
        store.update { it.copy(themeName = "Nord") }
        store.update { it.copy(themeName = null) }
        store.settings.test { assertEquals(null, awaitItem().themeName) }
    }
}
