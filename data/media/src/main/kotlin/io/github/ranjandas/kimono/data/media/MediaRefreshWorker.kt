package io.github.ranjandas.kimono.data.media

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/** Refreshes "continue watching" in the background, retrying with backoff when a source can't be reached. */
class MediaRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val result = MediaRepository.create(applicationContext).refresh()
        return if (shouldRetry(result, runAttemptCount)) Result.retry() else Result.success()
    }

    companion object {
        private const val WORK_NAME = "media-refresh"
        private const val REFRESH_MINUTES = 30L
        private const val BACKOFF_MINUTES = 1L

        /** Retrying more than this waits for the next period instead, so a dead server isn't polled all day. */
        internal const val MAX_ATTEMPTS = 3

        internal fun shouldRetry(result: RefreshResult, runAttemptCount: Int) =
            result.failed.isNotEmpty() && runAttemptCount < MAX_ATTEMPTS

        /** Starts the periodic refresh when there are sources to read, and stops it when there are none. */
        fun schedule(context: Context, repository: MediaRepository) {
            val workManager = WorkManager.getInstance(context)
            if (!repository.hasSources) {
                workManager.cancelUniqueWork(WORK_NAME)
                return
            }
            val request = PeriodicWorkRequestBuilder<MediaRefreshWorker>(REFRESH_MINUTES, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_MINUTES, TimeUnit.MINUTES)
                .build()
            workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
