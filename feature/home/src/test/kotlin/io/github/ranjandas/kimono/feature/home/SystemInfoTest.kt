package io.github.ranjandas.kimono.feature.home

import java.time.Duration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SystemInfoTest {
    @Test
    fun `uptime shows the largest two units`() {
        assertEquals("12m", formatUptime(Duration.ofMinutes(12)))
        assertEquals("5h 12m", formatUptime(Duration.ofHours(5).plusMinutes(12)))
        assertEquals("3d 4h", formatUptime(Duration.ofDays(3).plusHours(4).plusMinutes(59)))
        assertEquals("0m", formatUptime(Duration.ofSeconds(30)))
    }

    @Test
    fun `storage is in decimal gigabytes`() {
        assertEquals("12.3 of 32 GB free", formatStorage(Storage(12_300_000_000, 32_000_000_000)))
        assertEquals("0.0 of 0 GB free", formatStorage(Storage(0, 0)))
    }
}
