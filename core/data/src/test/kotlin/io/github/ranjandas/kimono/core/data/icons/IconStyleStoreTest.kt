package io.github.ranjandas.kimono.core.data.icons

import app.cash.turbine.test
import io.github.ranjandas.kimono.core.data.InMemoryDataStore
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IconStyleStoreTest {
    private val store = DataStoreIconStyleStore(InMemoryDataStore())

    @Test
    fun `starts with every app on the default`() = runTest {
        store.styles.test { assertEquals(emptyMap<String, IconStyle>(), awaitItem()) }
    }

    @Test
    fun `keeps a style per app`() = runTest {
        store.set("pkg.kayo", IconStyle.Wordmark)
        store.set("pkg.stan", IconStyle.Monogram)
        store.styles.test {
            assertEquals(mapOf("pkg.kayo" to IconStyle.Wordmark, "pkg.stan" to IconStyle.Monogram), awaitItem())
        }
    }

    @Test
    fun `going back to the default forgets the app`() = runTest {
        store.set("pkg.kayo", IconStyle.Wordmark)
        store.set("pkg.kayo", IconStyle.Logo)
        store.styles.test { assertEquals(emptyMap<String, IconStyle>(), awaitItem()) }
    }

    @Test
    fun `next steps through the styles and wraps round`() {
        assertEquals(IconStyle.Wordmark, IconStyle.Logo.next)
        assertEquals(IconStyle.Monogram, IconStyle.Wordmark.next)
        assertEquals(IconStyle.Logo, IconStyle.Monogram.next)
    }
}
