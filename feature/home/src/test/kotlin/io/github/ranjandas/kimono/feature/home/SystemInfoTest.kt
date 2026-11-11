package io.github.ranjandas.kimono.feature.home

import java.time.Duration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SystemInfoTest {
    @Test
    fun `uptime counts whole days, and hours and minutes below a day`() {
        assertEquals("12m", formatUptime(Duration.ofMinutes(12)))
        assertEquals("5h 12m", formatUptime(Duration.ofHours(5).plusMinutes(12)))
        assertEquals("3 days", formatUptime(Duration.ofDays(3).plusHours(4).plusMinutes(59)))
        assertEquals("1 day", formatUptime(Duration.ofDays(1).plusHours(23)))
        assertEquals("0m", formatUptime(Duration.ofSeconds(30)))
    }

    @Test
    fun `free storage is in decimal gigabytes, with a decimal below ten`() {
        assertEquals("22 GB free", formatFree(Storage(22_300_000_000, 26_000_000_000)))
        assertEquals("3.4 GB free", formatFree(Storage(3_400_000_000, 32_000_000_000)))
        assertEquals("0.0 GB free", formatFree(Storage(0, 0)))
    }

    @Test
    fun `the storage bar fills with what's used`() {
        assertEquals(0.75f, Storage(8_000_000_000, 32_000_000_000).usedFraction)
        assertEquals(0f, Storage(0, 0).usedFraction)
    }
}
