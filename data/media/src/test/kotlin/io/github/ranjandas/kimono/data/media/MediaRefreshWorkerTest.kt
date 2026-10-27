package io.github.ranjandas.kimono.data.media

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MediaRefreshWorkerTest {
    private val ok = RefreshResult(refreshed = setOf("jellyfin"), failed = emptySet())
    private val failed = RefreshResult(refreshed = emptySet(), failed = setOf("jellyfin"))

    @Test
    fun `retries when a source failed`() {
        assertTrue(MediaRefreshWorker.shouldRetry(failed, runAttemptCount = 0))
    }

    @Test
    fun `doesn't retry when everything refreshed`() {
        assertFalse(MediaRefreshWorker.shouldRetry(ok, runAttemptCount = 0))
    }

    @Test
    fun `waits for the next period after the last attempt`() {
        assertFalse(MediaRefreshWorker.shouldRetry(failed, runAttemptCount = MediaRefreshWorker.MAX_ATTEMPTS))
    }
}
