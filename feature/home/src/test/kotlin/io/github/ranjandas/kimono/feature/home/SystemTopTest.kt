package io.github.ranjandas.kimono.feature.home

import android.os.PowerManager
import java.time.Duration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SystemTopTest {
    @Test
    fun `memory is used over total, in decimal gigabytes`() {
        assertEquals("1.4 / 3.8 GB", formatMemory(Memory(1_400_000_000, 3_800_000_000)))
        assertEquals(0.5f, Memory(1, 2).fraction)
        assertEquals(0f, Memory(1, 0).fraction)
    }

    @Test
    fun `clock is in gigahertz from one up, megahertz below`() {
        assertEquals("1.4 GHz", formatClock(1_400_000))
        assertEquals("850 MHz", formatClock(850_000))
    }

    @Test
    fun `cpu clock averages the cores against the fastest`() {
        val clock = cpuClockOf(listOf(CpuClock(1_000_000, 1_800_000), CpuClock(2_000_000, 2_000_000)))
        assertEquals(CpuClock(1_500_000, 2_000_000), clock)
        assertNull(cpuClockOf(emptyList()))
    }

    @Test
    fun `rates are per second, and a counter that went back gives none`() {
        val earlier = TrafficSample(rxBytes = 1_000, txBytes = 500, atMillis = 10_000)
        assertEquals(Traffic(1_000, 250), TrafficSample(3_000, 1_000, 12_000).rateSince(earlier))
        assertNull(TrafficSample(0, 1_000, 12_000).rateSince(earlier))
        assertNull(earlier.rateSince(earlier))
    }

    @Test
    fun `rates use decimal units`() {
        assertEquals("0 B/s", formatRate(0))
        assertEquals("40 KB/s", formatRate(40_500))
        assertEquals("1.2 MB/s", formatRate(1_234_000))
    }

    @Test
    fun `heat has three steps`() {
        assertEquals(Heat.Normal, heatOf(PowerManager.THERMAL_STATUS_NONE))
        assertEquals(Heat.Warm, heatOf(PowerManager.THERMAL_STATUS_MODERATE))
        assertEquals(Heat.Hot, heatOf(PowerManager.THERMAL_STATUS_SEVERE))
    }

    @Test
    fun `screen time shows hours and minutes`() {
        assertEquals("1h 12m", formatScreenTime(Duration.ofMinutes(72)))
        assertEquals("20m", formatScreenTime(Duration.ofMinutes(20)))
        assertEquals("<1m", formatScreenTime(Duration.ofSeconds(30)))
    }
}
